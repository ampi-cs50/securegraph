package ar.edu.utn.frlp.securegraph.service;

import ar.edu.utn.frlp.securegraph.domain.entity.Analisis;
import ar.edu.utn.frlp.securegraph.domain.entity.Componente;
import ar.edu.utn.frlp.securegraph.domain.entity.RelacionDependencia;
import ar.edu.utn.frlp.securegraph.repository.AnalisisRepository;
import ar.edu.utn.frlp.securegraph.repository.ComponenteRepository;
import ar.edu.utn.frlp.securegraph.repository.ProyectoRepository;
import ar.edu.utn.frlp.securegraph.repository.RelacionDependenciaRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.cyclonedx.Version;
import org.cyclonedx.parsers.JsonParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class SbomImportService {

    private final ObjectReader reader;
    private final JsonParser schemaValidator = new JsonParser();
    private final ProyectoRepository projects;
    private final AnalisisRepository analyses;
    private final ComponenteRepository components;
    private final RelacionDependenciaRepository dependencies;

    public SbomImportService(ObjectMapper mapper, ProyectoRepository projects, AnalisisRepository analyses,
                             ComponenteRepository components, RelacionDependenciaRepository dependencies) {
        this.reader = mapper.readerFor(JsonNode.class)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS,
                        DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        this.projects = projects;
        this.analyses = analyses;
        this.components = components;
        this.dependencies = dependencies;
    }

    @Transactional
    public ImportResult importSbom(Long projectId, MultipartFile file) {
        String filename = file == null ? null : file.getOriginalFilename();
        if (filename == null || file.isEmpty() || !filename.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new InvalidSbomException();
        }

        JsonNode document = readDocument(file);
        validateSchema(document);

        Map<String, Componente> nodes = new LinkedHashMap<>();
        Componente root = addComponent(document.path("metadata").path("component"), nodes);
        root.setRaiz(true);
        Deque<JsonNode> pending = new ArrayDeque<>();
        addChildren(document, pending);
        while (!pending.isEmpty()) {
            JsonNode next = pending.removeFirst();
            addComponent(next, nodes);
            addChildren(next, pending);
        }

        Map<String, Set<String>> edges = readEdges(document, nodes);
        validateAcyclic(nodes.keySet(), edges);
        String rootRef = root.getBomRef();
        Set<String> direct = edges.getOrDefault(rootRef, Set.of());

        Analisis analysis = new Analisis();
        analysis.setProyecto(projects.findById(projectId).orElseThrow(ProjectNotFoundException::new));
        // The root describes the project and is stored as a graph node, outside the library count.
        analysis.setTotalComponentes(nodes.size() - 1);
        Analisis saved = analyses.save(analysis);
        nodes.forEach((ref, component) -> {
            component.setAnalisis(saved);
            component.setEsDirecta(direct.contains(ref));
        });
        components.saveAll(nodes.values());
        dependencies.saveAll(edges.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(child ->
                        relation(saved, nodes.get(entry.getKey()), nodes.get(child), !rootRef.equals(entry.getKey()))))
                .toList());
        return new ImportResult(projectId, saved.getId(), filename, saved.getEstado());
    }

    private JsonNode readDocument(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            JsonNode document = reader.readValue(input);
            if (document == null || !document.isObject()) {
                throw new InvalidSbomException();
            }
            return document;
        } catch (IOException ex) {
            throw new InvalidSbomException(ex);
        }
    }

    private void validateSchema(JsonNode document) {
        Version version = switch (readText(document, "specVersion", 16)) {
            case "1.2" -> Version.VERSION_12;
            case "1.3" -> Version.VERSION_13;
            case "1.4" -> Version.VERSION_14;
            case "1.5" -> Version.VERSION_15;
            case "1.6" -> Version.VERSION_16;
            case null, default -> throw new InvalidSbomException();
        };
        try {
            if (!schemaValidator.validate(document, version).isEmpty()) {
                throw new InvalidSbomException();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load the CycloneDX JSON schema", ex);
        }
    }

    private static void addChildren(JsonNode container, Deque<JsonNode> pending) {
        JsonNode children = container.get("components");
        if (children != null) {
            if (!children.isArray()) {
                throw new InvalidSbomException();
            }
            children.forEach(pending::addLast);
        }
    }

    private static Componente addComponent(JsonNode node, Map<String, Componente> components) {
        if (!node.isObject() || readText(node, "type", 32) == null) {
            throw new InvalidSbomException();
        }
        String name = readText(node, "name", 150);
        if (name == null) {
            throw new InvalidSbomException();
        }
        Componente component = new Componente();
        component.setArtifactId(name);
        component.setGroupId(readText(node, "group", 150));
        component.setVersion(readText(node, "version", 50));
        component.setPurl(readText(node, "purl", 255));
        String scope = readText(node, "scope", 30);
        if (scope != null) {
            component.setScope(scope);
        }
        String ref = readText(node, "bom-ref", 255);
        if (ref == null) {
            ref = "urn:securegraph:component:" + components.size();
        }
        component.setBomRef(ref);
        if (components.putIfAbsent(ref, component) != null) {
            throw new InvalidSbomException();
        }
        return component;
    }

    private static Map<String, Set<String>> readEdges(JsonNode document, Map<String, Componente> nodes) {
        Map<String, Set<String>> edges = new LinkedHashMap<>();
        JsonNode graph = document.get("dependencies");
        if (graph == null) {
            return edges;
        }
        if (!graph.isArray()) {
            throw new InvalidSbomException();
        }
        for (JsonNode dependency : graph) {
            String parent = readText(dependency, "ref", 255);
            if (!nodes.containsKey(parent)) {
                throw new InvalidSbomException();
            }
            Set<String> children = edges.computeIfAbsent(parent, ignored -> new LinkedHashSet<>());
            JsonNode targets = dependency.get("dependsOn");
            if (targets == null) {
                continue;
            }
            if (!targets.isArray()) {
                throw new InvalidSbomException();
            }
            for (JsonNode target : targets) {
                if (!target.isTextual() || !nodes.containsKey(target.textValue())) {
                    throw new InvalidSbomException();
                }
                children.add(target.textValue());
            }
        }
        return edges;
    }

    private static void validateAcyclic(Set<String> nodes, Map<String, Set<String>> edges) {
        Map<String, Integer> incoming = new LinkedHashMap<>();
        nodes.forEach(ref -> incoming.put(ref, 0));
        edges.values().forEach(children -> children.forEach(ref -> incoming.merge(ref, 1, Integer::sum)));
        Deque<String> ready = new ArrayDeque<>();
        incoming.forEach((ref, count) -> {
            if (count == 0) {
                ready.addLast(ref);
            }
        });
        int visited = 0;
        while (!ready.isEmpty()) {
            String ref = ready.removeFirst();
            visited++;
            for (String child : edges.getOrDefault(ref, Set.of())) {
                if (incoming.merge(child, -1, Integer::sum) == 0) {
                    ready.addLast(child);
                }
            }
        }
        if (visited != nodes.size()) {
            throw new InvalidSbomException();
        }
    }

    private static String readText(JsonNode node, String field, int maxLength) {
        JsonNode value = node.get(field);
        if (value == null) {
            return null;
        }
        if (!value.isTextual() || value.textValue().isBlank() || value.textValue().length() > maxLength
                || value.textValue().indexOf(0) >= 0) {
            throw new InvalidSbomException();
        }
        return value.textValue();
    }

    private static RelacionDependencia relation(Analisis analysis, Componente parent, Componente child,
                                                boolean transitive) {
        RelacionDependencia relation = new RelacionDependencia();
        relation.setAnalisis(analysis);
        relation.setPadreComponente(parent);
        relation.setHijoComponente(child);
        relation.setTipoTransitiva(transitive);
        return relation;
    }

    public record ImportResult(Long projectId, Long analysisId, String filename, String status) {
    }

    public static class InvalidSbomException extends RuntimeException {
        public InvalidSbomException() {
        }

        public InvalidSbomException(IOException cause) {
            super(cause);
        }
    }

    public static class ProjectNotFoundException extends RuntimeException {
    }
}

package ar.edu.utn.frlp.securegraph.service;

import ar.edu.utn.frlp.securegraph.repository.RelacionDependenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DependencyGraphService {

    private final RelacionDependenciaRepository dependencias;

    public DependencyGraphService(RelacionDependenciaRepository dependencias) {
        this.dependencias = dependencias;
    }

    @Transactional(readOnly = true)
    public List<ComponentDepth> findMinimumDepths(long analysisId) {
        return dependencias.buscarProfundidadesMinimas(analysisId).stream()
                .map(row -> new ComponentDepth(row.getComponentId(), row.getBomRef(), row.getDepth()))
                .toList();
    }

    /** A null depth represents a component unreachable from the root. */
    public record ComponentDepth(long componentId, String bomRef, Integer depth) {
    }
}

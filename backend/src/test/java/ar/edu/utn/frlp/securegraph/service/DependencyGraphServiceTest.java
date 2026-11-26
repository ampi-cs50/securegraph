package ar.edu.utn.frlp.securegraph.service;

import ar.edu.utn.frlp.securegraph.repository.RelacionDependenciaRepository;
import ar.edu.utn.frlp.securegraph.repository.RelacionDependenciaRepository.ProfundidadComponente;
import ar.edu.utn.frlp.securegraph.service.DependencyGraphService.ComponentDepth;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DependencyGraphServiceTest {

    private final RelacionDependenciaRepository repository = mock(RelacionDependenciaRepository.class);
    private final DependencyGraphService service = new DependencyGraphService(repository);

    @Test
    void preservesRootAndUnreachableDepthsForScoringConsumers() {
        ProfundidadComponente root = mock(ProfundidadComponente.class);
        when(root.getComponentId()).thenReturn(10L);
        when(root.getBomRef()).thenReturn("root");
        when(root.getDepth()).thenReturn(0);
        ProfundidadComponente disconnected = mock(ProfundidadComponente.class);
        when(disconnected.getComponentId()).thenReturn(11L);
        when(disconnected.getBomRef()).thenReturn("disconnected");
        when(disconnected.getDepth()).thenReturn(null);
        when(repository.buscarProfundidadesMinimas(7L)).thenReturn(List.of(root, disconnected));

        assertThat(service.findMinimumDepths(7L)).containsExactly(
                new ComponentDepth(10L, "root", 0),
                new ComponentDepth(11L, "disconnected", null));
    }
}

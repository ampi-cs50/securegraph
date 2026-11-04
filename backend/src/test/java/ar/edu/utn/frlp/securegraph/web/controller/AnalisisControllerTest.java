package ar.edu.utn.frlp.securegraph.web.controller;

import ar.edu.utn.frlp.securegraph.service.AnalisisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalisisController.class)
@Import(AnalisisService.class)
class AnalisisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnSummaryForAnalysis() throws Exception {
        mockMvc.perform(get("/api/analisis/42/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").value(25))
                .andExpect(jsonPath("$.vulnerabilities").value(3));
    }

    @Test
    void shouldReturnPrioritizedFindingsForAnalysis() throws Exception {
        mockMvc.perform(get("/api/analisis/42/hallazgos").param("orden", "score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(42))
                .andExpect(jsonPath("$.findings[0].cveId").value("CVE-2021-44228"));
    }

    @Test
    void shouldReturnGraphForAnalysis() throws Exception {
        mockMvc.perform(get("/api/analisis/42/grafo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(42))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.edges").isArray());
    }

    @Test
    void shouldReturnConsolidatedReportForAnalysis() throws Exception {
        mockMvc.perform(get("/api/analisis/42/reporte"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(42))
                .andExpect(jsonPath("$.sbomFormat").value("CycloneDX-JSON"))
                .andExpect(jsonPath("$.findings").isArray());
    }
}


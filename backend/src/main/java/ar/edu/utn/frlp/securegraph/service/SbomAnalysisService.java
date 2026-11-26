package ar.edu.utn.frlp.securegraph.service;

import ar.edu.utn.frlp.securegraph.repository.AnalisisRepository;
import ar.edu.utn.frlp.securegraph.service.SbomImportService.ImportResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SbomAnalysisService {

    private final SbomImportService importer;
    private final AnalisisRepository analyses;
    private final VulnerabilityScanService scanner;

    public SbomAnalysisService(SbomImportService importer, AnalisisRepository analyses,
                               VulnerabilityScanService scanner) {
        this.importer = importer;
        this.analyses = analyses;
        this.scanner = scanner;
    }

    @Transactional
    public ImportResult analyze(Long projectId, MultipartFile file) {
        ImportResult result = importer.importSbom(projectId, file);
        var analysis = analyses.getReferenceById(result.analysisId());
        analysis.setTotalVulnerabilidades(scanner.scan(analysis));
        return result;
    }
}

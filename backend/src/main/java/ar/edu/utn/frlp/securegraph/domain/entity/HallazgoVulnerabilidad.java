package ar.edu.utn.frlp.securegraph.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "hallazgo_vulnerabilidad")
public class HallazgoVulnerabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analisis_id", nullable = false)
    private Analisis analisis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "componente_id", nullable = false)
    private Componente componente;

    @Column(name = "osv_id", nullable = false, length = 100)
    private String osvId;

    @Column(name = "cve_id", length = 50)
    private String cveId;

    @Column(name = "severidad_cvss", nullable = false, precision = 3, scale = 1)
    private BigDecimal severidadCvss = BigDecimal.ZERO;

    @Column(name = "nivel_severidad", nullable = false, length = 20)
    private String nivelSeveridad;

    @Column(nullable = false)
    private Integer profundidad = 1;

    @Column(name = "score_prioridad", nullable = false, precision = 4, scale = 1)
    private BigDecimal scorePrioridad = BigDecimal.ZERO;

    @Column(name = "razones_prioridad", columnDefinition = "TEXT")
    private String razonesPrioridad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Analisis getAnalisis() {
        return analisis;
    }

    public void setAnalisis(Analisis analisis) {
        this.analisis = analisis;
    }

    public Componente getComponente() {
        return componente;
    }

    public void setComponente(Componente componente) {
        this.componente = componente;
    }

    public String getOsvId() {
        return osvId;
    }

    public void setOsvId(String osvId) {
        this.osvId = osvId;
    }

    public String getCveId() {
        return cveId;
    }

    public void setCveId(String cveId) {
        this.cveId = cveId;
    }

    public BigDecimal getSeveridadCvss() {
        return severidadCvss;
    }

    public void setSeveridadCvss(BigDecimal severidadCvss) {
        this.severidadCvss = severidadCvss;
    }

    public String getNivelSeveridad() {
        return nivelSeveridad;
    }

    public void setNivelSeveridad(String nivelSeveridad) {
        this.nivelSeveridad = nivelSeveridad;
    }

    public Integer getProfundidad() {
        return profundidad;
    }

    public void setProfundidad(Integer profundidad) {
        this.profundidad = profundidad;
    }

    public BigDecimal getScorePrioridad() {
        return scorePrioridad;
    }

    public void setScorePrioridad(BigDecimal scorePrioridad) {
        this.scorePrioridad = scorePrioridad;
    }

    public String getRazonesPrioridad() {
        return razonesPrioridad;
    }

    public void setRazonesPrioridad(String razonesPrioridad) {
        this.razonesPrioridad = razonesPrioridad;
    }
}


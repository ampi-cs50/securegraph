package ar.edu.utn.frlp.securegraph.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "analisis")
public class Analisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "fecha_analisis", nullable = false)
    private LocalDateTime fechaAnalisis;

    @Column(name = "sbom_formato", nullable = false, length = 50)
    private String sbomFormato = "CycloneDX-JSON";

    @Column(nullable = false, length = 30)
    private String estado = "COMPLETED";

    @Column(name = "total_componentes", nullable = false)
    private Integer totalComponentes = 0;

    @Column(name = "total_vulnerabilidades", nullable = false)
    private Integer totalVulnerabilidades = 0;

    @PrePersist
    protected void onCreate() {
        if (this.fechaAnalisis == null) {
            this.fechaAnalisis = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    public LocalDateTime getFechaAnalisis() {
        return fechaAnalisis;
    }

    public void setFechaAnalisis(LocalDateTime fechaAnalisis) {
        this.fechaAnalisis = fechaAnalisis;
    }

    public String getSbomFormato() {
        return sbomFormato;
    }

    public void setSbomFormato(String sbomFormato) {
        this.sbomFormato = sbomFormato;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getTotalComponentes() {
        return totalComponentes;
    }

    public void setTotalComponentes(Integer totalComponentes) {
        this.totalComponentes = totalComponentes;
    }

    public Integer getTotalVulnerabilidades() {
        return totalVulnerabilidades;
    }

    public void setTotalVulnerabilidades(Integer totalVulnerabilidades) {
        this.totalVulnerabilidades = totalVulnerabilidades;
    }
}


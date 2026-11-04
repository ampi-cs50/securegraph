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

@Entity
@Table(name = "componente")
public class Componente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analisis_id", nullable = false)
    private Analisis analisis;

    @Column(name = "bom_ref", nullable = false)
    private String bomRef;

    @Column(name = "group_id", length = 150)
    private String groupId;

    @Column(name = "artifact_id", nullable = false, length = 150)
    private String artifactId;

    @Column(length = 50)
    private String version;

    @Column(length = 255)
    private String purl;

    @Column(length = 30)
    private String scope = "required";

    @Column(name = "es_directa", nullable = false)
    private Boolean esDirecta = false;

    @Column(name = "modulo_contexto", length = 100)
    private String moduloContexto;

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

    public String getBomRef() {
        return bomRef;
    }

    public void setBomRef(String bomRef) {
        this.bomRef = bomRef;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPurl() {
        return purl;
    }

    public void setPurl(String purl) {
        this.purl = purl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getEsDirecta() {
        return esDirecta;
    }

    public void setEsDirecta(Boolean esDirecta) {
        this.esDirecta = esDirecta;
    }

    public String getModuloContexto() {
        return moduloContexto;
    }

    public void setModuloContexto(String moduloContexto) {
        this.moduloContexto = moduloContexto;
    }
}


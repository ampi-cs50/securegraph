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
@Table(name = "relacion_dependencia")
public class RelacionDependencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analisis_id", nullable = false)
    private Analisis analisis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "padre_componente_id", nullable = false)
    private Componente padreComponente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hijo_componente_id", nullable = false)
    private Componente hijoComponente;

    @Column(name = "tipo_transitiva", nullable = false)
    private Boolean tipoTransitiva = false;

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

    public Componente getPadreComponente() {
        return padreComponente;
    }

    public void setPadreComponente(Componente padreComponente) {
        this.padreComponente = padreComponente;
    }

    public Componente getHijoComponente() {
        return hijoComponente;
    }

    public void setHijoComponente(Componente hijoComponente) {
        this.hijoComponente = hijoComponente;
    }

    public Boolean getTipoTransitiva() {
        return tipoTransitiva;
    }

    public void setTipoTransitiva(Boolean tipoTransitiva) {
        this.tipoTransitiva = tipoTransitiva;
    }
}


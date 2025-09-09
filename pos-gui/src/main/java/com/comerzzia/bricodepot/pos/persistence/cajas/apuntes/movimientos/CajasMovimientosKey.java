package com.comerzzia.bricodepot.pos.persistence.cajas.apuntes.movimientos;

public class CajasMovimientosKey {
    private String uidActividad;

    private String codconceptoMovOrigen;

    private String codcajaOrigen;

    private String codcajaDestino;

    private String codconceptoMovDestino;

    public String getUidActividad() {
        return uidActividad;
    }

    public void setUidActividad(String uidActividad) {
        this.uidActividad = uidActividad == null ? null : uidActividad.trim();
    }

    public String getCodconceptoMovOrigen() {
        return codconceptoMovOrigen;
    }

    public void setCodconceptoMovOrigen(String codconceptoMovOrigen) {
        this.codconceptoMovOrigen = codconceptoMovOrigen == null ? null : codconceptoMovOrigen.trim();
    }

    public String getCodcajaOrigen() {
        return codcajaOrigen;
    }

    public void setCodcajaOrigen(String codcajaOrigen) {
        this.codcajaOrigen = codcajaOrigen == null ? null : codcajaOrigen.trim();
    }

    public String getCodcajaDestino() {
        return codcajaDestino;
    }

    public void setCodcajaDestino(String codcajaDestino) {
        this.codcajaDestino = codcajaDestino == null ? null : codcajaDestino.trim();
    }

    public String getCodconceptoMovDestino() {
        return codconceptoMovDestino;
    }

    public void setCodconceptoMovDestino(String codconceptoMovDestino) {
        this.codconceptoMovDestino = codconceptoMovDestino == null ? null : codconceptoMovDestino.trim();
    }
}
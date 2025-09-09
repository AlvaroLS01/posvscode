package com.comerzzia.bricodepot.pos.persistence.cajas.apuntes.movimientos;

public class CajasMovimientos extends CajasMovimientosKey {
    private String signoDestino;

    public String getSignoDestino() {
        return signoDestino;
    }

    public void setSignoDestino(String signoDestino) {
        this.signoDestino = signoDestino == null ? null : signoDestino.trim();
    }
}
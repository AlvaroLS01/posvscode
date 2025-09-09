package com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle;

import java.math.BigDecimal;
import java.util.Date;

public class CajaDet extends CajaDetKey {
    private Date fecha;

    private BigDecimal cargo;

    private BigDecimal abono;

    private String concepto;

    private String documento;

    private String codmedpag;

    private String idDocumento;

    private String codconceptoMov;

    private Long idTipoDocumento;

    private String uidTransaccionDet;

    private String coddivisa;

    private BigDecimal tipoDeCambio;

    private String usuario;

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getCargo() {
        return cargo;
    }

    public void setCargo(BigDecimal cargo) {
        this.cargo = cargo;
    }

    public BigDecimal getAbono() {
        return abono;
    }

    public void setAbono(BigDecimal abono) {
        this.abono = abono;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto == null ? null : concepto.trim();
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento == null ? null : documento.trim();
    }

    public String getCodmedpag() {
        return codmedpag;
    }

    public void setCodmedpag(String codmedpag) {
        this.codmedpag = codmedpag == null ? null : codmedpag.trim();
    }

    public String getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(String idDocumento) {
        this.idDocumento = idDocumento == null ? null : idDocumento.trim();
    }

    public String getCodconceptoMov() {
        return codconceptoMov;
    }

    public void setCodconceptoMov(String codconceptoMov) {
        this.codconceptoMov = codconceptoMov == null ? null : codconceptoMov.trim();
    }

    public Long getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(Long idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getUidTransaccionDet() {
        return uidTransaccionDet;
    }

    public void setUidTransaccionDet(String uidTransaccionDet) {
        this.uidTransaccionDet = uidTransaccionDet == null ? null : uidTransaccionDet.trim();
    }

    public String getCoddivisa() {
        return coddivisa;
    }

    public void setCoddivisa(String coddivisa) {
        this.coddivisa = coddivisa == null ? null : coddivisa.trim();
    }

    public BigDecimal getTipoDeCambio() {
        return tipoDeCambio;
    }

    public void setTipoDeCambio(BigDecimal tipoDeCambio) {
        this.tipoDeCambio = tipoDeCambio;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario == null ? null : usuario.trim();
    }
}
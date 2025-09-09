package com.comerzzia.bricodepot.pos.persistence.cajas.tiendascajas;

import java.util.Date;

public class TiendasCajas {
    private String uidCaja;

    private String uidActividad;

    private String codalm;

    private String codcaja;

    private String uidTpv;

    private String activo;

    private String direccionIp;

    private Long idTipoCaja;

    private Long versionArticulosApl;

    private Date fechaVersionArticulosApl;

    private Long idActVersionArticulosApl;

    private Long idBuzonRemoto;

    private byte[] configuracion;

    public String getUidCaja() {
        return uidCaja;
    }

    public void setUidCaja(String uidCaja) {
        this.uidCaja = uidCaja == null ? null : uidCaja.trim();
    }

    public String getUidActividad() {
        return uidActividad;
    }

    public void setUidActividad(String uidActividad) {
        this.uidActividad = uidActividad == null ? null : uidActividad.trim();
    }

    public String getCodalm() {
        return codalm;
    }

    public void setCodalm(String codalm) {
        this.codalm = codalm == null ? null : codalm.trim();
    }

    public String getCodcaja() {
        return codcaja;
    }

    public void setCodcaja(String codcaja) {
        this.codcaja = codcaja == null ? null : codcaja.trim();
    }

    public String getUidTpv() {
        return uidTpv;
    }

    public void setUidTpv(String uidTpv) {
        this.uidTpv = uidTpv == null ? null : uidTpv.trim();
    }

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo == null ? null : activo.trim();
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp == null ? null : direccionIp.trim();
    }

    public Long getIdTipoCaja() {
        return idTipoCaja;
    }

    public void setIdTipoCaja(Long idTipoCaja) {
        this.idTipoCaja = idTipoCaja;
    }

    public Long getVersionArticulosApl() {
        return versionArticulosApl;
    }

    public void setVersionArticulosApl(Long versionArticulosApl) {
        this.versionArticulosApl = versionArticulosApl;
    }

    public Date getFechaVersionArticulosApl() {
        return fechaVersionArticulosApl;
    }

    public void setFechaVersionArticulosApl(Date fechaVersionArticulosApl) {
        this.fechaVersionArticulosApl = fechaVersionArticulosApl;
    }

    public Long getIdActVersionArticulosApl() {
        return idActVersionArticulosApl;
    }

    public void setIdActVersionArticulosApl(Long idActVersionArticulosApl) {
        this.idActVersionArticulosApl = idActVersionArticulosApl;
    }

    public Long getIdBuzonRemoto() {
        return idBuzonRemoto;
    }

    public void setIdBuzonRemoto(Long idBuzonRemoto) {
        this.idBuzonRemoto = idBuzonRemoto;
    }

    public byte[] getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(byte[] configuracion) {
        this.configuracion = configuracion;
    }
}
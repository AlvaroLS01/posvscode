package com.comerzzia.bricodepot.pos.services.ticket.lineas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.persistence.motivos.Motivo;
import com.comerzzia.pos.persistence.core.impuestos.porcentajes.PorcentajeImpuestoBean;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.sesion.SesionImpuestos;
import com.comerzzia.pos.services.ticket.lineas.LineaTicket;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.format.FormatUtil;

@Component
@Primary
@Scope("prototype")
public class BricodepotLineaTicket extends LineaTicket {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "isAnticipo")
	private Boolean isAnticipo = false;

	private String numPresupuesto;

	@XmlElement(name = "motivo")
	private List<Motivo> motivos;

	private String ivaLinea;

	private boolean conversionAFT;

	private BigDecimal porcentajeIvaConversion;

	private String porcentajeIva;

	public BricodepotLineaTicket() {
		super();
		motivos = new ArrayList<>();
	}

	public List<Motivo> getMotivos() {
		return motivos;
	}

	public void setMotivos(List<Motivo> motivo) {
		this.motivos = motivo;
	}

	public Boolean getIsAnticipo() {
		return isAnticipo;
	}

	public void setIsAnticipo(Boolean isAnticipo) {
		this.isAnticipo = isAnticipo;
	}

	public String obtenerPorcentajeImpuestoArticulo(LineaTicket linea) {
		Sesion sesion = SpringContext.getBean(Sesion.class);
		PorcentajeImpuestoBean porcentaje = sesion.getImpuestos().getPorcentaje(linea.getCabecera().getCliente().getIdTratImpuestos(), linea.getCodImpuesto());
		BigDecimal porcentajeImpuestos = porcentaje.getPorcentaje();
		
		if(((BricodepotLineaTicket) linea).isConversionAFT()) {
			porcentajeImpuestos = ((BricodepotLineaTicket) linea).getPorcentajeIvaConversion();
		}
		
		return FormatUtil.getInstance().formateaNumero(porcentajeImpuestos);
	}

	public String getIvaLinea() {
		if(isConversionAFT()) {
			BigDecimal precioTarifa = BigDecimalUtil.getAntesDePorcentaje(precioTotalSinDto, getPorcentajeIvaConversion());
			return FormatUtil.getInstance().formateaImporte((importeTotalConDto.divide(getCantidad())).subtract(precioTarifa));
		}
		
		return FormatUtil.getInstance().formateaImporte(importeTotalConDto.subtract(importeConDto));
	}
	
	public BigDecimal getIvaLineaAsBigDecimal() {
		if(isConversionAFT()) {
			BigDecimal precioTarifa = BigDecimalUtil.getAntesDePorcentaje(precioTotalSinDto, getPorcentajeIvaConversion());
			return (importeTotalConDto.divide(getCantidad())).subtract(precioTarifa);
		}
		
		return precioTotalConDto.subtract(precioConDto);
	}

	public void setIvaLinea(String ivaLinea) {
		this.ivaLinea = ivaLinea;
	}

	public void nuevoMotivo(Motivo motivo) {
		this.motivos.clear();
		getMotivos().add(motivo);
	}

	public boolean isConversionAFT() {
		return conversionAFT;
	}

	public void setConversionAFT(boolean conversionAFT) {
		this.conversionAFT = conversionAFT;
	}

	public BigDecimal getPorcentajeIvaConversion() {
		return porcentajeIvaConversion;
	}

	public void setPorcentajeIvaConversion(BigDecimal porcentajeIvaConversion) {
		this.porcentajeIvaConversion = porcentajeIvaConversion;
	}

	public String getNumPresupuesto() {
		return numPresupuesto;
	}

	public void setNumPresupuesto(String numPresupuesto) {
		this.numPresupuesto = numPresupuesto;
	}

	public String getPorcentajeIva() {
		porcentajeIva = obtenerPorcentajeImpuestoArticulo(this);
		return porcentajeIva;
	}
	
	public String setPorcentajeIva(String porcentajeIva) {
		this.porcentajeIva = porcentajeIva;
		return this.porcentajeIva;
	}

	@Override
	public void recalcularPreciosImportes() {
		Long idTratamientoImpuestos = getCabecera().getCliente().getIdTratImpuestos();
		Sesion sesion = SpringContext.getBean(Sesion.class);
		SesionImpuestos sesionImpuestos = sesion.getImpuestos();

		// Calculamos precio con impuestos de la tarifa origen
		if (precioTarifaOrigen == null) {
			precioTarifaOrigen = tarifa.getPrecioVenta();
		}

		if (ivaIncluido) {
			precioTotalTarifaOrigen = tarifa.getPrecioTotal();
			precioTarifaOrigen = sesionImpuestos.getPrecioVenta(getCodImpuesto(), idTratamientoImpuestos, precioTotalTarifaOrigen);
		}
		else {
			impuestos = sesionImpuestos.getImpuestos(getCodImpuesto(), idTratamientoImpuestos, precioTarifaOrigen);
			precioTotalTarifaOrigen = BigDecimalUtil.redondear(precioTarifaOrigen.add(impuestos));
		}

		// Por defecto, el precio de venta sin descuentos es igual al precio de la tarifa origen
		precioSinDto = precioTarifaOrigen;
		precioTotalSinDto = precioTotalTarifaOrigen;

		recalcularImporteFinal();
	}
	
	@Override
	public void recalcularImporteFinal() {
		super.recalcularImporteFinal();
		if(isConversionAFT()) {
			precioTarifaOrigen = BigDecimalUtil.getAntesDePorcentaje(precioTotalSinDto, getPorcentajeIvaConversion());
		}
	}

}

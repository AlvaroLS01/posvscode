package com.comerzzia.bricodepot.pos.services.ticket;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.services.core.impuestos.porcentajes.BricodepotPorcentajesImpuestosService;
import com.comerzzia.bricodepot.pos.services.ticket.lineas.BricodepotLineaTicket;
import com.comerzzia.pos.persistence.core.impuestos.porcentajes.PorcentajeImpuestoBean;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.ticket.TicketVentaAbono;
import com.comerzzia.pos.services.ticket.cabecera.ComparadorSubtotalesIvaTicketPorcentaje;
import com.comerzzia.pos.services.ticket.cabecera.ISubtotalIvaTicket;
import com.comerzzia.pos.services.ticket.cabecera.SubtotalIvaTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicket;
import com.comerzzia.pos.services.ticket.pagos.PagoTicket;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.format.FormatUtil;

@Component
@Primary
@XmlRootElement(name = "ticket")
@Scope("prototype")
@SuppressWarnings({ "unchecked", "deprecation" })
public class BricodepotTicketVentaAbono extends TicketVentaAbono {

	private Logger log = Logger.getLogger(BricodepotTicketVentaAbono.class);
	
	public static final String CODIGO_PAGO_ECOMMERCE = "0402";
	
	public List<BricodepotLineaTicket> getLineasAgrupadas() {
		List<BricodepotLineaTicket> lineasPersonalizadas = new ArrayList<>();
		try {

			List<Integer> lineasYaAgrupadas = new ArrayList<>();
			List<LineaTicket> nuevasLineas = new ArrayList<>();
			
			List<LineaTicket> copiaLineasAux = new ArrayList<>(lineas);
			
			
			for (LineaTicket linea : lineas) {
				if (!lineasYaAgrupadas.contains(linea.getIdLinea())) {
					lineasYaAgrupadas.add(linea.getIdLinea());

					if (copiaLineasAux.contains(linea)) {
						copiaLineasAux.remove(linea);
					}

					BigDecimal cantidadTotal = linea.getCantidad();
					BigDecimal importeTotalPromociones = linea.getImporteTotalPromociones();
					BigDecimal importeTotalConDto = linea.getImporteTotalConDto();
					BigDecimal importeConDto = linea.getImporteConDto();

					Iterator<LineaTicket> itAux = copiaLineasAux.iterator();

					while (itAux.hasNext()) {
						LineaTicket lineaAux = itAux.next();

						if (tienenMismasCondicionesVenta(linea, lineaAux)) {
							itAux.remove();
							lineasYaAgrupadas.add(lineaAux.getIdLinea());
							copiaLineasAux.remove(lineaAux);

							cantidadTotal = cantidadTotal.add(lineaAux.getCantidad());
							importeTotalPromociones = importeTotalPromociones.add(lineaAux.getImporteTotalPromociones());
							importeTotalConDto = importeTotalConDto.add(lineaAux.getImporteTotalConDto());
							importeConDto  = importeConDto.add(lineaAux.getImporteConDto());
						}
					}
					

					if (!BigDecimalUtil.isIgualACero(cantidadTotal)) {
						linea.setImporteTotalConDto(importeTotalConDto);
						linea.setCantidad(cantidadTotal);
						linea.setImporteTotalPromociones(importeTotalPromociones);
						linea.setImporteConDto(importeConDto);
						
						//Operación para extraer el porcentaje de iva de cada artículo.
						List<ISubtotalIvaTicket> porcentajes = this.getCabecera().getSubtotalesIva();
						for (ISubtotalIvaTicket imp : porcentajes) {
							if(imp.getCodImpuesto().equals(linea.getCodImpuesto())) {
								 BigDecimal lineaIva = BigDecimalUtil.redondear(BigDecimalUtil.porcentaje(linea.getPrecioConDto().multiply(cantidadTotal), imp.getPorcentaje()), 4);
								if(linea instanceof BricodepotLineaTicket) {
									((BricodepotLineaTicket)linea).setIvaLinea(FormatUtil.getInstance().formateaNumero(lineaIva, 4));									
								} else {
									log.error("getLineasAgrupadas() - No se ha podido recuperar el IVA de la línea correctamente. ");
								}

							}
						}
						
						nuevasLineas.add(linea);
						lineasPersonalizadas.add((BricodepotLineaTicket) linea);
					}
				}
			}
			lineas = nuevasLineas;
			
		}
		catch (Exception e) {
			log.error("agruparLineas() - Ha habido un error al agrupar lineas: " + e.getMessage(), e);
		}
		return lineasPersonalizadas;
	}

	private boolean tienenMismasCondicionesVenta(LineaTicket linea, LineaTicket lineaAux) {
		if (!linea.getCodArticulo().equals(lineaAux.getCodArticulo())) {
			return false;
		}
		if (!BigDecimalUtil.isIgual(linea.getPrecioTotalConDto(), lineaAux.getPrecioTotalConDto())) {
			return false;
		}
		if(linea.getCantidad().signum() != lineaAux.getCantidad().signum()) {
			return false;
		}
		return true;
	}
	
	public String getNumPedido() {
		
		for (LineaTicket linea : lineas) {
			String numPresupuesto = ((BricodepotLineaTicket)linea).getNumPresupuesto();
			if(StringUtils.isNotBlank(numPresupuesto)){
				return numPresupuesto;
			}
		}
//		((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).getNumPresupuesto();
		
		for (PagoTicket pago : pagos) {
			if(pago.getCodMedioPago().equals(CODIGO_PAGO_ECOMMERCE) && pago.getExtendedData().containsKey("documento")) {
				return (String) pago.getExtendedData().get("documento");
			}
		}			
		
		return null;
	}
	
	public String getNumTarjetaRegalo() {
		String numTarjetaRegalo = "";
		for (PagoTicket pago : pagos) {
				if(pago.getGiftcards()!=null && !pago.getGiftcards().isEmpty()) {
				numTarjetaRegalo = numTarjetaRegalo + " " + pago.getGiftcards().get(0).getNumTarjetaRegalo();
			}
		}
		numTarjetaRegalo = numTarjetaRegalo.trim();
		numTarjetaRegalo.replace(" ", "/");
        if (numTarjetaRegalo.endsWith("/")) {
        	numTarjetaRegalo = numTarjetaRegalo.substring(0, numTarjetaRegalo.length() - 1);
        }	
		return StringUtils.isNotBlank(numTarjetaRegalo) ? numTarjetaRegalo : "";
	}
	
	@Override
	public void recalcularSubtotalesIva() {
        // Construimos mapa con subtotales recorriendo todas las líneas del ticket
        Map<String, SubtotalIvaTicket> subtotales = new HashMap<>();
        Map<BigDecimal, SubtotalIvaTicket> subtotalesFlexpoint = new HashMap<>();
        Sesion sesion = SpringContext.getBean(Sesion.class);
        for (LineaTicket linea : lineas) {
         	linea.recalcularImporteFinal();
            String codImpuesto = linea.getArticulo().getCodImpuesto();
            
             SubtotalIvaTicket subtotal = getSubtotalSegunPorcentaje(subtotales, subtotalesFlexpoint, linea, codImpuesto);
            
            if (subtotal == null){
                subtotal = crearSubTotalIvaTicket();
                PorcentajeImpuestoBean porcentajeImpuesto = getPorcentajeImpuestoSegunPorcentaje(sesion, linea, codImpuesto);
                subtotal.setPorcentajeImpuestoBean(porcentajeImpuesto);
                addSubtotales(subtotales, subtotalesFlexpoint, linea, codImpuesto, subtotal);
                
            }
            subtotal.addLinea(linea);
        }
               
        recalcularSubtotalesTicket(subtotales, subtotalesFlexpoint);
        
        ComparadorSubtotalesIvaTicketPorcentaje comparador = new ComparadorSubtotalesIvaTicketPorcentaje();
        Collections.sort(getCabecera().getSubtotalesIva(),comparador);

	}

	private void recalcularSubtotalesTicket(Map<String, SubtotalIvaTicket> subtotales, Map<BigDecimal, SubtotalIvaTicket> subtotalesFlexpoint) {
		getCabecera().getSubtotalesIva().clear();
		for (SubtotalIvaTicket subtotal : subtotales.values()) {
			subtotal.recalcular();
			getCabecera().getSubtotalesIva().add(subtotal);
		}

		for (SubtotalIvaTicket subtotal : subtotalesFlexpoint.values()) {
			subtotal.recalcular();
			getCabecera().getSubtotalesIva().add(subtotal);
		}
	}

	private void addSubtotales(Map<String, SubtotalIvaTicket> subtotales, Map<BigDecimal, SubtotalIvaTicket> subtotalesFlexpoint, LineaTicket linea, String codImpuesto, SubtotalIvaTicket subtotal) {
		if (((BricodepotLineaTicket) linea).isConversionAFT()) {
			subtotalesFlexpoint.put(((BricodepotLineaTicket)linea).getPorcentajeIvaConversion(), subtotal);
		}else {
			subtotales.put(codImpuesto, subtotal);
		}
	}

	private PorcentajeImpuestoBean getPorcentajeImpuestoSegunPorcentaje(Sesion sesion, LineaTicket linea, String codImpuesto) {
		PorcentajeImpuestoBean porcentajeImpuesto = null;
		if (((BricodepotLineaTicket) linea).isConversionAFT()) {
			Long idTratImpuestos = sesion.getAplicacion().getTienda().getCliente().getIdTratImpuestos();
			BigDecimal porcentajeIvaConversion = ((BricodepotLineaTicket) linea).getPorcentajeIvaConversion();
			
			BricodepotPorcentajesImpuestosService impuestosService = SpringContext.getBean(BricodepotPorcentajesImpuestosService.class);
			porcentajeImpuesto = impuestosService.consultarPorcentajesImpuestosActualFlex(idTratImpuestos, porcentajeIvaConversion);
			
			if(porcentajeImpuesto == null) {
				log.warn("recalcularSubtotalesIva() - No se ha encontrado el impuesto relativo a este porcentaje.");
				porcentajeImpuesto = sesion.getImpuestos().getPorcentaje(getCliente().getIdTratImpuestos(), codImpuesto);
			}
		}
		else {
			porcentajeImpuesto = sesion.getImpuestos().getPorcentaje(getCliente().getIdTratImpuestos(), codImpuesto);                	
		}
		return porcentajeImpuesto;
	}

	private SubtotalIvaTicket getSubtotalSegunPorcentaje(Map<String, SubtotalIvaTicket> subtotales, Map<BigDecimal, SubtotalIvaTicket> subtotalesFlexpoint, LineaTicket linea, String codImpuesto) {
		SubtotalIvaTicket subtotal = null;
		if (((BricodepotLineaTicket) linea).isConversionAFT()) {
			subtotal = subtotalesFlexpoint.get(((BricodepotLineaTicket)linea).getPorcentajeIvaConversion());
		}
		else {
			subtotal = obtenerPorcentajeFlexInsertado(subtotalesFlexpoint, linea);
			if(subtotal == null) {
				subtotal = subtotales.get(codImpuesto);
			}
		}
		return subtotal;
	}

	private SubtotalIvaTicket obtenerPorcentajeFlexInsertado(Map<BigDecimal, SubtotalIvaTicket> subtotalesFlexpoint, LineaTicket linea) {
		SubtotalIvaTicket subFlex = subtotalesFlexpoint.get(FormatUtil.getInstance().desformateaImporte(((BricodepotLineaTicket)linea).getPorcentajeIva()));
		if(subFlex != null) {
			return subFlex;
		}
		return null;
	}
}


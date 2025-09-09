package com.comerzzia.bricodepot.pos.gui.ventas.devoluciones.articulos;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;

import com.comerzzia.bricodepot.pos.gui.motivos.MotivoController;
import com.comerzzia.bricodepot.pos.gui.motivos.MotivoView;
import com.comerzzia.bricodepot.pos.persistence.motivos.Motivo;
import com.comerzzia.bricodepot.pos.services.ticket.lineas.BricodepotLineaTicket;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.gui.ventas.devoluciones.articulos.DevolucionArticulosController;
import com.comerzzia.pos.gui.ventas.devoluciones.articulos.LineaProvisionalDevolucion;
import com.comerzzia.pos.persistence.giftcard.GiftCardBean;
import com.comerzzia.pos.services.giftcard.GiftCardService;
import com.comerzzia.pos.services.ticket.lineas.LineaTicketAbstract;
import com.comerzzia.pos.services.ticket.lineas.LineaTicketException;
import com.comerzzia.pos.services.ticket.tarjetaRegalo.TarjetaRegaloException;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.i18n.I18N;

@Controller
@Primary
public class BricodepotDevolucionArticulosController extends DevolucionArticulosController {

	private static final Logger log = Logger.getLogger(BricodepotDevolucionArticulosController.class.getName());

	@Autowired
	private GiftCardService giftCardService;

	private BricodepotLineaTicket agnadeMotivoDevolucion(LineaTicketAbstract lineaDevolucion) {
		log.debug("agnadeMotivoDevolucion() - Añadiendo motivo de devolucion a la línea");
		BricodepotLineaTicket linea = (BricodepotLineaTicket) lineaDevolucion;
		
		HashMap<String, Object> datosVentana = new HashMap<String, Object>();
		datosVentana.put(MotivoController.PARAMETRO_LINEA, linea);
		datosVentana.put(MotivoController.PARAMETRO_TIPO_MOTIVO, MotivoController.CODIGO_TIPO_DEVOLUCION);
		getApplication().getMainView().showModalCentered(MotivoView.class, datosVentana, getStage());
		Motivo motivo = (Motivo) datosVentana.get(MotivoController.PARAMETRO_MOTIVO);
		if (motivo != null) {
			linea.getMotivos().add(motivo);
		}
		return linea;
	}

	/**
	 * Añade las líneas seleccionadas al ticket de devolución.
	 */
	protected void facturarLineasADevolver() {

		for (LineaProvisionalDevolucion lineaProvisional : lineasProvisionales) {
			LineaTicketAbstract linea = lineaProvisional.getLineaOriginal();
			// Volvemos a poner el devolver original a 0 porque hemos ido actualizando para
			// actualizar la interfaz
			linea.setCantidadADevolver(lineaProvisional.getCantADevolverOriginal());

			try {
				if (BigDecimalUtil.isMayor(lineaProvisional.getCantADevolver().add(linea.getCantidadADevolver()), linea.getCantidad())) {
					lineaProvisional.setCantADevolver(linea.getCantidad().subtract(linea.getCantidadADevolver()));
				}

				BricodepotLineaTicket lineaDevolucion = null;

				if (BigDecimalUtil.isMayorACero(lineaProvisional.getCantADevolver())) {
					if (ticketManager.comprobarTarjetaRegalo(linea.getCodArticulo())) {
						lineaDevolucion = (BricodepotLineaTicket) ticketManager.nuevaLineaArticulo(linea.getCodArticulo(), linea.getDesglose1(), linea.getDesglose2(),
						        lineaProvisional.getCantADevolver(), linea.getIdLinea());
						String numTarjeta = ticketManager.getTicketOrigen().getCabecera().getTarjetaRegalo().getNumTarjetaRegalo();

						GiftCardBean tarjeta = giftCardService.getGiftCard(numTarjeta);

						// Si encuentra la tarjeta.
						if (tarjeta != null) {
							if (numTarjeta.equals(ticketManager.getTicketOrigen().getCabecera().getTarjetaRegalo().getNumTarjetaRegalo())) {
								ticketManager.getTicket().getCabecera().agnadirTarjetaRegalo(tarjeta);
								ticketManager.getTicket().getCabecera().getTarjetaRegalo().setImporteRecarga(ticketManager.getTicket().getTotales().getTotalAPagar());
								ticketManager.setEsDevolucionTarjetaRegalo(true);
							}
							else {
								log.warn("facturarLineasADevolver() - " + I18N.getTexto("El número de tarjeta no coincide con el de la operación original"));
								throw new TarjetaRegaloException(I18N.getTexto("El número de tarjeta no coincide con el de la operación original"));
							}
						}
						else {
							log.error("facturarLineasADevolver() - " + I18N.getTexto("El número de tarjeta no es válido"));
							throw new TarjetaRegaloException(I18N.getTexto("El número de tarjeta no es válido"));
						}
					}
					else {
						lineaDevolucion = (BricodepotLineaTicket) ticketManager.nuevaLineaArticulo(linea.getCodArticulo(), linea.getDesglose1(), linea.getDesglose2(),
						        lineaProvisional.getCantADevolver(), linea.getIdLinea());
					}
					
					if (lineaDevolucion instanceof BricodepotLineaTicket) {
							lineaDevolucion = agnadeMotivoDevolucion(lineaDevolucion);
					}
					
					lineaDevolucion.setAdmitePromociones(false);
					asignarNumerosSerie(lineaDevolucion);
				}
			}
			catch (LineaTicketException e) {
				log.error("facturarLineasADevolver() -Error facturando la línea a devolver: " + e.getMessage(), e);
				VentanaDialogoComponent.crearVentanaError(this.getStage(),
				        I18N.getTexto("La lÃ­nea {0} no se ha podido insertar por el siguiente motivo: ", linea.getIdLinea()) + System.lineSeparator() + e.getMessage(), e);
			}
			catch (TarjetaRegaloException e) {
				log.error("facturarLineasADevolver() - Error en el proceso de tarjeta regalo: " + e.getMessage(), e);
				VentanaDialogoComponent.crearVentanaError(this.getStage(), e.getMessage(), e);
			}
			catch (Exception e) {
				log.error("facturarLineasADevolver() - Ha habido un error al procesar la línea: " + e.getMessage(), e);
				VentanaDialogoComponent.crearVentanaError(this.getStage(), e.getMessage(), e);
			}
		}
	}

}

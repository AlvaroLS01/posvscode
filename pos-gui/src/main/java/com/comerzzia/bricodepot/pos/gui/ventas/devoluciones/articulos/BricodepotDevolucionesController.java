package com.comerzzia.bricodepot.pos.gui.ventas.devoluciones.articulos;

import com.comerzzia.bricodepot.pos.gui.ventas.articulos.autorizaracciones.AutorizarAccionesController;
import com.comerzzia.bricodepot.pos.gui.ventas.articulos.autorizaracciones.AutorizarAccionesView;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.BricodepotTicketManager;
import com.comerzzia.core.util.fechas.Fecha;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.gui.ventas.devoluciones.DevolucionesController;
import com.comerzzia.pos.gui.ventas.devoluciones.IntroduccionArticulosView;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.util.i18n.I18N;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class BricodepotDevolucionesController extends DevolucionesController {

	private static final Logger log = Logger.getLogger(BricodepotDevolucionesController.class.getName());
	protected final String MAX_DIAS_PERMITIDOS_DEVOLUCION = "TPV.MAX_DIAS_PERMITIDOS_DEVOLUCION";

	public static final String ID_DOCUMENTO_FLEXPOINT = "ID_DOCUMENTO_FLEXPOINT";

	@Autowired
	protected VariablesServices servicioVariables;

	@Override
	protected void recuperarTicketDevolucionSucceeded(boolean encontrado) {

		/* [BRICO-78] - Devoluciones de tickets de Flexpoint */

		if (!encontrado) {
			if (VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("No se ha encontrado el documento origen en central.¿Desea continuar con la devolución?"), this.getStage())) {
				getDatos().put(ID_DOCUMENTO_FLEXPOINT, tfOperacion.getText());
				getApplication().getMainView().showModalCentered(AutorizarAccionesView.class, getDatos(), getStage());
			}

			if (getDatos().get(AutorizarAccionesController.sDocumento) != null) {
				try {
					((BricodepotTicketManager) ticketManager).crearTicketOrigenFlexpoint(getDatos().get(AutorizarAccionesController.sDocumento).toString(),tfCodDoc.getText());
					((BricodepotTicketManager) ticketManager).setEsDevolucionFlexpoint(Boolean.TRUE);
					encontrado = true;
				}
				catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}

		if (encontrado) {
			boolean esMismoTratamientoFiscal = ticketManager.comprobarTratamientoFiscalDev();
			if (!esMismoTratamientoFiscal) {
				try {
					ticketManager.eliminarTicketCompleto();
				}
				catch (Exception e) {
					log.error("recuperarTicketDevolucionSucceeded() - Ha habido un error al eliminar los tickets: " + e.getMessage(), e);
				}

				lbMensajeError.setText(I18N.getTexto("El ticket fue realizando en una tienda con un tratamiento fiscal diferente al de esta tienda. No se puede realizar esta devolución."));
			}
			else {
				if (!((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
					Integer diasPermitidosDevolucion = servicioVariables.getVariableAsInteger(MAX_DIAS_PERMITIDOS_DEVOLUCION);
					Fecha fechaOrigen = new Fecha(ticketManager.getTicketOrigen().getCabecera().getFecha());
					Fecha fechaHoy = new Fecha();
					boolean cumple = fechaHoy.diferenciaDias(fechaOrigen) > diasPermitidosDevolucion;
					getDatos().put("diasSuperados", cumple);
				}
				try {
					getDatos().put(FacturacionArticulosController.TICKET_KEY, ticketManager);
					getView().changeSubView(IntroduccionArticulosView.class, datos);
				}
				catch (InitializeGuiException e) {
					if (e.isMostrarError()) {
						log.error("accionCambiarArticulo() - Error abriendo ventana", e);
						VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("Error cargando pantalla. Para mas información consulte el log."), e);
					}
				}
			}	

			if (!((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
				boolean recoveredOnline = ticketManager.getTicket().getCabecera().getDatosDocOrigen().isRecoveredOnline();
				if (!recoveredOnline) {
					VentanaDialogoComponent.crearVentanaAviso(
					        I18N.getTexto("No se han podido recuperar las líneas devueltas desde la central. Por favor, compruebe el ticket impreso para ver las líneas ya devueltas."), getStage());
				}
			}
		}
		else {
			lbMensajeError.setText(I18N.getTexto("No se ha encontrado ningún ticket con esos datos"));
		}
	}

}

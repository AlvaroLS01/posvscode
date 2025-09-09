package com.comerzzia.bricodepot.pos.gui.ventas.cajas.recuentos;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.gui.ventas.cajas.cajaventa.CajaVentaController;
import com.comerzzia.pos.core.dispositivos.Dispositivos;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.gui.ventas.cajas.recuentos.RecuentoCajaController;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.mediospagos.MediosPagosService;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

@Primary
@Component
public class BricodepotRecuentoCajaController extends RecuentoCajaController {

	@Autowired
	private Sesion sesion;

	private static final Logger log = Logger.getLogger(BricodepotRecuentoCajaController.class.getName());

	public void accionAnotarRecuento(BigDecimal importe, String cantidad) {
		log.debug("accionAnotarRecuento() - Medio de pago: " + medioPagoSeleccionado + " // Cantidad: " + cantidad
				+ " // Importe: " + importe);

		if (medioPagoSeleccionado == null) {
			VentanaDialogoComponent.crearVentanaError(I18N.getTexto("No hay ninguna forma de pago seleccionada."),
					getStage());
			return;
		}

		cajaSesion.nuevaLineaRecuento(medioPagoSeleccionado.getCodMedioPago(), importe, Integer.parseInt(cantidad));
		refrescarDatosPantalla();
		tfImporte.requestFocus();

	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		log.debug("initializeForm() - Inicializando formulario");
		try {
			cajaSesion = sesion.getSesionCaja();
			cajaSesion.actualizarRecuentoCaja();
			refrescarDatosPantalla();

			Dispositivos.abrirCajon();

			panelMedioPago.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
					if (t.booleanValue() == false && t1.booleanValue() == true) {
						medioPagoSeleccionado = MediosPagosService.medioPagoDefecto;
						lbMedioPago.setText(medioPagoSeleccionado.getDesMedioPago());
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								tfImporte.requestFocus();
							}
						});
					}
				}
			});

			// Establecemos el medio de pago por defecto
			medioPagoSeleccionado = MediosPagosService.medioPagoDefecto;
			lbMedioPago.setText(medioPagoSeleccionado.getDesMedioPago());
		} catch (CajasServiceException e) {
			log.error("initializeForm() - Error de caja: " + e.getMessageI18N());
			throw new InitializeGuiException(e.getMessageI18N(), e);
		} catch (Exception e) {
			log.error("initializeForm() - Error inesperado inicializando formulario. ", e);
			throw new InitializeGuiException(e);
		}
	}

	@FXML
	public void aceptar() {
		log.debug("aceptar() ");
		try {
			cajaSesion.salvarRecuento();
			cajaSesion.actualizarRecuentoCaja();
			BigDecimal importe = cajaSesion.getCajaAbierta().getTotalRecuento();
			getDatos().put(CajaVentaController.RECUENTO, importe);
			getStage().close();

		} catch (CajasServiceException e) {
			VentanaDialogoComponent.crearVentanaError(getStage(), e.getMessageI18N(), e);
		}
	}

}

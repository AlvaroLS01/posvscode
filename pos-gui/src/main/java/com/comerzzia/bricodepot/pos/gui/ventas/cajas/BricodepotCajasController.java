package com.comerzzia.bricodepot.pos.gui.ventas.cajas;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.cashmanagement.client.model.Movimiento;
import com.comerzzia.bricodepot.pos.gui.ventas.cajas.apuntes.BricodepotInsertarApunteView;
import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.services.core.sesion.BricodepotSesionCaja;
import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.bricodepot.pos.util.MovimientoJasperBean;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.permisos.exception.SinPermisosException;
import com.comerzzia.pos.gui.ventas.cajas.CajasController;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.devices.DeviceException;
import com.comerzzia.pos.servicios.impresion.ServicioImpresion;
import com.comerzzia.pos.util.config.AppConfig;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.i18n.I18N;

@Primary
@Component
public class BricodepotCajasController extends CajasController {

	private static final Logger log = Logger.getLogger(BricodepotCajasController.class.getName());

	@Autowired
	protected BricodepotSesionCaja sesionCaja;
	@Autowired
	protected Sesion sesion;
	@Autowired
	protected BricodepotCajasService cajasService;
	public static final String CONCEPTO_CAJA = "concepto";
	public static final String SALIDA_CAJA = "salida";
	public static final String PERMISO_APUNTES_VISIBLES = "APUNTES VISIBLES";

	@Override
	public void initializeComponents() throws InitializeGuiException {
		super.initializeComponents();
	}

	public void salidaDeCaja() {
		log.debug("salidaDeCaja() - Abriendo pantalla de salida de caja");
		if (comprobarCierreCajaDiarioObligatorio()) {
			int registros = movimientos.size();
			getDatos().put(CONCEPTO_CAJA, CajasConstants.PARAM_CONCEPTO_84);
			getApplication().getMainView().showModalCentered(BricodepotInsertarApunteView.class, getDatos(), this.getStage());
			actualizarEstadoCaja();
			refrescarDatosPantalla();
			if (registros != movimientos.size()) {
				tbMovimientos.requestFocus();
				tbMovimientos.getSelectionModel().selectLast();// .select(0);
				int indSeleccionado = tbMovimientos.getSelectionModel().getSelectedIndex();
				tbMovimientos.getFocusModel().focus(indSeleccionado);
				tbMovimientos.scrollTo(indSeleccionado);
			}
		}
		else {
			String fechaCaja = FormatUtil.getInstance().formateaFecha(sesion.getSesionCaja().getCajaAbierta().getFechaApertura());
			String fechaActual = FormatUtil.getInstance().formateaFecha(new Date());
			VentanaDialogoComponent.crearVentanaError(
			        I18N.getTexto("No se puede insertar un apunte manual. El día de apertura de la caja {0} no coincide con el del sistema {1}", fechaCaja, fechaActual), getStage());
		}

	}

	public void ingresoDesdeBanco() {
		log.debug("ingresoDesdeBanco() - Abriendo pantalla de ingreso desde banco");
		if (comprobarCierreCajaDiarioObligatorio()) {
			int registros = movimientos.size();
			getDatos().put(CONCEPTO_CAJA, CajasConstants.PARAM_CONCEPTO_82);
			getApplication().getMainView().showModalCentered(BricodepotInsertarApunteView.class, getDatos(), this.getStage());
			actualizarEstadoCaja();
			refrescarDatosPantalla();
			if (registros != movimientos.size()) {
				tbMovimientos.requestFocus();
				tbMovimientos.getSelectionModel().selectLast();// .select(0);
				int indSeleccionado = tbMovimientos.getSelectionModel().getSelectedIndex();
				tbMovimientos.getFocusModel().focus(indSeleccionado);
				tbMovimientos.scrollTo(indSeleccionado);
			}
		}
		else {
			String fechaCaja = FormatUtil.getInstance().formateaFecha(sesion.getSesionCaja().getCajaAbierta().getFechaApertura());
			String fechaActual = FormatUtil.getInstance().formateaFecha(new Date());
			VentanaDialogoComponent.crearVentanaError(
			        I18N.getTexto("No se puede insertar un apunte manual. El día de apertura de la caja {0} no coincide con el del sistema {1}", fechaCaja, fechaActual), getStage());
		}
	}

	public void retiradaABanco() {
		log.debug("retiradaABanco() - Abriendo pantalla de retirada a banco");
		if (comprobarCierreCajaDiarioObligatorio()) {
			int registros = movimientos.size();
			getDatos().put(CONCEPTO_CAJA, CajasConstants.PARAM_CONCEPTO_91);
			getApplication().getMainView().showModalCentered(BricodepotInsertarApunteView.class, getDatos(), this.getStage());
			actualizarEstadoCaja();
			refrescarDatosPantalla();
			if (registros != movimientos.size()) {
				tbMovimientos.requestFocus();
				tbMovimientos.getSelectionModel().selectLast();// .select(0);
				int indSeleccionado = tbMovimientos.getSelectionModel().getSelectedIndex();
				tbMovimientos.getFocusModel().focus(indSeleccionado);
				tbMovimientos.scrollTo(indSeleccionado);
			}
		}
		else {
			String fechaCaja = FormatUtil.getInstance().formateaFecha(sesion.getSesionCaja().getCajaAbierta().getFechaApertura());
			String fechaActual = FormatUtil.getInstance().formateaFecha(new Date());
			VentanaDialogoComponent.crearVentanaError(
			        I18N.getTexto("No se puede insertar un apunte manual. El día de apertura de la caja {0} no coincide con el del sistema {1}", fechaCaja, fechaActual), getStage());
		}
	}

	public void reingresoCambio() {
		log.debug("reingresoCambio() - Abriendo pantalla de reingreso de cambio");

		if (comprobarCierreCajaDiarioObligatorio()) {
			int registros = movimientos.size();
			getDatos().put(CONCEPTO_CAJA, CajasConstants.PARAM_CONCEPTO_83);
			abrirCajon();
			getApplication().getMainView().showModalCentered(BricodepotInsertarApunteView.class, getDatos(), this.getStage());
			actualizarEstadoCaja();
			refrescarDatosPantalla();
			if (registros != movimientos.size()) {
				tbMovimientos.requestFocus();
				tbMovimientos.getSelectionModel().selectLast();
				int indSeleccionado = tbMovimientos.getSelectionModel().getSelectedIndex();
				tbMovimientos.getFocusModel().focus(indSeleccionado);
				tbMovimientos.scrollTo(indSeleccionado);
			}
		}
		else {
			String fechaCaja = FormatUtil.getInstance().formateaFecha(sesion.getSesionCaja().getCajaAbierta().getFechaApertura());
			String fechaActual = FormatUtil.getInstance().formateaFecha(new Date());
			VentanaDialogoComponent.crearVentanaError(
			        I18N.getTexto("No se puede insertar un apunte manual. El día de apertura de la caja {0} no coincide con el del sistema {1}", fechaCaja, fechaActual), getStage());
		}
	}

	public void aportacionCambio() {
		log.debug("aportacionCambio() - Abriendo pantalla de aportacion de cambio");
		if (comprobarCierreCajaDiarioObligatorio()) {
			int registros = movimientos.size();
			abrirCajon();
			getDatos().put(CONCEPTO_CAJA, CajasConstants.PARAM_CONCEPTO_85);
			getApplication().getMainView().showModalCentered(BricodepotInsertarApunteView.class, getDatos(), this.getStage());
			actualizarEstadoCaja();
			refrescarDatosPantalla();
			if (registros != movimientos.size()) {
				tbMovimientos.requestFocus();
				tbMovimientos.getSelectionModel().selectLast();// .select(0);
				int indSeleccionado = tbMovimientos.getSelectionModel().getSelectedIndex();
				tbMovimientos.getFocusModel().focus(indSeleccionado);
				tbMovimientos.scrollTo(indSeleccionado);
			}
		}
		else {
			String fechaCaja = FormatUtil.getInstance().formateaFecha(sesion.getSesionCaja().getCajaAbierta().getFechaApertura());
			String fechaActual = FormatUtil.getInstance().formateaFecha(new Date());
			VentanaDialogoComponent.crearVentanaError(
			        I18N.getTexto("No se puede insertar un apunte manual. El día de apertura de la caja {0} no coincide con el del sistema {1}", fechaCaja, fechaActual), getStage());
		}
	}

	public void abrirPagoProveedor() {
		log.debug("abrirPagoProveedor() - Abriendo pantalla de pagos a proveedores");
		if (comprobarCierreCajaDiarioObligatorio()) {
			int registros = movimientos.size();
			getDatos().put(CONCEPTO_CAJA, CajasConstants.PARAM_CONCEPTO_03);
			abrirCajon();
			getApplication().getMainView().showModalCentered(BricodepotInsertarApunteView.class, getDatos(), this.getStage());
			actualizarEstadoCaja();
			refrescarDatosPantalla();
			if (registros != movimientos.size()) {
				tbMovimientos.requestFocus();
				tbMovimientos.getSelectionModel().selectLast();// .select(0);
				int indSeleccionado = tbMovimientos.getSelectionModel().getSelectedIndex();
				tbMovimientos.getFocusModel().focus(indSeleccionado);
				tbMovimientos.scrollTo(indSeleccionado);
			}
		}
		else {
			String fechaCaja = FormatUtil.getInstance().formateaFecha(sesion.getSesionCaja().getCajaAbierta().getFechaApertura());
			String fechaActual = FormatUtil.getInstance().formateaFecha(new Date());
			VentanaDialogoComponent.crearVentanaError(
			        I18N.getTexto("No se puede insertar un apunte manual. El día de apertura de la caja {0} no coincide con el del sistema {1}", fechaCaja, fechaActual), getStage());
		}
	}

	public void retiradaEfectivo() {
		log.debug("retiradaEfectivo() - Abriendo ventana de retirada de efectivo");
		if (comprobarCierreCajaDiarioObligatorio()) {
			int registros = movimientos.size();
			getDatos().put(CONCEPTO_CAJA, CajasConstants.PARAM_CONCEPTO_02);
			abrirCajon();
			getApplication().getMainView().showModalCentered(BricodepotInsertarApunteView.class, getDatos(), this.getStage());
			actualizarEstadoCaja();
			refrescarDatosPantalla();
			if (registros != movimientos.size()) {
				tbMovimientos.requestFocus();
				tbMovimientos.getSelectionModel().selectLast();// .select(0);
				int indSeleccionado = tbMovimientos.getSelectionModel().getSelectedIndex();
				tbMovimientos.getFocusModel().focus(indSeleccionado);
				tbMovimientos.scrollTo(indSeleccionado);
			}
		}
		else {
			String fechaCaja = FormatUtil.getInstance().formateaFecha(sesion.getSesionCaja().getCajaAbierta().getFechaApertura());
			String fechaActual = FormatUtil.getInstance().formateaFecha(new Date());
			VentanaDialogoComponent.crearVentanaError(
			        I18N.getTexto("No se puede insertar un apunte manual. El día de apertura de la caja {0} no coincide con el del sistema {1}", fechaCaja, fechaActual), getStage());
		}

	}

	public void informeBanco() {
		log.debug("informeBanco() - imprimiendo informe");
		HashMap<String, Object> parametros = new HashMap<String, Object>();

		// Fecha del último movimiento de caja en la caja 90 con código de concepto “91”

		Movimiento movimiento91 = cajasService.getMovimiento91(sesion.getAplicacion().getCodAlmacen());
		if (movimiento91 == null) {
			movimiento91 = new Movimiento();
		}
		Date fechaFiltro = movimiento91.getFecha() == null ? null : movimiento91.getFecha();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		parametros.put("FECHAULTMOV", fechaFiltro == null ? null : sdf.format(fechaFiltro));
		String datosTimezone = System.getProperty("user.timezone");
		TimeZone timezone = TimeZone.getTimeZone(datosTimezone);
		parametros.put("REPORT_TIME_ZONE", timezone);
		parametros.put("net.sf.jasperreports.jdbc.time.zone", datosTimezone);
		List<Movimiento> lista90 = cajasService.getListaMovimientos90(sesion.getAplicacion().getCodAlmacen());
		if (lista90 == null) {
			lista90 = new ArrayList<Movimiento>();
		}
		BigDecimal sumCargo = BigDecimal.ZERO;
		if (!lista90.isEmpty() && lista90 != null) {
			Collections.reverse(lista90);
			List<Movimiento> cajaMovimientoPorFecha = lista90;
			if (fechaFiltro != null) {
				cajaMovimientoPorFecha = lista90.stream().filter(c -> c.getFecha().compareTo(fechaFiltro) > 0).collect(Collectors.toList());
			}
			List<MovimientoJasperBean> listaJasper = new ArrayList<MovimientoJasperBean>();
			for (Movimiento cajaMov : cajaMovimientoPorFecha) {
				MovimientoJasperBean movJasper = new MovimientoJasperBean();

				movJasper.setCargo(FormatUtil.getInstance().formateaNumero(cajaMov.getCargo(), 2));
				movJasper.setDocumento(cajaMov.getDocumento());
				movJasper.setFecha(sdf.format(cajaMov.getFecha()));
				movJasper.setUsuario(cajaMov.getUsuario());

				listaJasper.add(movJasper);
			}

			parametros.put("LISTAMOV", listaJasper);

			for (Movimiento caja : cajaMovimientoPorFecha) {
				sumCargo = sumCargo.add(caja.getCargo());
			}
			parametros.put("SUMCARGO", FormatUtil.getInstance().formateaNumero(sumCargo, 2));
		}

		parametros.put("CODALMACEN", sesion.getAplicacion().getTienda().getCodAlmacen());
		parametros.put("DESALMACEN", sesion.getAplicacion().getTienda().getDesAlmacen());
		parametros.put("USUARIO", sesion.getSesionUsuario().getUsuario().getUsuario());
		Date fechaactual = new Date();
		parametros.put("FECHAACTUAL", sdf.format(fechaactual));

		try {
			ServicioImpresion.imprimir("jasper/cajas/retiradaBanco", parametros);
		}
		catch (DeviceException e) {
			log.error("imprimir() - Error: Ha ocurrido un error al imprimir el informe ", e);
			VentanaDialogoComponent.crearVentanaError("Error: Ha ocurrido un error al imprimir el informe.", getStage());
		}
		catch (Exception ex) {
			log.error("imprimir() - Error: Ha ocurrido un error al imprimir el informe ", ex);
			VentanaDialogoComponent.crearVentanaError("Error: Ha ocurrido un error al imprimir el informe.", getStage());
		}
	}

	@Override
	public void actualizarEstadoCaja() {
		log.debug("actualizarEstadoCaja() - Actualizando estado de la caja");
		ocultarBotonesSegunPermisos();
		if (!cajaSesion.isCajaAbierta()) {
			// Poner etiqueta de estado como Caja CERRADA
			lbCaja.setText(I18N.getTexto("Caja CERRADA"));
			// Desactivar botones de Cierre, Insertar apunte y Recuento (Activar el resto)
			botoneraMenu.setAccionDisabled("abrirCaja", false);
			botoneraMenu.setAccionDisabled("abrirCierreCaja", true);
			botoneraMenu.setAccionDisabled("abrirRecuentoCaja", true);
			botoneraMenu.setAccionDisabled("retiradaEfectivo", true);
			botoneraMenu.setAccionDisabled("reingresoCambio", true);
			botoneraMenu.setAccionDisabled("aportacionCambio", true);
			botoneraMenu.setAccionDisabled("abrirPagoProveedor", true);
			botoneraMenu.setAccionDisabled("ingresoDesdeBanco", true);
			botoneraMenu.setAccionDisabled("retiradaABanco", true);
			botoneraMenu.setAccionDisabled("informeBanco", true);
			botoneraMenu.setAccionDisabled("salidaDeCaja", true);

		}
		else {
			// Poner etiqueta de estado como Caja ABIERTA
			lbCaja.setText(I18N.getTexto("Caja ABIERTA"));
			// Desactivar botón de Apertura (Activar el resto)
			botoneraMenu.setAccionDisabled("abrirCaja", true);
			botoneraMenu.setAccionDisabled("abrirCierreCaja", false);
			botoneraMenu.setAccionDisabled("abrirRecuentoCaja", false);
			botoneraMenu.setAccionDisabled("ingresoDesdeBanco", false);
			botoneraMenu.setAccionDisabled("retiradaABanco", false);
			botoneraMenu.setAccionDisabled("reingresoCambio", false);
			botoneraMenu.setAccionDisabled("aportacionCambio", false);
			botoneraMenu.setAccionDisabled("abrirPagoProveedor", false);
			botoneraMenu.setAccionDisabled("retiradaEfectivo", false);
			botoneraMenu.setAccionDisabled("informeBanco", false);
			botoneraMenu.setAccionDisabled("salidaDeCaja", false);
		}
	}

	public void ocultarBotonesSegunPermisos() {
		log.debug("ocultarBotonesSegunPermisos() - Ocultando botones para usuarios no administradores");
		try {
			super.compruebaPermisos(PERMISO_APUNTES_VISIBLES);
			botoneraMenu.setAccionVisible("ingresoDesdeBanco", true);
			botoneraMenu.setAccionVisible("retiradaABanco", true);
			botoneraMenu.setAccionVisible("abrirPagoProveedor", true);
			botoneraMenu.setAccionVisible("informeBanco", true);
			botoneraMenu.setAccionVisible("salidaDeCaja", false);
		}
		catch (SinPermisosException e) {
			botoneraMenu.setAccionVisible("ingresoDesdeBanco", false);
			botoneraMenu.setAccionVisible("retiradaABanco", false);
			botoneraMenu.setAccionVisible("abrirPagoProveedor", false);
			botoneraMenu.setAccionVisible("informeBanco", false);
			botoneraMenu.setAccionVisible("salidaDeCaja", false);
		}
	}

	@Override
	public void abrirCierreCaja() {

		super.abrirCierreCaja();
		refrescarDatosPantalla();
	}
}

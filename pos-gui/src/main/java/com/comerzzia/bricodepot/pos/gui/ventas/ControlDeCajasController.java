package com.comerzzia.bricodepot.pos.gui.ventas;

import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.comerzzia.bricodepot.pos.gui.ventas.cajas.InsertarApunteBancoView;
import com.comerzzia.bricodepot.pos.gui.ventas.cajas.cajaventa.CajaVentaView;
import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.services.cajas.cajadetalle.CajasDetService;
import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.bricodepot.pos.util.MovimientoJasperBean;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.botonaccion.BotonBotoneraComponent;
import com.comerzzia.pos.core.gui.componentes.botonaccion.normal.BotonBotoneraNormalComponent;
import com.comerzzia.pos.core.gui.componentes.botonera.BotoneraComponent;
import com.comerzzia.pos.core.gui.componentes.botonera.IContenedorBotonera;
import com.comerzzia.pos.core.gui.componentes.botonera.PanelBotoneraBean;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.Controller;
import com.comerzzia.pos.core.gui.exception.CargarPantallaException;
import com.comerzzia.pos.core.gui.tablas.celdas.CellFactoryBuilder;
import com.comerzzia.pos.persistence.cajas.movimientos.CajaMovimientoBean;
import com.comerzzia.pos.services.cajas.Caja;
import com.comerzzia.pos.services.cajas.CajaEstadoException;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.devices.DeviceException;
import com.comerzzia.pos.servicios.impresion.ServicioImpresion;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.i18n.I18N;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

@SuppressWarnings("unused")
@Component
public class ControlDeCajasController extends Controller implements IContenedorBotonera {

	@Override
	public void realizarAccion(BotonBotoneraComponent botonAccionado) throws CajasServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeFocus() {
		// TODO Auto-generated method stub
		
	}
//
//	protected static final Logger log = Logger.getLogger(ControlDeCajasController.class);
//
//	public static final String IMPORTE_RETIRADA = "IMPORTE_RETIRADA";
//
//	public static final String PARAMETRO_CAJA80 = "CAJA80";
//
//	public static final String PARAMETRO_CAJA90 = "CAJA90";
//
//	public static final String PARAMETRO_ACEPTAR = "Aceptar";
//
//	private Caja caja80;
//
//	private Caja caja90;
//
//	private BigDecimal importeCajaVenta;
//
//	protected BotoneraComponent botoneraMenu;
//
//	@Autowired
//	protected BricodepotCajasService cajasService;
//
//	@Autowired
//	protected CajasDetService cajasDetService;
//
//	@Autowired
//	protected Sesion sesion;
//
//	@FXML
//	private Label lbTienda;
//
//	@FXML
//	private TextField tfTotal80, tfTotal90, tfNombreCajero, tfUsuarioCajero;
//
//	@FXML
//	protected AnchorPane panelBotonera;
//
//	protected ObservableList<ControlDeCajasGui> cajas;
//
//	@FXML
//	protected TableView<ControlDeCajasGui> tbCajas;
//
//	@SuppressWarnings("rawtypes")
//	@FXML
//	protected TableColumn tcFecha, tcUsuario, tcTotalCaja, tcCodCaja;
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public void initialize(URL arg0, ResourceBundle arg1) {
//
//		tcCodCaja.setCellFactory(CellFactoryBuilder.createCellRendererCelda("tbCajas", "tcCodCaja", 2, CellFactoryBuilder.ESTILO_ALINEACION_IZQ));
//		tcFecha.setCellFactory(CellFactoryBuilder.createCellRendererCelda("tbCajas", "tcFecha", 2, CellFactoryBuilder.ESTILO_ALINEACION_IZQ));
//		tcUsuario.setCellFactory(CellFactoryBuilder.createCellRendererCelda("tbCajas", "tcUsuario", 2, CellFactoryBuilder.ESTILO_ALINEACION_IZQ));
//		tcTotalCaja.setCellFactory(CellFactoryBuilder.createCellRendererCelda("tbCajas", "tcTotalCaja", 2, CellFactoryBuilder.ESTILO_ALINEACION_IZQ));
//
//		tcCodCaja.setCellValueFactory(new PropertyValueFactory<ControlDeCajasGui, String>("codCaja"));
//		tcFecha.setCellValueFactory(new PropertyValueFactory<ControlDeCajasGui, String>("fecha"));
//		tcUsuario.setCellValueFactory(new PropertyValueFactory<ControlDeCajasGui, String>("usuario"));
//		tcTotalCaja.setCellValueFactory(new PropertyValueFactory<ControlDeCajasGui, String>("totalCaja"));
//	}
//
//	@Override
//	public void initializeComponents() throws InitializeGuiException {
//		log.debug("inicializarComponentes() - Carga de acciones de botonera inferior");
//		try {
//			PanelBotoneraBean panelBotoneraBean = getView().loadBotonera();
//			botoneraMenu = new BotoneraComponent(panelBotoneraBean, panelBotonera.getPrefWidth(),
//					panelBotonera.getPrefHeight(), this, BotonBotoneraNormalComponent.class);
//			panelBotonera.getChildren().add(botoneraMenu);
//		} catch (InitializeGuiException | CargarPantallaException e) {
//			log.error("initializeComponents() - Error al crear botonera: " + e.getMessage(), e);
//		}
//
//	}
//
//	@Override
//	public void initializeFocus() {
//	}
//
//	@Override
//	public void initializeForm() throws InitializeGuiException {
//
//		tfNombreCajero.setText(sesion.getSesionUsuario().getUsuario().getDesusuario());
//		tfUsuarioCajero.setText(sesion.getSesionUsuario().getUsuario().getUsuario());
//
//		refrescarDatosPantalla();
//
//	}
//
//	public void refrescarDatosPantalla() {
//		log.debug("refrescarDatosPantalla()");
//		cajas = FXCollections.observableList(new ArrayList<ControlDeCajasGui>());
//		tbCajas.setItems(cajas);
//		try {
//			caja80 = caja80 == null || caja80.getFechaCierre() != null
//					? cajasService.consultarCajaParticularAbierta(CajasConstants.PARAM_CAJA_80)
//					: caja80;
//			caja90 = caja90 == null || caja90.getFechaCierre() != null
//					? cajasService.consultarCajaParticularAbierta(CajasConstants.PARAM_CAJA_90)
//					: caja90;
//			
//			if(caja80 != null) {
//				cajasService.consultarTotales(caja80);
//				cajasService.consultarMovimientos(caja80);				
//			}
//			if(caja90 != null) {
//				cajasService.consultarTotales(caja90);
//				cajasService.consultarMovimientos(caja90);				
//			}
//		} catch (CajasServiceException e) {
//			log.error("refrescarDatosPantalla() - Error: Se ha producido un error consultando la caja.");
//			lbTienda.setText(I18N.getTexto("TIENDA CERRADA"));
//		} catch (CajaEstadoException e) {
//			log.warn("refrescarDatosPantalla() - Warning: " + e.getMessage());
//			VentanaDialogoComponent.crearVentanaAviso("La tienda está cerrada", getStage());
//			lbTienda.setText(I18N.getTexto("TIENDA CERRADA"));
//		}
//
//		actualizarEstadoCaja();
//
//		try {
//			cajas.clear();
//			List<Caja> cajasAparcadas = cajasService.consultarCajasAparcadas();
//
//			for (Caja caja : cajasAparcadas) {
//				cajasService.consultarTotales(caja);
//				ControlDeCajasGui linea = new ControlDeCajasGui(caja);
//				cajas.add(linea);
//			}
//		} catch (CajasServiceException e) {
//			log.error("refrescarDatosPantalla() - Error: Se ha producido un error consultando las cajas aparcadas");
//		}
//
//	}
//
//	public void actualizarEstadoCaja() {
//		log.debug("actualizarEstadoCaja()");
//		if (caja80 != null && caja80.getFechaCierre() == null && caja90 != null && caja90.getFechaCierre() == null) {
//			lbTienda.setText(I18N.getTexto("TIENDA ABIERTA"));
//
//			BigDecimal importeTotal80 = caja80.getTotal();
////			caja90.recalcularTotales();
////			caja90.recalcularTotalesRecuento();
//			BigDecimal importeTotal90 = caja90.getTotal();
//
//			tfTotal80.setText(FormatUtil.getInstance().formateaNumero(importeTotal80, 2) + " €");
//			tfTotal90.setText(FormatUtil.getInstance().formateaNumero(importeTotal90, 2) + " €");
//			botoneraMenu.setAccionDisabled("abrirCaja", false);
//			botoneraMenu.setAccionDisabled("abrirTienda", true);
//			botoneraMenu.setAccionDisabled("cierre", false);
//			botoneraMenu.setAccionDisabled("recepcion", false);
//			botoneraMenu.setAccionDisabled("retirada", false);
//			botoneraMenu.setAccionDisabled("imprimir", false);
//		} else {
//			lbTienda.setText(I18N.getTexto("TIENDA CERRADA"));
//			tfTotal80.setText("");
//			tfTotal90.setText("");
//			botoneraMenu.setAccionDisabled("abrirCaja", true);
//			botoneraMenu.setAccionDisabled("abrirTienda", false);
//			botoneraMenu.setAccionDisabled("cierre", true);
//			botoneraMenu.setAccionDisabled("recepcion", true);
//			botoneraMenu.setAccionDisabled("retirada", true);
//			botoneraMenu.setAccionDisabled("imprimir", true);
//		}
//	}
//
//	private void abrirTienda() throws CajasServiceException {
//		log.debug("abrirTienda()");
//		if (VentanaDialogoComponent.crearVentanaConfirmacion("¿Desea realizar la apertura de día de la tienda?", getStage())) {
//
//			try {
//				if (caja80 == null || caja80.getFechaCierre() != null) {
//					caja80 = cajasService.crearCajaParticular(new Date(), CajasConstants.PARAM_CAJA_80, "ADMINISTRADOR");
//					Caja caja80Anterior = null;
//					try {
//						caja80Anterior = cajasService.consultarUltimaCajaCerradaParticular(CajasConstants.PARAM_CAJA_80);
//						cajasService.consultarTotales(caja80Anterior);
//					}
//					catch (CajaEstadoException ex) {
//						log.debug("accionApertura() - No se ha encontrado una caja 80 anterior");
//					}
//					if (caja80Anterior != null && caja80Anterior.getTotal().compareTo(BigDecimal.ZERO) > 0) {
//						cajasService.crearMovimientoAperturaParticular(caja80Anterior.getTotal(), new Date(), caja80.getUidDiarioCaja(), "ADMINISTRADOR");
//					}
//				}
//				if (caja90 == null || caja90.getFechaCierre() != null) {
//					caja90 = cajasService.consultarCajaParticularAbierta(PARAMETRO_CAJA90);
//					if(caja90 == null || caja90.getFechaCierre() != null) {
//						abrirCaja90();
//					}
//				}
//			}
//			catch (CajasServiceException e) {
//				log.error("accionApertura() - Error: " + e.getMessage());
//				throw e;
//			}
//			catch (CajaEstadoException e) {
//				log.error("accionApertura() - Error: " + e.getMessage());
//
//			}
//			refrescarDatosPantalla();
//		}
//
//	}
//	
//
//	private void cierre() throws CajasServiceException {
//		if (VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("¿Desea realizar el cierre de la tienda?"), getStage())) {
//			Boolean aparcadas = false;
//			List<Caja> cajasAparcadas = cajasService.consultarCajasAparcadas();
//
//			aparcadas = !cajasAparcadas.isEmpty();
//
//			if (!aparcadas) {
//				caja80.setFechaCierre(new Date());
//				caja80.setLineasRecuento(new ArrayList<>());
//				cajasService.cerrarCaja(caja80, new Date());
//				
//				//TODO
////				caja90.setLineasRecuento(new ArrayList<>());
////				caja90.setFechaCierre(new Date());
////				cajasService.cerrarCaja(caja90, new Date());
//			}
//			else {
//				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No se ha podido realizar el cierre de tienda, todavía hay cajas de venta sin cerrar."), getStage());
//			}
//			refrescarDatosPantalla();
//		}
//	}
//
//	private void abrirCaja() {
//		log.debug("abrirCaja() - Inicio de apertura de caja");
//		
//		getApplication().getMainView().showModalCentered(CajaVentaView.class, getDatos(), this.getStage());
//
//		refrescarDatosPantalla();
//
//	}
//
//	private void recepcion() {
//
//		getDatos().put(PARAMETRO_CAJA80, caja80);
//
//		getApplication().getMainView().showModalCentered(InsertarApunteBancoView.class, getDatos(), this.getStage());
//
//		refrescarDatosPantalla();
//
//	}
//
//	private void imprimir() {
//
//		HashMap<String, Object> parametros = new HashMap<String, Object>();
//
//		// Fecha del último movimiento de caja en la caja 90 con código de concepto “91”
//		List<CajaMovimientoBean> lista = cajasService.consultarMovHistorico(CajasConstants.PARAM_CAJA_90,
//				CajasConstants.PARAM_CONCEPTO_91);
//
//		CajaMovimientoBean ultimoMov91 = new CajaMovimientoBean();
//		if (!lista.isEmpty() && lista != null) {
//			Collections.reverse(lista);
//			ultimoMov91 = lista.get(0);
//		}
//
//		Date fechaFiltro = ultimoMov91.getFecha() == null ? null : ultimoMov91.getFecha();
//		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//		parametros.put("FECHAULTMOV", fechaFiltro == null ? null : sdf.format(fechaFiltro));
//
//		List<CajaMovimientoBean> lista90 = cajasService.consultarMovHistorico(CajasConstants.PARAM_CAJA_90,
//				CajasConstants.PARAM_CAJA_90);
//		BigDecimal sumCargo = BigDecimal.ZERO;
//		if (!lista90.isEmpty() && lista90 != null) {
//			Collections.reverse(lista90);
//			CajaMovimientoBean ultimoMov = lista90.get(0);
//			List<CajaMovimientoBean> cajaMovimientoPorFecha = lista90;
//			if (fechaFiltro != null) {
//				cajaMovimientoPorFecha = lista90.stream().filter(c -> c.getFecha().compareTo(fechaFiltro) > 0)
//						.collect(Collectors.toList());
//			}
//			List<MovimientoJasperBean> listaJasper = new ArrayList<MovimientoJasperBean>();
//			for (CajaMovimientoBean cajaMov : cajaMovimientoPorFecha) {
//				MovimientoJasperBean movJasper = new MovimientoJasperBean();
//
//				movJasper.setCargo(FormatUtil.getInstance().formateaNumero(cajaMov.getCargo(), 2));
//				movJasper.setDocumento(cajaMov.getDocumento());
//				movJasper.setFecha(sdf.format(cajaMov.getFecha()));
//				movJasper.setUsuario(cajaMov.getUsuario());
//
//				listaJasper.add(movJasper);
//			}
//
//			parametros.put("LISTAMOV", listaJasper);
//
//			for (CajaMovimientoBean caja : cajaMovimientoPorFecha) {
//				sumCargo = sumCargo.add(caja.getCargo());
//			}
//			parametros.put("SUMCARGO", FormatUtil.getInstance().formateaNumero(sumCargo, 2));
//		}
//
//		parametros.put("CODALMACEN", sesion.getAplicacion().getTienda().getCodAlmacen());
//		parametros.put("DESALMACEN", sesion.getAplicacion().getTienda().getDesAlmacen());
//		parametros.put("USUARIO", sesion.getSesionUsuario().getUsuario().getUsuario());
//		Date fechaactual = new Date();
//		parametros.put("FECHAACTUAL", sdf.format(fechaactual));
//		try {
//			ServicioImpresion.imprimir("jasper/cajas/retiradaBanco", parametros);
//		} catch (DeviceException e) {
//			log.error("imprimir() - Error: Ha ocurrido un error al imprimir el informe ", e);
//			VentanaDialogoComponent.crearVentanaError("Error: Ha ocurrido un error al imprimir el informe.",
//					getStage());
//		} catch (Exception ex) {
//			log.error("imprimir() - Error: Ha ocurrido un error al imprimir el informe ", ex);
//			VentanaDialogoComponent.crearVentanaError("Error: Ha ocurrido un error al imprimir el informe.",
//					getStage());
//		}
//	}
//
//	private void retirada() throws CajasServiceException, CajaEstadoException {
//		try {
//			if (caja90 != null) {
//				BigDecimal sumCargo = caja90.getTotal();
//
//				getDatos().put(IMPORTE_RETIRADA, sumCargo);
//				getDatos().put(PARAMETRO_CAJA90, caja90);
//				getApplication().getMainView().showModalCentered(InsertarApunteBancoView.class, getDatos(),
//						this.getStage());
//				if(getDatos().containsKey(PARAMETRO_ACEPTAR) && (boolean) getDatos().get(PARAMETRO_ACEPTAR)) {
//					cerrarCaja90();
//					abrirCaja90();
//					getDatos().remove(PARAMETRO_ACEPTAR);
//				}
//				
//			}else {
//				VentanaDialogoComponent.crearVentanaError("Ha ocurrido un error y no se puede acceder a la caja fuerte del banco.", getStage());
//			}
//		} catch (Exception e) {
//			VentanaDialogoComponent.crearVentanaError("Ha ocurrido un error inesperado", getStage());
//		}
//		refrescarDatosPantalla();
//
//	}
//
//	@Override
//	public void realizarAccion(BotonBotoneraComponent arg0) throws CajasServiceException {
//	}
//
//	private void cerrarCaja90() throws CajasServiceException, CajaEstadoException {
//		log.debug("cerrarCaja90() - Cerramos la caja 90");
//		caja90.setLineasRecuento(new ArrayList<>());
//		caja90.setFechaCierre(new Date());
//		cajasService.cerrarCaja(caja90, new Date());
//	}
//	
//	private void abrirCaja90() throws CajasServiceException, CajaEstadoException {
//		log.debug("abrirCaja90() - Abrimos la caja 90");
//		caja90 = cajasService.crearCajaParticular(new Date(), CajasConstants.PARAM_CAJA_90, "ADMINISTRADOR");
//		cajasService.crearMovimientoAperturaParticular(BigDecimal.ZERO, new Date(), caja90.getUidDiarioCaja(), "ADMINISTRADOR");
//	}
//	
//	@FXML
//	public void refrescar() {
//		refrescarDatosPantalla();
//	}
	
}

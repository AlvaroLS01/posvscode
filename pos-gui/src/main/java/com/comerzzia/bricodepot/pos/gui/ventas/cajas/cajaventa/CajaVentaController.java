package com.comerzzia.bricodepot.pos.gui.ventas.cajas.cajaventa;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.gui.ventas.cajas.apertura.recuento.RecuentoCajaAperturaView;
import com.comerzzia.pos.persistence.cajas.conceptos.CajaConceptoBean;
import com.comerzzia.pos.persistence.core.usuarios.UsuarioBean;
import com.comerzzia.pos.services.cajas.Caja;
import com.comerzzia.pos.services.cajas.CajaEstadoException;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.cajas.conceptos.CajaConceptosServiceException;
import com.comerzzia.pos.services.cajas.conceptos.CajaConceptosServices;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.usuarios.ParametrosBuscarUsuariosBean;
import com.comerzzia.pos.services.core.usuarios.UsuariosService;
import com.comerzzia.pos.services.core.usuarios.UsuariosServiceException;
import com.comerzzia.pos.util.format.FormatUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

@Component
public class CajaVentaController extends WindowController {

	protected static final Logger log = Logger.getLogger(CajaVentaController.class);

	public static final String RECUENTO = "RECUENTO";

	protected List<UsuarioBean> usuarios;

	@Autowired
	protected UsuariosService usuariosService;

	@Autowired
	protected BricodepotCajasService cajasService;

	@Autowired
	private Sesion sesion;

	@Autowired
	private CajaConceptosServices cajaConceptosServices;

	@FXML
	protected ComboBox<String> cbUsuario;
	protected ObservableList<String> usuario;

	@FXML
	protected TextField tfImporte;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
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

//	@Override
//	public void initializeComponents() throws InitializeGuiException {
//
//		try {
//			usuarios = usuariosService.consultarUsuarios(new ParametrosBuscarUsuariosBean());
//		} catch (UsuariosServiceException e) {
//			log.error("Error obteniendo los usuarios disponibles.", e);
//		}
//
//		usuario = FXCollections.observableArrayList();
//		usuario.add("");
//
//		for (UsuarioBean usu : usuarios) {
//			usuario.add(usu.getUsuario());
//		}
//		cbUsuario.setItems(usuario);
//		cbUsuario.getSelectionModel().clearSelection();
//		cbUsuario.getSelectionModel().selectFirst();
//
//	}
//
//	@Override
//	public void initializeFocus() {
//	}
//
//	@Override
//	public void initializeForm() throws InitializeGuiException {
//		limpiarFormulario();
//	}
//
//	@FXML
//	private void accionAceptar() throws CajasServiceException, CajaEstadoException, CajaConceptosServiceException {
//		log.debug("accionAceptar() - Inicio del proceso de apertura de caja");
//		try {
//			if (!StringUtils.isBlank(cbUsuario.getValue())) {
//				log.debug("accionAceptar() - Se procede a realizar la apertura de caja para el usuario " + cbUsuario.getValue() + " con importe " + tfImporte.getText());
//				
//				String user = cbUsuario.getValue();
//				BigDecimal importe = new BigDecimal(tfImporte.getText().replaceAll(",", ".").trim());
//				Caja cajaVenta = new Caja();
//
//				Caja caja80 = cajasService.consultarCajaParticularAbierta(CajasConstants.PARAM_CAJA_80);
//				cajasService.consultarTotales(caja80);
//				if (caja80.getTotal().compareTo(importe) >= 0) {
//					cajaVenta = cajasService.crearCajaParticular(new Date(), sesion.getAplicacion().getCodCaja(), user);
//
//					cajasService.crearMovimientoAperturaParticular(importe, new Date(), cajaVenta.getUidDiarioCaja(), user);
//					CajaConceptoBean cajaConcepto = cajaConceptosServices.consultarConcepto(CajasConstants.PARAM_CAJA_80);
//
//					cajasService.crearMovimientoManualParticular(importe, CajasConstants.PARAM_CONCEPTO_APERTURA_CAJA, null, cajaConcepto.getDesConceptoMovimiento(), caja80.getUidDiarioCaja());
//				}
//				else {
//					VentanaDialogoComponent.crearVentanaError("La cantidad indicada es superior al importe total de la caja fuerte de aportaciones", getStage());
//					return;
//				}
//
//				getDatos().put(CajaVentaController.RECUENTO, importe);
//
//				getStage().close();
//			} else {
//				log.debug("accionAceptar() - No se ha seleccionado ningún usuario.");
//				VentanaDialogoComponent.crearVentanaAviso("Seleccione un Usuario", getStage());
//			}
//		} catch (CajaEstadoException e) {
//			VentanaDialogoComponent.crearVentanaError("Ya existe una caja para ese usuario", getStage());
//			limpiarFormulario();
//		}
//	}
//
//	@FXML
//	public void accionContarSaldo() {
//		HashMap<String, Object> datos = new HashMap<String, Object>();
//		getApplication().getMainView().showModal(RecuentoCajaAperturaView.class, datos);
//		BigDecimal saldo = (BigDecimal) datos.get("saldo");
//		if (saldo != null) {
//			tfImporte.setText(FormatUtil.getInstance().formateaImporte(saldo));
//		}
//	}
//
//	public void limpiarFormulario() {
//		tfImporte.setText("0");
//		cbUsuario.getSelectionModel().clearSelection();
//		cbUsuario.getSelectionModel().selectFirst();
//
//	}
//
}

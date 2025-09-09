package com.comerzzia.bricodepot.pos.gui.ventas.cajas.apuntes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.cashmanagement.client.model.Caja90Dto;
import com.comerzzia.bricodepot.pos.gui.ventas.cajas.BricodepotCajasController;
import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.services.cajas.retirada.RetiradaABancoService;
import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.core.util.numeros.BigDecimalUtil;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.gui.ventas.cajas.apuntes.InsertarApunteController;
import com.comerzzia.pos.persistence.cajas.conceptos.CajaConceptoBean;
import com.comerzzia.pos.persistence.cajas.movimientos.CajaMovimientoBean;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.cajas.conceptos.CajaConceptosServiceException;
import com.comerzzia.pos.services.cajas.conceptos.CajaConceptosServices;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.sesion.SesionCaja;
import com.comerzzia.pos.servicios.impresion.ServicioImpresion;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
@Primary
@Component
public class BricodepotInsertarApunteController extends InsertarApunteController{

	protected static final Logger log = Logger.getLogger(BricodepotInsertarApunteController.class.getName());
	
	protected SesionCaja cajaSesion;
	protected CajaConceptoBean concepto;
	
	@Autowired
	private Sesion sesion;
	@Autowired
	protected BricodepotCajasService cajasService;

	@Autowired
	private CajaConceptosServices cajaConceptosServices;
	@Autowired
	protected BricodepotCajasService bricodepotCajasService;
	
	@Autowired
	protected RetiradaABancoService retiradaABancoService;
	@FXML
	protected Label lblTituloVentana;
	@FXML
	protected Button btnAceptar;
	
	public String codConcepto ;
	
	protected String uidDiarioCaja90;
	
	@Override
	public void initializeForm() {
		// TODO Auto-generated method stub
		super.initializeForm();
		cajaSesion = sesion.getSesionCaja();
		codConcepto = (String) getDatos().get(BricodepotCajasController.CONCEPTO_CAJA);
		String tituloVentana = generarTituloPantalla(codConcepto);
		lblTituloVentana.setText(tituloVentana);
		validaConcepto(codConcepto);
		
		tratarRetiradaABanco();
		
	}


	@Override
	public void initializeComponents() {
		// TODO Auto-generated method stub
		super.initializeComponents();
		codConcepto = (String) getDatos().get(BricodepotCajasController.CONCEPTO_CAJA);
		String tituloVentana = generarTituloPantalla(codConcepto);
		lblTituloVentana.setText(tituloVentana);
		validaConcepto(codConcepto);
		tfCodConcepto.setEditable(false);
		tfDescConcepto.setEditable(false);
	}
	public boolean comprobarNegativo(String cantidad) {
		if (cantidad.contains(",")) {
			cantidad = cantidad.replace(",", ".");
		}
		BigDecimal cantidadDecimal = new BigDecimal(cantidad);
        return cantidadDecimal.compareTo(BigDecimal.ZERO) >= 0;
	}
	@Override
	public void accionAceptar(ActionEvent event) {
		log.debug("accionAceptar()");

		if ((this.concepto == null) || (!this.concepto.getCodConceptoMovimiento().equals(tfCodConcepto.getText()))) {
			if (!validaConcepto(tfCodConcepto.getText())) {
				return;
			}
		}

		frApunte.setDocumento(tfDocumento.getText());
		frApunte.setImporte(tfImporte.getText());
		frApunte.setConcepto(concepto);
		frApunte.setDescripcion(tfDescConcepto.getText());

		if (!accionValidarFormulario()) {
			log.debug("datos del apunte invalidos");
			return;
		}
		if (!comprobarNegativo(frApunte.getImporte())) {
			VentanaDialogoComponent.crearVentanaError(I18N.getTexto("No se pueden hacer inserciones negativas."), getStage());
			return;
		}
		
		if (StringUtils.isBlank(tfDocumento.getText())) {
			log.debug("accionAceptar() - Documento no informado");
			lbError.setText(I18N.getTexto("El campo Documento es obligatorio"));
			return;
		}
		String conceptoCaja = tfCodConcepto.getText();
		String cantidad = tfImporte.getText();
		if (cantidad.contains(",")) {
			cantidad = cantidad.replace(",", ".");
		}
		BigDecimal importe = new BigDecimal(cantidad);
		importe = importe.abs();
		String codAlm = sesion.getSesionCaja().getCajaAbierta().getCodAlm();
		CajaConceptoBean concepto = cajaConceptosServices.getConceptoCaja(conceptoCaja);
		String desConcepto = null;
		if (concepto != null) {
			desConcepto = concepto.getDesConceptoMovimiento();
		}
		String documento = tfDocumento.getText();
		try {
			actualizarMovimientos(concepto, importe, documento, codAlm, desConcepto);
		}
		catch (Exception e) {
			log.debug("accionAceptar() - Ha ocurrido una excepcion al generar un movimiento en central para la caja " + CajasConstants.PARAM_CAJA_80 + ": " + e.getMessage());
		}
		getStage().close();
	}

	public void actualizarMovimientos(CajaConceptoBean concepto, BigDecimal importe, String documento, String codalm, String desConcepto) throws Exception {
		log.debug("actualizarMovimientos() - Actualizando movimientos para el concepto: " + concepto.getCodConceptoMovimiento());
		String codConcepto = concepto.getCodConceptoMovimiento();
		CajaMovimientoBean mov = null;

		/* Retirada de efectivo */
		if (CajasConstants.PARAM_CONCEPTO_02.equals(codConcepto)) {
			mov = cajaSesion.crearApunteManual(importe, CajasConstants.PARAM_CONCEPTO_02, frApunte.getDocumento(), desConcepto);
			cajasService.generarDocumentoParaCajaFicticia(CajasConstants.PARAM_CAJA_90, CajasConstants.PARAM_CONCEPTO_90, importe, null, documento,null);
			
		}
		/* Pago a proveedor */
		if (CajasConstants.PARAM_CONCEPTO_03.equals(codConcepto)) {
			mov = cajaSesion.crearApunteManual(importe, CajasConstants.PARAM_CONCEPTO_03, frApunte.getDocumento(), desConcepto);
			
		}
		/* Entrada de cambio */
		if (CajasConstants.PARAM_CONCEPTO_85.equals(codConcepto)) {
			/* Se genera documento con concepto 83 para la caja 80 */
			cajasService.generarDocumentoParaCajaFicticia(CajasConstants.PARAM_CAJA_80, CajasConstants.PARAM_CONCEPTO_83, null, importe, documento,null);
			/* Se crea el movimiento 80 en positivo */
			concepto = cajaConceptosServices.getConceptoCaja(CajasConstants.PARAM_CONCEPTO_80);
			mov = cajaSesion.crearApunteManual(importe, CajasConstants.PARAM_CONCEPTO_85, frApunte.getDocumento(), concepto.getDesConceptoMovimiento());
		}
		/* Ingreso desde banco */
		if (CajasConstants.PARAM_CONCEPTO_82.equals(codConcepto)) {
			/* Se genera documento con concepto 82 para la caja 80 */
			mov = cajasService.generarDocumentoParaCajaFicticia(CajasConstants.PARAM_CAJA_80, CajasConstants.PARAM_CONCEPTO_82, importe, null, documento, null);
			VentanaDialogoComponent.crearVentanaInfo(
			        I18N.getTexto("Se ha generado un documento para la caja 80, con concepto:") + CajasConstants.PARAM_CONCEPTO_82 + I18N.getTexto("importe:") + importe.toString(),
			        this.getStage());
		}
		/* Salida de cambio */
		if (CajasConstants.PARAM_CONCEPTO_83.equals(codConcepto)) {
			mov = cajaSesion.crearApunteManual(importe, CajasConstants.PARAM_CONCEPTO_83, frApunte.getDocumento(), desConcepto);
			/* Se genera documento con concepto 80 para la caja 80 */
			cajasService.generarDocumentoParaCajaFicticia(CajasConstants.PARAM_CAJA_80, CajasConstants.PARAM_CONCEPTO_85, importe, null, documento,null);
		}
		/*Ingreso hacia banco*/
		if (CajasConstants.PARAM_CONCEPTO_84.equals(codConcepto)) {
			log.debug("actualizarMovimientos()- Hay salida a caja");
			mov = cajasService.generarDocumentoParaCajaFicticia(CajasConstants.PARAM_CAJA_80, CajasConstants.PARAM_CONCEPTO_84, null, importe, documento, null);
			VentanaDialogoComponent.crearVentanaInfo(
			        I18N.getTexto("Se ha generado un documento para la caja 80, con concepto:") + CajasConstants.PARAM_CONCEPTO_84 + I18N.getTexto("importe:") + importe.toString(),
			        this.getStage());
		}
		

		/* Retirada a banco */
		if (CajasConstants.PARAM_CONCEPTO_91.equals(codConcepto)) {
			try {
				//Aqui se crea el xml de retiradaAbancoDTO
				mov = cajasService.guardarMovimientos(importe, documento, codalm);
				imprimirMovimiento(mov);
				cajaSesion.actualizarDatosCaja();
				mov = cajasService.generarDocumentoParaCajaFicticia(CajasConstants.PARAM_CAJA_90, CajasConstants.PARAM_CONCEPTO_91, null, importe, documento, uidDiarioCaja90);
				VentanaDialogoComponent.crearVentanaInfo(
				        I18N.getTexto("Se ha generado un documento para la caja 90, con concepto:") + CajasConstants.PARAM_CONCEPTO_91 + I18N.getTexto("importe:") + importe.toString(),
				        this.getStage());
			}
			catch (Exception e) {
				log.error("actualizarMovimientos() - Se ha producido un error al generar los movimientos de retirada a banco : " + e.getMessage());
			}
		}
		
		if (mov != null) {
			try {
				imprimirMovimiento(mov);
			}
			catch (Exception e) {
				log.error("accionAceptar() - Error inesperado imprimiendo- " + e.getCause(), e);
				VentanaDialogoComponent.crearVentanaError(getStage(), e);
			}
		}
	}
	@Override
	public void imprimirMovimiento(CajaMovimientoBean movimiento) throws CajasServiceException {
		try {
			// String printTicket = VelocityServices.getInstance().getPrintCierreCaja(caja);

			// Rellenamos los parametros
			Map<String, Object> contextoTicket = new HashMap<String, Object>();

			// Introducimos los parÃ¡metros que necesita el ticket para imprimir la
			// informacion del cierre
			contextoTicket.put("movimiento", movimiento);
			contextoTicket.put("caja", sesion.getSesionCaja().getCajaAbierta().getCodCaja());
			contextoTicket.put("tienda", sesion.getAplicacion().getTienda().getCodAlmacen());
			contextoTicket.put("empleado", sesion.getSesionUsuario().getUsuario().getDesusuario());

			// Llamamos al servicio de impresiÃ³n
			if (frApunte.getConcepto().getCodConceptoMovimiento().equals("02")) {
				contextoTicket.put("estuche", frApunte.getDocumento());
				contextoTicket.put("empresa", sesion.getAplicacion().getEmpresa());
				contextoTicket.put("domicilioTienda", sesion.getAplicacion().getTienda().getDomicilio());
				ServicioImpresion.imprimir("retirada_caja", contextoTicket);
			} else {
				ServicioImpresion.imprimir(ServicioImpresion.PLANTILLA_MOVIMIENTO_CAJA, contextoTicket);
			}
		} catch (Exception e) {
			log.error("imprimirCierre() - Error imprimiendo  cierre de caja. Error inesperado: " + e.getMessage(), e);
			throw new CajasServiceException("error.service.cajas.print", e);
		}
	}

	public String generarTituloPantalla(String codConcepto) {
		String titulo = null;

		switch (codConcepto) {
			case CajasConstants.PARAM_CONCEPTO_02:
				titulo = I18N.getTexto("Retirada de efectivo");
				break;

			case CajasConstants.PARAM_CONCEPTO_03:
				titulo = I18N.getTexto("Pago a proveedor");
				break;

			case CajasConstants.PARAM_CONCEPTO_82:
				titulo = I18N.getTexto("Ingreso desde banco");
				break;

			case CajasConstants.PARAM_CONCEPTO_83:
				titulo = I18N.getTexto("Salida de cambio");
				break;
				
			case CajasConstants.PARAM_CONCEPTO_84:
				titulo = I18N.getTexto("Correcion ingreso desde banco");
				break;

			case CajasConstants.PARAM_CONCEPTO_85:
				titulo = I18N.getTexto("Aportacion de cambio");
				break;

			case CajasConstants.PARAM_CONCEPTO_91:
				titulo = I18N.getTexto("Retirada a Banco");
				break;

			default:
				titulo = I18N.getTexto("Insertar Apunte");
				break;
		}
		return titulo;
	}

	@Override
	protected boolean validaConcepto(String codigo) {
		frApunte.clearErrorStyle();
		lbError.setText("");

		String codConcepto = codigo.trim();

		if (!codConcepto.isEmpty()) {
			CajaConceptoBean concepto = null;

			try {
				concepto = cajaConceptosServices.consultarConcepto(codConcepto);
				this.concepto = concepto;
				tfCodConcepto.setText(concepto.getCodConceptoMovimiento());
				tfDescConcepto.setText(concepto.getDesConceptoMovimiento());
				evalInOutValue(tfImporte.getText());
			}
			catch (CajaConceptosServiceException ex) {
				log.error("No se encontró el código del concepto de movimiento de caja.");
				lbError.setText(I18N.getTexto("El código de concepto no existe en la base de datos"));
				// tfCodConcepto.requestFocus();
				concepto = null;
				tfCodConcepto.setText("");
				tfDescConcepto.setText("");
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean evalInOutValue(String inOutValue) {
		return true;
	}
	public void tratarRetiradaABanco(){
		log.debug("tratarRetiradaABanco()");
		if (CajasConstants.PARAM_CONCEPTO_91.equals(codConcepto)) {
			try {
				Caja90Dto caja90 = cajasService.getImporteUidDiarioCaja90(sesion.getAplicacion().getCodAlmacen());
				if (caja90 != null) {
					tfImporte.setText(caja90.getImporte().toString());
					tfImporte.setDisable(true);
					uidDiarioCaja90 = caja90.getUidDiarioCaja();
					if (BigDecimalUtil.isMayorACero(caja90.getImporte())) {
						tfDocumento.setDisable(false);
						btnAceptar.setDisable(false);
					}else {
						tfDocumento.setDisable(true);
						btnAceptar.setDisable(true);
					}
				}else {
					VentanaDialogoComponent.crearVentanaError(I18N.getTexto("Ha ocurrido un error consultando el importe de la caja 90 en central, no existen registros."), getStage());
					tfImporte.setDisable(true);
					tfDocumento.setDisable(true);
					btnAceptar.setDisable(true);
				}
			}catch(Exception e) {
				VentanaDialogoComponent.crearVentanaError(I18N.getTexto("Ha ocurrido un error consultando el importe de la caja 90 en central."), getStage());
				log.error("tratarRetiradaABanco() - Ha ocurrido un error consultando el importe de la caja 90 en central : ",e);
				tfImporte.setDisable(true);
				tfDocumento.setDisable(true);
				btnAceptar.setDisable(true);
			}
		}else {
			tfImporte.setDisable(false);
			tfDocumento.setDisable(false);
			btnAceptar.setDisable(false);
		}
	}
	
}

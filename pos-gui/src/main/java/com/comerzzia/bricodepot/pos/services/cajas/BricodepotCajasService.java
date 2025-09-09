package com.comerzzia.bricodepot.pos.services.cajas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.bricodepot.cashmanagement.client.CashManagementApi;
import com.comerzzia.bricodepot.cashmanagement.client.model.Caja90Dto;
import com.comerzzia.bricodepot.cashmanagement.client.model.CajaFicticiaUidDiarioCaja;
import com.comerzzia.bricodepot.cashmanagement.client.model.DetMovRetiradaKey;
import com.comerzzia.bricodepot.cashmanagement.client.model.Movimiento;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.CajaDetMapper;
import com.comerzzia.bricodepot.pos.persistence.cajas.tiendascajas.TiendasCajasMapper;
import com.comerzzia.bricodepot.pos.services.cajas.ficticias.CajaFicticiaDTO;
import com.comerzzia.bricodepot.pos.services.cajas.retirada.RetiradaABancoDTO;
import com.comerzzia.bricodepot.pos.services.core.sesion.BricodepotSesionCaja;
import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.core.servicios.api.ComerzziaApiManager;
import com.comerzzia.core.servicios.sesion.DatosSesionBean;
import com.comerzzia.core.servicios.tipodocumento.ServicioTiposDocumentosImpl;
import com.comerzzia.core.servicios.variables.Variables;
import com.comerzzia.core.servicios.ventas.tickets.TicketException;
import com.comerzzia.core.util.mybatis.session.SqlSession;
import com.comerzzia.instoreengine.master.rest.client.cajas.CajaRequestRest;
import com.comerzzia.instoreengine.master.rest.client.cajas.CajaResponseRest;
import com.comerzzia.instoreengine.master.rest.client.cajas.CajasRest;
import com.comerzzia.model.ventas.cajas.CajaDTO;
import com.comerzzia.model.ventas.cajas.cabecera.CabeceraCaja;
import com.comerzzia.model.ventas.cajas.detalle.DetalleCaja;
import com.comerzzia.pos.core.dispositivos.Dispositivos;
import com.comerzzia.pos.persistence.cajas.CajaBean;
import com.comerzzia.pos.persistence.cajas.CajaExample;
import com.comerzzia.pos.persistence.cajas.CajaKey;
import com.comerzzia.pos.persistence.cajas.conceptos.CajaConceptoBean;
import com.comerzzia.pos.persistence.cajas.movimientos.CajaMovimientoBean;
import com.comerzzia.pos.persistence.cajas.movimientos.CajaMovimientoExample;
import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.persistence.core.usuarios.UsuarioBean;
import com.comerzzia.pos.persistence.mybatis.SessionFactory;
import com.comerzzia.pos.persistence.mybatis.SpringTransactionSqlSession;
import com.comerzzia.pos.persistence.tickets.TicketBean;
import com.comerzzia.pos.services.cajas.Caja;
import com.comerzzia.pos.services.cajas.CajaEstadoException;
import com.comerzzia.pos.services.cajas.CajasService;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.core.contadores.ServicioContadores;
import com.comerzzia.pos.services.core.documentos.Documentos;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.sesion.SesionAplicacion;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.mediospagos.MediosPagosService;
import com.comerzzia.pos.services.ticket.TicketsServiceException;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.config.AppConfig;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;
import com.comerzzia.pos.util.xml.MarshallUtil;
import com.comerzzia.pos.util.xml.MarshallUtilException;
import com.comerzzia.servicios.ventas.cajas.CajaException;
import com.comerzzia.servicios.ventas.cajas.conceptos.ConceptosMovimientosCajaNotFoundException;

@SuppressWarnings("deprecation")
@Component
@Primary
public class BricodepotCajasService extends CajasService {

	@Autowired
	protected CajaDetMapper cajaMovMapper;

	@Autowired
	protected TiendasCajasMapper tiendaCajaMapper;

	@Autowired
	private VariablesServices variablesServices;

	@Autowired
	protected BricodepotCajasService cajasService;

	@Autowired
	private SesionAplicacion sesionAplicacion;

	@Autowired
	protected Sesion sesion;

	@Autowired
	protected BricodepotSesionCaja sesioncaja;
	@Autowired
	protected Documentos documentos;
	@Autowired
	protected ServicioContadores servicioContadores;

	public static final String CAJA_FUERTE = "80";
	public static final String CAJA_PROSEGUR = "90";
	public static final String COD_TIPO_DOCUMENTO_PROSEGUR = "CAJPRO";
	public static final String COD_TIPO_DOCUMENTO_CAJA_FUERTE = "CAJEF";
	public static final String ID_MOV_RETIRADA = "ID_MOV_RETIRADA";
	public static final String TIPO_DOC_MOVIMIENTO_RETIRADA = "MOV_RET";
	private static final String PAIS_CMZ = "CMZ";

	@Autowired
	protected ComerzziaApiManager comerzziaApiManager;

	public Caja pedirYGuardarCajaAbierta(UsuarioBean usuario) throws Exception {
		log.debug("pedirYGuardarCajaAbierta() - Solicitando caja para el usuario : " + usuario.getUsuario());

		try {
			String apiKey = variablesServices.getVariableAsString(Variables.WEBSERVICES_APIKEY);

			CajaDTO cajaDto = pedirCajaEnMaster(usuario);
			log.info("pedirYGuardarCajaAbierta() - Caja solicitada en master para el usuario: " + usuario.getUsuario());
			
			if (cajaDto != null) {
				log.info("pedirYGuardarCajaAbierta() - Caja recibida con éxito, verificando la cabecera...");
				if (cajaDto.getCabecera() != null) {
					log.info("pedirYGuardarCajaAbierta() - Cabecera de la caja válida. Código de caja: " + cajaDto.getCabecera().getCodcaja() + ", UID Diario Caja: " + cajaDto.getCabecera().getUidDiarioCaja());

					CajaRequestRest requestTransferida = new CajaRequestRest();
					requestTransferida.setUidActividad(sesionAplicacion.getUidActividad());
					requestTransferida.setApiKey(apiKey);
					requestTransferida.setCodcaja(sesionAplicacion.getCodCaja());
					requestTransferida.setUidDiarioCaja(cajaDto.getCabecera().getUidDiarioCaja());
					try {
						CajasRest.cajaTransferidaPOS(requestTransferida);
					}
					catch (Exception e) {
						log.error("pedirYGuardarCajaAbierta() - Ha ocurrido un error al marcar la caja como transferida: " + e.getMessage(), e);
						return null;
					}
					Caja caja = null;
					try {
						caja = guardarCaja(cajaDto);
					}
					catch (Exception e) {
						log.error("pedirYGuardarCajaAbierta() - Ha ocurrido un error al salvar la caja. Se llamará al servicio REST para volver a marcar la caja como transferida al master", e);
//						requestTransferida.setCodcaja(cajaDto.getCabecera().getCodcaja());
						requestTransferida.setCodcaja(CajasConstants.PARAM_CAJA_APARCADA);
						CajasRest.cajaTransferidaPOS(requestTransferida);
					}
					return caja;
				}
				else {
					return new Caja();
				}
			}
			else {
				log.debug("pedirYGuardarCajaAbierta() - El servicio REST de la caja máster no ha devuelto una caja.");
				return null;
			}
		}
		catch (Exception e) {
			log.error("pedirYGuardarCajaAbierta() - Ha habido un error al recuperar la caja de la caja máster: " + e.getMessage(), e);
			throw e;
		}
	}

	private Caja guardarCaja(CajaDTO cajaDto) {
		log.debug("guardarCaja() - Guardando caja : " + cajaDto.getCabecera().getCodcaja());
		SqlSession sqlSession = SpringContext.getBean(SpringTransactionSqlSession.class);
		try {
			sqlSession.openSession(SessionFactory.openSession());

			CajaBean cajaBean = new CajaBean();
			cajaBean.setUidActividad(cajaDto.getCabecera().getUidActividad());
			cajaBean.setUidDiarioCaja(cajaDto.getCabecera().getUidDiarioCaja());
			cajaBean.setCodAlmacen(cajaDto.getCabecera().getCodalm());
			cajaBean.setCodCaja(sesionAplicacion.getCodCaja());
			cajaBean.setUsuario(cajaDto.getCabecera().getUsuario());
			cajaBean.setFechaApertura(cajaDto.getCabecera().getFechaApertura());
			cajaBean.setFechaCierre(cajaDto.getCabecera().getFechaCierre());
			cajaBean.setUsuarioCierre(cajaDto.getCabecera().getUsuarioCierre());
			cajaBean.setFechaEnvio(cajaDto.getCabecera().getFechaEnvio());

			CajaKey cajaKey = new CajaKey();
			cajaKey.setUidActividad(sesionAplicacion.getUidActividad());
			cajaKey.setUidDiarioCaja(cajaDto.getCabecera().getUidDiarioCaja());
			if (cajaMapper.selectByPrimaryKey(cajaKey) == null) {
				log.debug("guardarCaja() - Insertando caja con codAlmacen " + cajaDto.getCabecera().getCodalm() + "  y codCaja " + cajaDto.getCabecera().getCodcaja());

				// Dependiendo si la fecha tiene hora o no, llamamos a un método u otro del
				// mapper
				Calendar calendarApertura = Calendar.getInstance();
				calendarApertura.setTime(cajaDto.getCabecera().getFechaApertura());
				if (calendarApertura.get(Calendar.HOUR_OF_DAY) == 0 && calendarApertura.get(Calendar.MINUTE) == 0 && calendarApertura.get(Calendar.SECOND) == 0
				        && calendarApertura.get(Calendar.MILLISECOND) == 0) {
					cajaMapper.insertFechaAperturaDate(cajaBean);
				}
				else {
					cajaMapper.insertFechaAperturaDateTime(cajaBean);
				}
			}
			else {
				CajaMovimientoExample movExample = new CajaMovimientoExample();
				movExample.or().andUidActividadEqualTo(sesionAplicacion.getUidActividad()).andUidDiarioCajaEqualTo(cajaDto.getCabecera().getUidDiarioCaja());
				cajaMovimientoMapper.deleteByExample(movExample);

				cajaMapper.updateByPrimaryKey(cajaBean);
			}

			if (cajaDto.getDetalles() != null) {
				for (DetalleCaja detalle : cajaDto.getDetalles()) {
					CajaMovimientoBean movimiento = new CajaMovimientoBean();
					movimiento.setUidActividad(detalle.getUidActividad());
					movimiento.setUidDiarioCaja(detalle.getUidDiarioCaja());
					movimiento.setLinea(detalle.getLinea());
					movimiento.setFecha(detalle.getFecha());
					movimiento.setUidTransaccionDet(detalle.getUidTransaccionDet());
					movimiento.setAbono(detalle.getAbono());
					movimiento.setCargo(detalle.getCargo());
					movimiento.setCodConceptoMovimiento(detalle.getCodconceptoMov());
					movimiento.setCoddivisa(detalle.getCoddivisa());
					movimiento.setCodMedioPago(detalle.getCodmedpag());
					movimiento.setConcepto(detalle.getConcepto());
					movimiento.setDocumento(detalle.getDocumento());
					movimiento.setIdDocumento(detalle.getIdDocumento());
					movimiento.setIdTipoDocumento(detalle.getIdTipoDocumento());
					movimiento.setTipoDeCambio(detalle.getTipoDeCambio());
					movimiento.setUidTransaccionDet(detalle.getUidTransaccionDet());
					movimiento.setUsuario(detalle.getUsuario());
					cajaMovimientoMapper.insert(movimiento);
				}
			}

			sqlSession.commit();
			log.debug("guardarCaja() - Caja con UID Diario Caja: " 
				    + cajaDto.getCabecera().getUidDiarioCaja() 
				    + " guardada en la base de datos correctamente.");
			return new Caja(cajaBean);
		}
		catch (Exception e) {
			log.error("guardarCaja() - Se ha producido un error insertando caja:" + e.getMessage(), e);
			sqlSession.rollback();
			throw e;
		}
		finally {
			sqlSession.close();
		}
	}

	public CajaDTO pedirCajaEnMaster(UsuarioBean usuario) throws RestException, RestHttpException {
		log.debug("pedirCajaEnMaster() - Pidiendo caja en master para el usuario : " + usuario.getDesusuario());
		String apiKey = variablesServices.getVariableAsString(Variables.WEBSERVICES_APIKEY);

		CajaRequestRest request = new CajaRequestRest();
		request.setUidActividad(sesionAplicacion.getUidActividad());
		request.setApiKey(apiKey);
		request.setCodalm(sesionAplicacion.getCodAlmacen());
		request.setCodcaja(sesionAplicacion.getCodCaja());
		request.setUsuario(usuario.getUsuario());

		CajaResponseRest cajaDto = CajasRest.recuperarCaja(request);
		log.info("pedirCajaEnMaster() - Respuesta recibida de la API para la caja del usuario: " + usuario.getDesusuario());

		if (cajaDto == null || cajaDto.getCaja() == null) {
			log.info("pedirCajaEnMaster() - No se ha encontrado una caja en la caja Master");
			return null;
		}
		
		log.info("pedirCajaEnMaster() - Caja recibida con éxito para el usuario: " + usuario.getDesusuario() + ". UID Diario Caja: " + cajaDto.getCaja().getCabecera().getUidDiarioCaja());
	    
		return cajaDto.getCaja();
	}

	public void transferirCaja(Caja caja) throws RestException, RestHttpException, CajasServiceException {
		log.debug("transferirCaja() - Transfiriendo caja : " + caja.getCodAlm());
		String apiKey = variablesServices.getVariableAsString(Variables.WEBSERVICES_APIKEY);
		
		CajaRequestRest request = new CajaRequestRest();
		request.setUidActividad(sesionAplicacion.getUidActividad());
		request.setApiKey(apiKey);
		request.setUidDiarioCaja(caja.getUidDiarioCaja());
		
		log.info("transferirCaja() - Petición REST de caja creada con uidActividad: " + sesionAplicacion.getUidActividad() + " y uidDiarioCaja: " + caja.getUidDiarioCaja());

		CajaDTO cajaDTO = new CajaDTO();
		CabeceraCaja cabecera = new CabeceraCaja();
		cabecera.setCodalm(caja.getCodAlm());
		cabecera.setCodcaja(caja.getCodCaja());
		cabecera.setFechaApertura(caja.getFechaApertura());
		cabecera.setFechaCierre(caja.getFechaCierre());
		cabecera.setFechaContable(caja.getFechaContable());
		cabecera.setUidActividad(sesion.getAplicacion().getUidActividad());
		cabecera.setUidDiarioCaja(caja.getUidDiarioCaja());
		cabecera.setUsuario(caja.getUsuario());
		cajaDTO.setCabecera(cabecera);
		
		log.info("transferirCaja() - Cabecera de la caja preparada con código de almacén: " + cabecera.getCodalm() + " y código de caja: " + cabecera.getCodcaja());

		consultarMovimientos(caja);
		
		log.info("Movimientos de la caja consultados. Total movimientos: " + caja.getMovimientos().size());

		for (CajaMovimientoBean movimiento : caja.getMovimientos()) {
			DetalleCaja detalle = new DetalleCaja();
			detalle.setAbono(movimiento.getAbono());
			detalle.setCargo(movimiento.getCargo());
			detalle.setCodconceptoMov(movimiento.getCodConceptoMovimiento());
			detalle.setCoddivisa(movimiento.getCoddivisa());
			detalle.setCodmedpag(movimiento.getCodMedioPago());
			detalle.setConcepto(movimiento.getConcepto());
			detalle.setDocumento(movimiento.getDocumento());
			detalle.setFecha(movimiento.getFecha());
			detalle.setIdDocumento(movimiento.getIdDocumento());
			detalle.setIdTipoDocumento(movimiento.getIdTipoDocumento());
			detalle.setLinea(movimiento.getLinea());
			detalle.setTipoDeCambio(movimiento.getTipoDeCambio());
			detalle.setUidActividad(movimiento.getUidActividad());
			detalle.setUidDiarioCaja(movimiento.getUidDiarioCaja());
			detalle.setUidTransaccionDet(movimiento.getUidTransaccionDet());
			detalle.setUsuario(movimiento.getUsuario());
			cajaDTO.getDetalles().add(detalle);
		}
		CajasRest.transferirCaja(request, cajaDTO);
	}

	public void marcarCajaTransferida(CajaBean caja) {
		log.debug("marcarCajaTransferid () - Marcando caja : " + caja.getCodCaja());
//		String apiKey = variablesServices.getVariableAsString(Variables.WEBSERVICES_APIKEY);
//		SqlSession sqlSession = SpringContext.getBean(SpringTransactionSqlSession.class);
//		try {
//			sqlSession.openSession(SessionFactory.openSession());
			CajaMovimientoExample detalleExample = new CajaMovimientoExample();
			detalleExample.or().andUidActividadEqualTo(sesion.getAplicacion().getUidActividad()).andUidDiarioCajaEqualTo(caja.getUidDiarioCaja());

			if (sesion.getAplicacion().getTiendaCaja().getIdTipoCaja() != 0L) {
				/* Si estamos en la caja master, no borraremos nada */
				cajaMovimientoMapper.deleteByExample(detalleExample);
				cajaMapper.deleteByPrimaryKey(caja);
			}


//			CajaRequestRest request = new CajaRequestRest();
//			request.setApiKey(apiKey);
//			request.setUidActividad(sesion.getAplicacion().getUidActividad());
//			request.setUidDiarioCaja(caja.getUidDiarioCaja());
			
//			request.setCodcaja(caja.getCodCaja());
//			request.setCodcaja(CajasConstants.PARAM_CAJA_APARCADA);
			
//			CajasRest.cajaTransferidaPOS(request);

//			sqlSession.commit();

			((BricodepotSesionCaja) sesion.getSesionCaja()).setCajaAbierta(null);
//		}
//		catch (Exception e) {
//			log.error("cajaTransferida() - Se ha producido un error insertando caja:" + e.getMessage(), e);
////			sqlSession.rollback();
//			throw e;
//		}
//		finally {
//			sqlSession.close();
//		}
	}

	@Override
	public Caja consultarCajaAbierta() throws CajasServiceException, CajaEstadoException {
		try {
			CajaExample exampleCaja = new CajaExample();
			exampleCaja.or().andUidActividadEqualTo(sesion.getAplicacion().getUidActividad()).andCodAlmacenEqualTo(sesion.getAplicacion().getCodAlmacen())
			        .andCodcajaEqualTo(sesion.getAplicacion().getCodCaja()).andFechaCierreIsNull().andFechaEnvioIsNull();
			log.debug("consultarCajaAbierta() - Consultado caja abierta en sesion");
			List<CajaBean> cajasBean = cajaMapper.selectByExample(exampleCaja);

			if (cajasBean.isEmpty()) {
				throw new CajaEstadoException(I18N.getTexto("No existe caja abierta en el sistema"));
			}
			return new Caja(cajasBean.get(0));
		}
		catch (CajaEstadoException e) {
			throw e;
		}
		catch (Exception e) {
			String msg = "Se ha producido un error consultando caja abierta en sesion :" + e.getMessage();
			log.error("consultarCajaAbiertaNoTransferida() - " + msg, e);
			throw new CajasServiceException(I18N.getTexto("Error al consultar caja abierta en sesión del sistema"), e);
		}
	}

	@Override
	public Caja crearCaja(Date fechaApertura) throws CajasServiceException, CajaEstadoException {
		Caja caja = super.crearCaja(fechaApertura);
		Dispositivos.abrirCajon();
		return caja;
	}

	@Override
	public CajaMovimientoBean crearMovimientoManual(BigDecimal importe, String codConcepto, String documento, String descConcepto) throws CajasServiceException {
		log.debug("crearMovimientoManual() - Registrando movimiento manual por importe: " + importe + ". Y concepto: " + codConcepto);
		SqlSession sqlSession = SpringContext.getBean(SpringTransactionSqlSession.class);
		CajaMovimientoBean movimiento = new CajaMovimientoBean();
		try {
			sqlSession.openSession(SessionFactory.openSession());

			CajaConceptoBean concepto = cajaConceptosServices.getConceptoCaja(codConcepto);
			if (concepto == null) {
				log.error("crearMovimientoManual() - Se está intentando insertar un movimiento con concepto nulo. Código concepto: " + codConcepto);
				throw new CajasServiceException(I18N.getTexto("Error al insertar movimiento de caja"));
			}
			concepto.setDesConceptoMovimiento(descConcepto);
			if (CajaConceptoBean.MOV_ENTRADA.equals(concepto.getInOut())) {
				movimiento.setCargo(importe);
			}
			else if (CajaConceptoBean.MOV_SALIDA.equals(concepto.getInOut())) {
				movimiento.setAbono(importe);
			}
			else {
				if (BigDecimalUtil.isMayorACero(importe)) {
					movimiento.setAbono(importe); // salida de caja
				}
				else {
					movimiento.setCargo(importe.negate()); // entrada de caja
				}
			}
			movimiento.setCodConceptoMovimiento(concepto.getCodConceptoMovimiento());
			movimiento.setConcepto(concepto.getDesConceptoMovimiento());
			movimiento.setCodMedioPago(MediosPagosService.medioPagoDefecto.getCodMedioPago());
			movimiento.setDocumento(documento);
			movimiento.setFecha(new Date());
			movimiento.setUsuario(sesion.getSesionUsuario().getUsuario().getUsuario());

			crearMovimiento(sqlSession, movimiento);

			sqlSession.commit();
			sesioncaja.actualizarDatosCaja();

		}
		catch (CajasServiceException e) {
			sqlSession.rollback();
			throw e;
		}
		catch (Exception e) {
			sqlSession.rollback();
			String msg = "Se ha producido un error insertando movimiento de caja por concepto: " + codConcepto + " : " + e.getMessage();
			log.error("crearMovimientoManual() - " + msg, e);
			throw new CajasServiceException(I18N.getTexto("Error al insertar movimiento de caja"), e);
		}
		finally {
			sqlSession.close();
		}

		return movimiento;
	}

	public List<CajaMovimientoBean> consultarMovHistorico(String codCaja, String codConcepto_mov) {
		SqlSession sqlSession = new SqlSession();
		try {
			sqlSession.openSession(SessionFactory.openSession());
			String uidActividad = sesion.getAplicacion().getUidActividad();
			List<CajaMovimientoBean> lista = cajaMovMapper.selectMovCaja(uidActividad, codCaja, codConcepto_mov);

			return lista;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			sqlSession.close();
		}
	}

	public TicketBean generarTicket(String codCaja) {
		log.debug("generarTicket() - Se procede a generar el ticket para documento de caja : " + codCaja);

		TicketBean ticket = new TicketBean();
		try {
			ticket.setUidActividad(sesion.getAplicacion().getUidActividad());
			String uid = UUID.randomUUID().toString();
			ticket.setUidTicket(uid);
			ticket.setLocatorId(uid);

			String codalm = sesion.getAplicacion().getCodAlmacen();
			ticket.setCodAlmacen(codalm);

			String codcaja = sesion.getAplicacion().getCodCaja();
			ticket.setCodcaja(codcaja);

			TipoDocumentoBean documento = null;

			if (codCaja.equals(CajasConstants.PARAM_CAJA_80)) {
//				documento = sesion.getAplicacion().getDocumentos().getDocumento(COD_TIPO_DOCUMENTO_CAJA_FUERTE);
				documento = obtenerDocumentoCaja(COD_TIPO_DOCUMENTO_CAJA_FUERTE);
				ticket.setCodcaja(CajasConstants.PARAM_CAJA_80);
			}
			else {
//				documento = sesion.getAplicacion().getDocumentos().getDocumento(COD_TIPO_DOCUMENTO_PROSEGUR);
				documento = obtenerDocumentoCaja(COD_TIPO_DOCUMENTO_PROSEGUR);
				ticket.setCodcaja(CajasConstants.PARAM_CAJA_90);
			}

			ticket.setIdTipoDocumento(documento.getIdTipoDocumento());

			ticket.setCodTicket("*");
			ticket.setFirma("*");
			ticket.setSerieTicket("*");

			ticket.setFecha(new Date());
			ticket.setIdTicket(servicioContadores.obtenerValorContador(documento.getIdContador(), sesion.getAplicacion().getUidActividad()));
			return ticket;
		}
		catch (Exception e) {
			log.error("generarTicket() - Ha habido un error al generar el ticket para documento de caja: " + codCaja + "error: " + e.getMessage(), e);
			throw new TicketException("generarTicket() - Ha habido un error al generar el ticket para documento de caja", e);
		}
	}

	public CajaMovimientoBean generarDocumentoParaCajaFicticia(String codCaja, String codConcepto, BigDecimal cargo, BigDecimal abono, String documento, String uidDiarioCaja)
	        throws MarshallUtilException, TicketsServiceException, ConceptosMovimientosCajaNotFoundException {
		log.debug("generarDocumentoParaCajaFicticia() - generando documpento para caja : " + codCaja + " y concepto : " + codConcepto);

		CajaFicticiaDTO cajaFicticia = new CajaFicticiaDTO();
		cajaFicticia.init();
		cajaFicticia.setCodAlm(sesion.getAplicacion().getCodAlmacen());
		cajaFicticia.setCodCaja(codCaja);
		CajaConceptoBean concepto = cajaConceptosServices.getConceptoCaja(codConcepto);
		if (concepto == null) {
			throw new ConceptosMovimientosCajaNotFoundException("generarDocumentoParaCajaFicticia() - No se ha encontrado el concepto: " + codConcepto);
		}
		CajaMovimientoBean movimiento = cajaFicticia.getMovimiento();
		movimiento.setCodConceptoMovimiento(concepto.getCodConceptoMovimiento());
		movimiento.setConcepto(concepto.getDesConceptoMovimiento());
		movimiento.setCodMedioPago(MediosPagosService.medioPagoDefecto.getCodMedioPago());
		movimiento.setDocumento(documento);
		movimiento.setFecha(new Date());
		movimiento.setUsuario(sesion.getSesionUsuario().getUsuario().getUsuario());
		movimiento.setUidTransaccionDet(sesioncaja.getCajaAbierta().getUidDiarioCaja());

		if (abono != null) {
			movimiento.setAbono(abono);
			movimiento.setCargo(BigDecimal.ZERO);
		}
		else {
			movimiento.setAbono(BigDecimal.ZERO);
			movimiento.setCargo(cargo);
		}

		if (uidDiarioCaja == null) {
			uidDiarioCaja = getUidDiarioCaja(codCaja, sesion.getAplicacion().getCodAlmacen());
		}
		cajaFicticia.setUidDiarioCaja(uidDiarioCaja);
		byte[] xml = MarshallUtil.crearXML(cajaFicticia);
		TicketBean ticket = generarTicket(codCaja);
		log.debug("generarDocumentoParaCaja() - XML : " + new String(xml));
		ticket.setTicket(xml);

		ticketsService.insertarTicket(null, ticket, false);
		log.debug("generarDocumentoParaCajaFicticia() - Ticket de cierre guardado en la base de datos para la caja: " + codCaja);
		return movimiento;
	}

	public String getUidDiarioCaja(String caja, String almacen) {
		log.debug("getUidDiarioCaja() - Haciendo peticiendo por cashmanagement de uid diario de caja para caja: " + caja + " y almacen: " + almacen);
		String uidDiarioCaja = null;
		try {
			DatosSesionBean datosSesion = new DatosSesionBean();
			datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
			datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
			datosSesion.setLocale(new Locale(AppConfig.idioma, AppConfig.pais));
			CashManagementApi api = comerzziaApiManager.getClient(datosSesion, "CashManagementApi");
			CajaFicticiaUidDiarioCaja cajaFicticia = api.getUidDiarioCaja(caja, almacen);

			uidDiarioCaja = cajaFicticia.getUidDiarioCaja();
		}
		catch (Exception e) {
			log.error("getUidDiarioCaja() - Ha ocurrido un error haciendo la peticion para el uid diario de caja a central de la caja: "+caja+" -almacen: "+almacen, e);
		}
		return uidDiarioCaja;
	}

	public Movimiento getMovimiento91(String codAlmacen) {
		log.debug("getMovimiento91() - Haciendo peticion por cashmanagement de movimiento 91 para almacen: " + codAlmacen);
		Movimiento movimiento = null;
		try {
			DatosSesionBean datosSesion = new DatosSesionBean();
			datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
			datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
			datosSesion.setLocale(new Locale(AppConfig.idioma, AppConfig.pais));
			CashManagementApi api = comerzziaApiManager.getClient(datosSesion, "CashManagementApi");
			movimiento = api.getUltimoMovimiento91deCaja90(codAlmacen);
		}
		catch (Exception e) {
			log.error("getMovimiento91() ", e);
		}
		return movimiento;
	}

	public List<Movimiento> getListaMovimientos90(String codAlmacen) {
		log.debug("getListaMovimientos90() - Haciendo peticion por cashmanagement de lista de movimientos 90 para almacen: " + codAlmacen);
		List<Movimiento> listaMov90 = null;
		try {
			DatosSesionBean datosSesion = new DatosSesionBean();
			datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
			datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
			datosSesion.setLocale(new Locale(AppConfig.idioma, AppConfig.pais));
			CashManagementApi api = comerzziaApiManager.getClient(datosSesion, "CashManagementApi");
			listaMov90 = api.getUltimoMovimiento90deCaja90(codAlmacen);
		}
		catch (Exception e) {
			log.error("getListaMovimientos90() ", e);
		}
		return listaMov90;
	}
	
	public Caja90Dto getImporteUidDiarioCaja90(String codAlmacen) {
		log.debug("getImporteUidDiarioCaja90() - Haciendo peticion por cashmanagement de importe y uidDiario 90 para almacen: " + codAlmacen);
		Caja90Dto caja90= null;
		try {
			DatosSesionBean datosSesion = new DatosSesionBean();
			datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
			datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
			datosSesion.setLocale(new Locale(AppConfig.idioma, AppConfig.pais));
			CashManagementApi api = comerzziaApiManager.getClient(datosSesion, "CashManagementApi");
			caja90 = api.getImporteUidDiarioCaja90(codAlmacen);
		}
		catch (Exception e) {
			log.error("getImporteUidDiarioCaja90() ", e);
		}
		return caja90;
	}
	
	public List<DetMovRetiradaKey> comparaMovimientos(List<com.comerzzia.bricodepot.cashmanagement.client.model.DetalleCaja> movimientos, List<DetMovRetiradaKey> retiradaMovimientos){
		log.debug("comparaMovimientos()");
		List<DetMovRetiradaKey> movimientosComparados = new ArrayList<DetMovRetiradaKey>();
		if(retiradaMovimientos != null ) {
			for(com.comerzzia.bricodepot.cashmanagement.client.model.DetalleCaja mov : movimientos) {
				DetMovRetiradaKey movRetirada = new DetMovRetiradaKey();
				
				movRetirada.setUidActividad(mov.getUidActividad());
				movRetirada.setUidDiarioCaja(mov.getUidDiarioCaja());
				movRetirada.setLinea(mov.getLinea());
				
				
				if(!retiradaMovimientos.contains(movRetirada)) {
					movimientosComparados.add(movRetirada);
				}
			}
		}
		return movimientosComparados;
	}
	public void crearDocumentoYguardarMov(RetiradaABancoDTO retiradaABancoDTO, List<DetMovRetiradaKey> movimientos,CashManagementApi api) {
		log.debug("guardarMovimientos() - Se procede a guardar los movimientos de retiradas a banco");

		TicketBean ticket = new TicketBean();
		try {
			String uidActividad = sesion.getAplicacion().getUidActividad();
			retiradaABancoDTO.setUidActividad(uidActividad);
			ticket.setUidActividad(uidActividad);

			String uid = UUID.randomUUID().toString();
			ticket.setUidTicket(uid);
			ticket.setLocatorId(uid);

			String codalm = sesion.getAplicacion().getCodAlmacen();
			ticket.setCodAlmacen(codalm);

			String codcaja = sesion.getAplicacion().getCodCaja();
			ticket.setCodcaja(codcaja);

			TipoDocumentoBean tipoDocumentoMovRet = sesion.getAplicacion().getDocumentos().getDocumento(TIPO_DOC_MOVIMIENTO_RETIRADA);
			ticket.setIdTipoDocumento(tipoDocumentoMovRet.getIdTipoDocumento());

			ticket.setCodTicket("*");
			ticket.setFirma("*");
			ticket.setSerieTicket("*");

			ticket.setCodAlmacen(sesion.getAplicacion().getCodAlmacen());
			ticket.setCodcaja(sesion.getAplicacion().getCodCaja());
			ticket.setFecha(new Date());

			ticket.setIdTicket(servicioContadores.obtenerValorContador(ID_MOV_RETIRADA, uidActividad));

			byte[] xml = MarshallUtil.crearXML(retiradaABancoDTO);
			log.debug("guardarMovimientos() - XML de retirada: " + new String(xml));
			ticket.setTicket(xml);

			ticketsService.insertarTicket(null, ticket, false);

			//api.insertarMovimientos(movimientos);

		}
		catch (Exception e) {
			log.error("guardarMovimientos() - Ha habido un error al guardar los movimientos de retirada a banco: " + e.getMessage(), e);
		}
		finally {
		}
	}
	
	public TipoDocumentoBean obtenerDocumentoCaja(String codDocumento) {
		DatosSesionBean datosSesion = new DatosSesionBean();
		TipoDocumentoBean documentoCajaFuerteProseur = new TipoDocumentoBean();
		try {
			datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
			datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
			com.comerzzia.core.model.tiposdocumentos.TipoDocumentoBean documentoConsultado = ServicioTiposDocumentosImpl.get().consultar(datosSesion, codDocumento, PAIS_CMZ);

			if(documentoConsultado != null) {
				documentoCajaFuerteProseur.setIdContador(documentoConsultado.getIdContador());
				documentoCajaFuerteProseur.setIdTipoDocumento(documentoConsultado.getIdTipoDocumento());
				return documentoCajaFuerteProseur;
			}
			
		}
		catch (Exception e) {
			log.error("obtenerDocumentoCajaFuerte() - Ha ocurrido un error al obtener el tipo documento de caja: " + e.getMessage(), e);
		}
		return null;

	}
	
	public CajaMovimientoBean guardarMovimientos(BigDecimal importe,String documento,String codAlmacen) throws CajaException {
		log.debug("guardarMovimientos() - guardando movimien tos para el almacen : "+codAlmacen);
		CajaMovimientoBean mov = null;
		try {
			CajaConceptoBean cajaConcepto = cajaConceptosServices.consultarConcepto(CajasConstants.PARAM_CONCEPTO_91);
			DatosSesionBean datosSesion = new DatosSesionBean();
			datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
			datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
			datosSesion.setLocale(new Locale(AppConfig.idioma, AppConfig.pais));
			CashManagementApi api = comerzziaApiManager.getClient(datosSesion, "CashManagementApi");

			Movimiento movimiento91 = cajasService.getMovimiento91(sesion.getAplicacion().getCodAlmacen());
			if (movimiento91 == null) {
				movimiento91 = new Movimiento();
			}
			Date fechaFiltro = movimiento91.getFecha() == null ? null : movimiento91.getFecha();
			List<Movimiento> lista90 = cajasService.getListaMovimientos90(sesion.getAplicacion().getCodAlmacen());
			if (lista90 == null) {
				lista90 = new ArrayList<Movimiento>();
			}
			List<Movimiento> retiradaMovsPendiente = null;
			if (!lista90.isEmpty() && lista90 != null) {
				Collections.reverse(lista90);
				retiradaMovsPendiente = lista90;
				if (fechaFiltro != null) {
					retiradaMovsPendiente = lista90.stream().filter(c -> c.getFecha().compareTo(fechaFiltro) > 0)
							.collect(Collectors.toList());
				}
			}
			List<DetMovRetiradaKey> retiradaMovs = null;
			if(retiradaMovsPendiente != null) {
				retiradaMovs = new ArrayList<DetMovRetiradaKey>();
				for (Movimiento movimiento : retiradaMovsPendiente) {
					DetMovRetiradaKey movRetirada = new DetMovRetiradaKey();
					
					movRetirada.setUidActividad(movimiento.getUidActividad());
					movRetirada.setUidDiarioCaja(movimiento.getUidDiarioCaja());
					movRetirada.setLinea(movimiento.getLinea());
					retiradaMovs.add(movRetirada);
					log.debug("guardarMovimientos() - Agregado movimiento 90 pendiente con uidDiarioCaja: " + movimiento.getUidDiarioCaja() + " de codigo concepto: " +movimiento.getCodconceptoMov() + " por el usuario: " + movimiento.getUsuario());
					
				}
			}

			if (documento == null) {
				documento = "";
			}
			
			mov = crearMovimientoManualParticular(importe,cajaConcepto,documento,api,codAlmacen);
			
			RetiradaABancoDTO retiradaABancoDTO = new RetiradaABancoDTO();
			retiradaABancoDTO.setUidActividad(sesion.getAplicacion().getUidActividad());
			retiradaABancoDTO.setMovimiento91(mov);
			/* Se añade cargo a 0 para que el procesador de BO no de error al intentar buscar su nodo */
			mov.setCargo(BigDecimal.ZERO);
			retiradaABancoDTO.setMovimientos90(retiradaMovs);
			crearDocumentoYguardarMov(retiradaABancoDTO,retiradaMovs,api);
		}
		catch (Exception e) {
			log.error("guardarMovimientos() ", e);
			throw new CajaException("guardarMovimientos() - Se ha producido un error al guardar los movimientos de la caja 90",e);
		}
		return mov;
	}
	public CajaMovimientoBean crearMovimientoManualParticular(BigDecimal importe, CajaConceptoBean concepto,String documento,CashManagementApi api,String codAlmacen) throws CajasServiceException {
		log.debug("crearMovimientoManualParticular() ");
		CajaMovimientoBean movimiento = new CajaMovimientoBean();
		if (concepto == null) {
			log.error("crearMovimientoManual() - Se está intentando insertar un movimiento con concepto nulo. Código concepto: 91");
			throw new CajasServiceException(I18N.getTexto("Error al insertar movimiento de caja"));
		}
		if (CajaConceptoBean.MOV_ENTRADA.equals(concepto.getInOut())) {
			movimiento.setCargo(importe);
		}
		else if (CajaConceptoBean.MOV_SALIDA.equals(concepto.getInOut())) {
			movimiento.setAbono(importe);
		}
		else {
			if (BigDecimalUtil.isMayorACero(importe)) {
				movimiento.setAbono(importe); // salida de caja
			}
			else {
				movimiento.setCargo(importe.negate()); // entrada de caja
			}
		}
		movimiento.setUidActividad(sesion.getAplicacion().getUidActividad());
		movimiento.setCodConceptoMovimiento(concepto.getCodConceptoMovimiento());
		movimiento.setConcepto(concepto.getDesConceptoMovimiento());
		movimiento.setCodMedioPago(MediosPagosService.medioPagoDefecto.getCodMedioPago());
		movimiento.setDocumento(documento);
		movimiento.setFecha(new Date());
		movimiento.setUsuario(sesion.getSesionUsuario().getUsuario().getUsuario());
		CajaFicticiaUidDiarioCaja uidDiarioCaja = api.getUidDiarioCaja(CajasConstants.PARAM_CAJA_90, codAlmacen);
		movimiento.setUidDiarioCaja(uidDiarioCaja.getUidDiarioCaja());
		Integer linea = api.selectMaxLineaMovimiento(uidDiarioCaja.getUidDiarioCaja());
		if (linea != null) {
			movimiento.setLinea(linea+1);
		}else {
			movimiento.setLinea(1);
		}
		return movimiento;
	}
}
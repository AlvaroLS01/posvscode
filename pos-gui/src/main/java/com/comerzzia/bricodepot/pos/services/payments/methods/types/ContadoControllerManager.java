package com.comerzzia.bricodepot.pos.services.payments.methods.types;

import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.services.core.documentos.DocumentoException;
import com.comerzzia.pos.services.core.documentos.Documentos;
import com.comerzzia.pos.services.core.documentos.tipos.TipoDocumentoService;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.payments.PaymentException;
import com.comerzzia.pos.services.payments.events.PaymentOkEvent;
import com.comerzzia.pos.services.payments.methods.types.ContadoManager;
import com.comerzzia.pos.services.ticket.pagos.PagoTicket;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.i18n.I18N;

@Primary
@Component
@Scope("prototype")
public class ContadoControllerManager extends ContadoManager {
	
	protected Logger log = Logger.getLogger(getClass());
	public static final String X_PROPIEDAD_IMPORTE_MAXIMO_EFECTIVO = "X_IMPORTE_MAXIMO_EFECTIVO";
	public static final String X_PROPIEDAD_IMPORTE_MAXIMO_EFECTIVO_PROFESIONAL = "X_IMPORTE_MAXIMO_EFECTIVO_PROFESIONAL";
	public static final String X_PROPIEDAD_IMPORTE_MAXIMO_EFECTIVO_EX = "X_IMPORTE_MAXIMO_EFECTIVO_EX";

	@Autowired
	protected Sesion sesion;
	@Autowired
	protected TipoDocumentoService tipoDocService;

	@Autowired
	protected Documentos documentosService;

	protected BigDecimal importeMaxEfectivo;

	private String tipoCliente;

	@Override
	public boolean pay(BigDecimal amount) throws PaymentException {
		if (ticket.getCabecera().getCliente().getCodpais().toUpperCase().equals("PT") && ticket.getCabecera().getCliente().getDatosFactura() != null) {
			log.debug("pay() - Se comprobara el importe para PT");
			PaymentOkEvent event = new PaymentOkEvent(this, paymentId, amount);

			Boolean importeMaximoSuperado = superaImporteMaximoEfectivo(amount);
			if (importeMaximoSuperado == null) {
				log.error("pay() - Error recuperando las propiedades del documento, no estan configuradas en bbdd local");
				throw new PaymentException(I18N.getTexto("Error comprobando el efectivo máximo, por favor contacte con un administrador"));
			}
			else if (importeMaximoSuperado) {
				throw new PaymentException(I18N.getTexto("El importe no puede ser superior a") + " " + importeMaxEfectivo + I18N.getTexto("€ al ser un cliente") + " " + I18N.getTexto(tipoCliente));
			}

			getEventHandler().paymentOk(event);

			return true;
		}
		else {
			return super.pay(amount);
		}
	}

	@SuppressWarnings("unchecked")
	protected Boolean superaImporteMaximoEfectivo(BigDecimal amount) {
		/*
		 * Consultamos las propiedades del tipo de documento para coger la propiedad de límite de pago de efectivo,
		 * dependiendo si es país extranjero o no.
		 */
		String codPaisCliente = null;
		tipoCliente = null;
		importeMaxEfectivo = null;
		TipoDocumentoBean tipoDocumento = null;

		log.debug("superaImporteMaximoEfectivo() - recuperando pais del cliente");
		if (ticket.getCliente().getDatosFactura() == null) {
			codPaisCliente = ticket.getCliente().getCodpais();
		}
		else {
			codPaisCliente = ticket.getCliente().getDatosFactura().getPais();
		}

		try {
			tipoDocumento = documentosService.getDocumento(ticket.getCabecera().getTipoDocumento());

		}
		catch (DocumentoException e) {
			log.error("superaImporteMaximoEfectivo() - Error al recuperar el documento");
		}

		log.debug("superaImporteMaximoEfectivo() - comprobando pais del cliente");
		if (sesion.getAplicacion().getTienda().getCliente().getCodpais().equals(codPaisCliente)) {
			if (ticket.getCabecera().getCliente().getDatosFactura().getCif().startsWith("1") || ticket.getCabecera().getCliente().getDatosFactura().getCif().startsWith("2")
			        || ticket.getCabecera().getCliente().getDatosFactura().getCif().startsWith("3")) {
				importeMaxEfectivo = getImporteMaximoEfectivo(tipoDocumento);
				tipoCliente = I18N.getTexto("particular");
			}
			else {
				importeMaxEfectivo = getImporteMaximoEfectivoProfesional(tipoDocumento);
				tipoCliente = I18N.getTexto("profesional");
			}
		}
		else {
			log.debug("superaImporteMaximoEfectivo() - cliente extranjero");
			importeMaxEfectivo = getImporteMaximoEfectivoEx(tipoDocumento);
			tipoCliente = I18N.getTexto("extranjero");
		}

		Boolean superaImporte = Boolean.FALSE;
		if (importeMaxEfectivo != null) {
			log.debug("superaImporteMaximoEfectivo() - comprobando importe máximo");

			BigDecimal importeEfectivo = BigDecimal.ZERO;
			if (ticket.getPagos() != null && ticket.getPagos().isEmpty()) {
				if (amount.compareTo(importeMaxEfectivo) == 1) {
					return Boolean.TRUE;
				}
			}
			else {
				for (PagoTicket pago : (List<PagoTicket>) ticket.getPagos()) {
					if (pago.getMedioPago().getEfectivo() && pago.getMedioPago().getManual() && pago.getCodMedioPago().equals("0000")) {
						importeEfectivo = importeEfectivo.add(pago.getImporte().abs());
					}
				}
				importeEfectivo = importeEfectivo.add(amount);
				if (BigDecimalUtil.isMayor(importeEfectivo, importeMaxEfectivo)) {
					superaImporte = Boolean.TRUE;
				}
			}

		}
		else if (importeMaxEfectivo == null) {
			superaImporte = null;
		}

		return superaImporte;
	}
	
	public BigDecimal getImporteMaximoEfectivo(TipoDocumentoBean documentoSeleccionado) {
		log.debug("getImporteMaximoEfectivo() - recuperando importe maximo efectivo");
		if (documentoSeleccionado.getPropiedades().containsKey(X_PROPIEDAD_IMPORTE_MAXIMO_EFECTIVO)) {
			String importeMax = documentoSeleccionado.getPropiedades().get(X_PROPIEDAD_IMPORTE_MAXIMO_EFECTIVO).getValor();
			if (importeMax != null && !importeMax.isEmpty()) {
				return new BigDecimal(importeMax);
			}
		}
		return null;
	}

	public BigDecimal getImporteMaximoEfectivoProfesional(TipoDocumentoBean documentoSeleccionado) {
		log.debug("getImporteMaximoEfectivoProfesional() - recuperando importe maximo efectivo");
		if (documentoSeleccionado.getPropiedades().containsKey(X_PROPIEDAD_IMPORTE_MAXIMO_EFECTIVO_PROFESIONAL)) {
			String importeMax = documentoSeleccionado.getPropiedades().get(X_PROPIEDAD_IMPORTE_MAXIMO_EFECTIVO_PROFESIONAL).getValor();
			if (importeMax != null && !importeMax.isEmpty()) {
				return new BigDecimal(importeMax);
			}
		}
		return null;
	}

	public BigDecimal getImporteMaximoEfectivoEx(TipoDocumentoBean documentoSeleccionado) {
		log.debug("getImporteMaximoEfectivoEx() - recuperando importe maximo efectivo");
		if (documentoSeleccionado.getPropiedades().containsKey(X_PROPIEDAD_IMPORTE_MAXIMO_EFECTIVO_EX)) {
			String importeMax = documentoSeleccionado.getPropiedades().get(X_PROPIEDAD_IMPORTE_MAXIMO_EFECTIVO_EX).getValor();
			if (importeMax != null && !importeMax.isEmpty()) {
				return new BigDecimal(importeMax);
			}
		}
		return null;
	}
}

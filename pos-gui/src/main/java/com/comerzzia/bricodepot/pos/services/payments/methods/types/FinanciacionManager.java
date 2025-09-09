package com.comerzzia.bricodepot.pos.services.payments.methods.types;

import java.math.BigDecimal;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.apache.commons.lang.StringUtils;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.financiacion.FinanciacionController;
import com.comerzzia.pos.services.payments.PaymentDto;
import com.comerzzia.pos.services.payments.PaymentException;
import com.comerzzia.pos.services.payments.events.PaymentOkEvent;
import com.comerzzia.pos.services.payments.methods.types.BasicPaymentMethodManager;


@Component
@Scope("prototype")
public class FinanciacionManager extends BasicPaymentMethodManager {
	
	private final String CAMPO_DOCUMENTO = "documento";
	
	@Override
	public boolean pay(BigDecimal amount) throws PaymentException {
		PaymentOkEvent event = new PaymentOkEvent(this, paymentId, amount);
		
		String documento = (String) parameters.get(FinanciacionController.PARAMETRO_DOCUMENTO);
		if (documento != null && StringUtils.isNotBlank(documento)) {
			event.addExtendedData(CAMPO_DOCUMENTO, documento);
		}
		getEventHandler().paymentOk(event);
		return true;
	}

	@Override
	public boolean returnAmount(BigDecimal amount) throws PaymentException {
		PaymentOkEvent event = new PaymentOkEvent(this, paymentId, amount);
		
		String documento = (String) parameters.get(FinanciacionController.PARAMETRO_DOCUMENTO);
		if (documento != null && StringUtils.isNotBlank(documento)) {
			event.addExtendedData(CAMPO_DOCUMENTO, documento);
		}
		getEventHandler().paymentOk(event);
		return true;
	}

	@Override
	public boolean cancelPay(PaymentDto payment) throws PaymentException {
		PaymentOkEvent event = new PaymentOkEvent(this, payment.getPaymentId(), payment.getAmount());
		event.setCanceled(true);
		getEventHandler().paymentOk(event);
		return true;
	}

	@Override
	public boolean cancelReturn(PaymentDto payment) throws PaymentException {
		PaymentOkEvent event = new PaymentOkEvent(this, payment.getPaymentId(), payment.getAmount());
		event.setCanceled(true);
		getEventHandler().paymentOk(event);
		return true;
	}

}

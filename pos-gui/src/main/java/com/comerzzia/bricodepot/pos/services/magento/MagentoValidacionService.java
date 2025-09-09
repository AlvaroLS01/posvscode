package com.comerzzia.bricodepot.pos.services.magento;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

// BRICO-266
@Service
public class MagentoValidacionService {

	protected static final Logger log = Logger.getLogger(MagentoValidacionService.class);

	public static final String REGEX_MAGENTO_EMAIL = "^((([a-z]|\\d|[!#\\$%&\\u0027\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+(\\.([a-z]|\\d|[!#\\$%&\\u0027\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+)*)|((\\u0022)((((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|\\x21|[\\x23-\\x5b]|[\\x5d-\\x7e]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(\\\\([\\x01-\\x09\\x0b\\x0c\\x0d-\\x7f]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]))))*(((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(\\u0022)))@((([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.)*(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.?$";
	public static final String REGEX_MAGENTO_TLF = "^\\d{9}$";

	public boolean validaFormatoEmail(String email) {
		log.debug("validaFormatoEmail() - Validando email: " + email);

		try {
			boolean emailValido = Pattern.compile(REGEX_MAGENTO_EMAIL, Pattern.CASE_INSENSITIVE).matcher(email).matches();
			log.debug("validaFormatoEmail() - El email: " + email + (emailValido ? " es válido" : " no es válido"));
			return emailValido;
		}
		catch (Exception e) {
			log.error("validaFormatoEmail() - Error validando email: " + email + " : " + e.getMessage(), e);
			return false;
		}
	}

	public boolean validaFormatoMovil(String movil) {
		log.debug("validaFormatoMovil() - Validando formato móvil: " + movil);

		try {
			boolean movilValido = Pattern.compile(REGEX_MAGENTO_TLF).matcher(movil).matches();
			log.debug("validaFormatoMovil() - El movil: " + movilValido + (movilValido ? " tiene formato válido" : " tiene formato invalido"));
			return movilValido;
		}
		catch (Exception e) {
			log.error("validaFormatoMovil() - Error validando movil: " + movil + " : " + e.getMessage(), e);
			return false;
		}
	}

}

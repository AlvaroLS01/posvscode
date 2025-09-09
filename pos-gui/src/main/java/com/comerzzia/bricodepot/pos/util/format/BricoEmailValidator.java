package com.comerzzia.bricodepot.pos.util.format;

import org.apache.commons.lang3.StringUtils;
import com.comerzzia.pos.util.i18n.I18N;

import javax.validation.ConstraintValidatorContext;

public class BricoEmailValidator {

	private static final String LOCAL_PART_REGEX = "^(?![.+])[A-Za-z0-9._+-]+(?<![.+])$";
	private static final String DOMAIN_LABEL_REGEX = "^(?!-)[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?<!-)$";
	private static final String DOMAIN_REGEX = "^(?=.{1,255}$)(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$";

	private BricoEmailValidator() {
		// no instancias
	}

	public static String getValidationErrorKey(String email) {
		if (StringUtils.isBlank(email)) {
			return null;
		}
		if (email.contains(" ") || email.indexOf('@') <= 0 || email.indexOf('@') != email.lastIndexOf('@')) {
			return I18N.getTexto("El formato del email no es válido");
		}

		String[] parts = email.split("@", 2);
		String local = parts[0];
		String domain = parts[1];

		if (local.length() > 64 || domain.length() > 255) {
			return I18N.getTexto("El formato del email no es válido");
		}

		if (!local.matches(LOCAL_PART_REGEX)) {
			return I18N.getTexto("El formato del email no es válido");
		}

		if (!domain.matches(DOMAIN_REGEX)) {
			return I18N.getTexto("El formato del email no es válido");
		}

		for (String label : domain.split("\\.")) {
			if (!label.matches(DOMAIN_LABEL_REGEX)) {
				return I18N.getTexto("El formato del email no es válido");
			}
		}

		return null;
	}

	public static boolean isValidEmail(String email) {
		return getValidationErrorKey(email) == null;
	}

	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		String email = value == null ? "" : value.toString();
		return isValidEmail(email);
	}
}

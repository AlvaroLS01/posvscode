package com.comerzzia.bricodepot.pos.services.passwords.validacion;

import com.comerzzia.validacion.passwords.PasswordChangeValidator;
import com.comerzzia.validacion.passwords.exceptions.LowerCaseException;
import com.comerzzia.validacion.passwords.exceptions.MinNumCharException;
import com.comerzzia.validacion.passwords.exceptions.NewPasswordConfirmEmptyException;
import com.comerzzia.validacion.passwords.exceptions.NewPasswordEmptyException;
import com.comerzzia.validacion.passwords.exceptions.NotMatchingException;
import com.comerzzia.validacion.passwords.exceptions.OldPasswordEmptyException;
import com.comerzzia.validacion.passwords.exceptions.SamePasswordException;
import com.comerzzia.validacion.passwords.exceptions.SpecialCharException;
import com.comerzzia.validacion.passwords.exceptions.UpperCaseException;

import org.springframework.stereotype.Service;

@Service
public class ValidacionPasswordService { // BRICO-326

	public void validaCambioPassword(String oldPassword, String newPassword, String newPasswordConfirm) throws OldPasswordEmptyException, NewPasswordEmptyException, NewPasswordConfirmEmptyException,
	        NotMatchingException, MinNumCharException, LowerCaseException, UpperCaseException, SpecialCharException, SamePasswordException {

		PasswordChangeValidator validator = PasswordChangeValidator.getInstance(oldPassword, newPassword, newPasswordConfirm);
		validator.validatePasswordChange();

	}
}

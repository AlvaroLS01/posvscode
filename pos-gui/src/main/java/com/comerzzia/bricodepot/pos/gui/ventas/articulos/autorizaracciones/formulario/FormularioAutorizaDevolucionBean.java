package com.comerzzia.bricodepot.pos.gui.ventas.articulos.autorizaracciones.formulario;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.comerzzia.pos.core.gui.validation.FormularioGui;

@Component
@Scope("prototype")
public class FormularioAutorizaDevolucionBean extends FormularioGui{

	@NotEmpty(message = "Campo requerido")
	private String usuario;
	@NotEmpty(message = "Campo requerido")
	private String pass;
	@NotEmpty(message = "Campo requerido")
	private String documento;
	@NotEmpty(message = "Campo requerido")
	
	public FormularioAutorizaDevolucionBean(){}
	
	@Override
	public void limpiarFormulario() {
		this.usuario = "";
		this.pass = "";
		this.documento = "";
	}

	public FormularioAutorizaDevolucionBean(String usuario, String pass, String documento, String tienda){
		this.usuario = usuario;
		this.pass = pass;
		this.documento = documento;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}
	
}

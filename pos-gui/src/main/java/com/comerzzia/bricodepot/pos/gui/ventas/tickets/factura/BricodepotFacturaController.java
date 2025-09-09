package com.comerzzia.bricodepot.pos.gui.ventas.tickets.factura;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.gui.ventas.conversion.ConversionController;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.gui.ventas.tickets.factura.FacturaController;
import com.comerzzia.pos.persistence.tickets.datosfactura.DatosFactura;
import com.comerzzia.pos.services.core.documentos.DocumentoException;
import com.comerzzia.pos.services.core.documentos.Documentos;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.ticket.TicketVenta;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Component
@Primary
public class BricodepotFacturaController extends FacturaController {

	@FXML
	protected Button btBuscarPais;

	@Autowired
	private Sesion sesion;

	@Override
	public void initializeForm() {
		super.initializeForm();

		setCamposModoEdicion(false);// TODO BCR poner condición, selección pantalla			

		refrescarDatosPantalla();
		
		if((Boolean)getDatos().containsKey(ConversionController.CONVERSION)){
			limpiarCampos();
		}
	}

	/* BRICO-253 se desactiva edición de campos, solo se elige cliente por buscador */
	protected void setCamposModoEdicion(boolean modoEdicion) {

		tfCodCliente.setDisable(!modoEdicion);
		tfDesCliente.setDisable(!modoEdicion);
		tfRazonSocial.setDisable(!modoEdicion);
		tfDomicilio.setDisable(!modoEdicion);
		tfPoblacion.setDisable(!modoEdicion);
		tfProvincia.setDisable(!modoEdicion);
		tfLocalidad.setDisable(!modoEdicion);
		tfCP.setDisable(!modoEdicion);
		tfNumDocIdent.setDisable(!modoEdicion);
		tfTelefono.setDisable(!modoEdicion);
		tfCodPais.setDisable(!modoEdicion);
		tfDesPais.setDisable(!modoEdicion);
		tfBanco.setDisable(!modoEdicion);
		tfBancoDomicilio.setDisable(!modoEdicion);
		tfBancoPoblacion.setDisable(!modoEdicion);
		tfBancoCCC.setDisable(!modoEdicion);

		cbTipoDocIdent.setDisable(!modoEdicion);

		// btBuscar.setVisible(modoEdicion); //Botón búsqueda de clientes
		btBusquedaCentral.setVisible(modoEdicion);
		btBuscarPais.setVisible(modoEdicion);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void establecerClienteFactura(Event event) {
		log.debug("establecerClienteFactura()");

		frDatosFactura.setNumDocIdent(frDatosFactura.trimTextField(tfNumDocIdent));
		frDatosFactura.setDomicilio(frDatosFactura.trimTextField(tfDomicilio));
		frDatosFactura.setProvincia(frDatosFactura.trimTextField(tfProvincia));
		frDatosFactura.setLocalidad(frDatosFactura.trimTextField(tfLocalidad));
		frDatosFactura.setPoblacion(frDatosFactura.trimTextField(tfPoblacion));
		frDatosFactura.setRazonSocial(frDatosFactura.trimTextField(tfRazonSocial));
		frDatosFactura.setcPostal(frDatosFactura.trimTextField(tfCP));
		frDatosFactura.setTelefono(frDatosFactura.trimTextField(tfTelefono));
		frDatosFactura.setPais(frDatosFactura.trimTextField(tfCodPais));
		frDatosFactura.setBanco(frDatosFactura.trimTextField(tfBanco));
		frDatosFactura.setBancoDomicilio(frDatosFactura.trimTextField(tfBancoDomicilio));
		frDatosFactura.setBancoPoblacion(frDatosFactura.trimTextField(tfBancoPoblacion));
		frDatosFactura.setBancoCCC(frDatosFactura.trimTextField(tfBancoCCC));

		if (validarFormularioDatosFactura()) {

			DatosFactura datosFactura = new DatosFactura();
			datosFactura.setCif(tfNumDocIdent.getText());
			datosFactura.setCp(tfCP.getText());
			datosFactura.setDomicilio(tfDomicilio.getText());
			datosFactura.setProvincia(tfProvincia.getText());
			datosFactura.setTelefono(tfTelefono.getText());
			datosFactura.setNombre(tfRazonSocial.getText());
			datosFactura.setPoblacion(tfPoblacion.getText());
			datosFactura.setLocalidad(tfLocalidad.getText());
			datosFactura.setPais(tfCodPais.getText());
			datosFactura.setBanco(tfBanco.getText());
			datosFactura.setBancoDomicilio(tfBancoDomicilio.getText());
			datosFactura.setBancoPoblacion(tfBancoPoblacion.getText());
			datosFactura.setCcc(tfBancoCCC.getText());

			if (cbTipoDocIdent.getSelectionModel().getSelectedItem() != null) {
				datosFactura.setTipoIdentificacion(cbTipoDocIdent.getSelectionModel().getSelectedItem().getCodTipoIden());
			}

			((TicketVenta) ticketManager.getTicket()).setDatosFacturacion(datosFactura);
			try {
				if (!ticketManager.getDocumentoActivo().getCodtipodocumento().equals(Documentos.FACTURA_COMPLETA)) {
					log.debug("establecerClienteFactura() - Se va a cambiar el documento activo. Documento activo actual " + ticketManager.getDocumentoActivo());
					ticketManager.setDocumentoActivo(sesion.getAplicacion().getDocumentos().getDocumento(ticketManager.getDocumentoActivo().getTipoDocumentoFacturaDirecta()));
				}
				getStage().close();
			}
			catch (DocumentoException ex) {
				log.error("No se pudo establecer el tipo documento factura", ex);
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Error al establecer el tipo de documento factura."), this.getStage());
			}
		}
	}
	
	
	  public void accionCancelar(){
		getDatos().put("cancela", "cancela");
		super.accionCancelar();

	 }
	

}

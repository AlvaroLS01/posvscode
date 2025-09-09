package com.comerzzia.bricodepot.pos.gui.ventas.tickets.clientes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.services.cliente.BricodepotClientesService;
import com.comerzzia.pos.core.gui.BackgroundTask;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.gui.ventas.tickets.clientes.ClienteGui;
import com.comerzzia.pos.gui.ventas.tickets.clientes.ConsultaClienteController;
import com.comerzzia.pos.gui.ventas.tickets.factura.paises.PaisesController;
import com.comerzzia.pos.gui.ventas.tickets.factura.paises.PaisesView;
import com.comerzzia.pos.persistence.clientes.ClienteBean;
import com.comerzzia.pos.persistence.paises.PaisBean;
import com.comerzzia.pos.persistence.tiposIdent.TiposIdentBean;
import com.comerzzia.pos.services.core.paises.PaisNotFoundException;
import com.comerzzia.pos.services.core.paises.PaisService;
import com.comerzzia.pos.services.core.paises.PaisServiceException;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.tiposIdent.TiposIdentNotFoundException;
import com.comerzzia.pos.services.core.tiposIdent.TiposIdentService;
import com.comerzzia.pos.services.core.tiposIdent.TiposIdentServiceException;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import rest.bean.cliente.BricodepotClienteBean;
import rest.client.clientes.BricodepotClientesRest;
import rest.client.clientes.BricodepotConsultarClienteRequestRest;
import rest.client.clientes.BricodepotResponseGetClientesRest;

@Component
@Primary
public class BricodepotConsultaClienteController extends ConsultaClienteController {

	private static final Logger log = Logger.getLogger(BricodepotConsultaClienteController.class.getName());

	@FXML
	protected TextField tfCodPais, tfDesPais;

	protected String codPais;

	@Autowired
	private PaisService paisService = SpringContext.getBean(PaisService.class);

	@Autowired
	private TiposIdentService tiposIdentService;

	@Autowired
	private VariablesServices variablesServices;
	@Autowired
	private Sesion sesion;

	@Override
	public void initializeComponents() {

		tfCodPais.focusedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				tfCodPais.setText(tfCodPais.getText().toUpperCase());
				if (!newValue ) {
					PaisBean pais = null;
					if(StringUtils.isNotBlank(tfCodPais.getText())) {
	                    try {
	                        pais = paisService.consultarCodPais(tfCodPais.getText());
	                    }
	                    catch (PaisNotFoundException e) {
	                    	log.debug("initializeComponents() - No se ha encontrado el país con código: " + tfCodPais.getText());
	                    }
	                    catch (PaisServiceException e) {
	                    	log.debug("initializeComponents() - Ha habido un error al buscar el país con código " + tfCodPais.getText() + ": " + e.getMessage());
	                    }
					}
                    
					if(pais != null) {
						tfDesPais.setText(pais.getDesPais());
					}
					else {
						tfDesPais.clear();
					}
					codPais = tfCodPais.getText();
					loadTiposIdentificacion();
				}
			}
		});

	}

	@Override
	public void initializeForm() {
		log.debug("initializeform()");

		try {
			
			tfDescripcion.clear();
			tfNumDocIdent.clear();
			
			String codigoPais = sesion.getAplicacion().getTienda().getCliente().getCodpais();
			tfCodPais.setText(codigoPais);

			String descripcionPais = paisService.consultarCodPais(codigoPais).getDesPais();
			tfDesPais.setText(descripcionPais);
			
			tiposIdent = FXCollections.observableArrayList();
			loadTiposIdentificacion();
			cbTipoDocIdent.setItems(tiposIdent);
		} catch (PaisNotFoundException | PaisServiceException e) {
			log.error(I18N.getTexto("initializeForm()- No se ha encontrado el pais:" + codPais), e);
		}

	}

	@FXML
	@Override
	public void accionBuscar() {
		log.trace("accionBuscar()");

		clientesBuscados.clear();
		lbError.setText("");

		frConsultaCliente.setNumDocIdent(tfNumDocIdent.getText());
		frConsultaCliente.setDescCliente(tfDescripcion.getText());

		if (validarFormularioConsultaCliente()) {
			String descripcion = tfDescripcion.getText();
			String ident = tfNumDocIdent.getText();
			String codTipoIdent = null;
			if (cbTipoDocIdent.getValue() != null) {
				codTipoIdent = cbTipoDocIdent.getValue().getCodTipoIden();
			} else {
				codTipoIdent = "";
			}
			/* BRICO-253 */
			String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
			String uidActividad = sesion.getAplicacion().getUidActividad();
			String codigoPais = tfCodPais.getText();

			if (StringUtils.isBlank(ident) || StringUtils.isBlank(codTipoIdent)) {
				log.debug("accionBuscar() - No se ha informado tipo documento y/o documento");

				lbError.setText(
						I18N.getTexto("Debe informar Tipo Documento y Documento para poder realizar la búsqueda."));
			} else {
				log.debug(
						"accionBuscar() - Se ha informado tipo documento y documento y se procede a realizar la consulta");
				new BricodepotBuscarClientesTask(codTipoIdent, ident, descripcion, uidActividad, codigoPais, apiKey)
						.start();
				/* fin BRICO-253 */
			}
		}
	}

	/*
	 * BRICO-253 Task similar a BuscarClientesTask de ConsultaClienteController
	 * hacían falta parámetros distintos para consultar los clientes
	 */
	class BricodepotBuscarClientesTask extends BackgroundTask<List<ClienteBean>> {

		private String codTipoIdent;
		private String cif;
		private String desCliente;
		private String uidActividad;
		private String codPais;
		private String apiKey;

		public BricodepotBuscarClientesTask(String codTipoIdent, String cif, String desCliente, String uidActividad,
				String codPais, String apiKey) {
			this.codTipoIdent = codTipoIdent;
			this.cif = cif;
			this.desCliente = desCliente;
			this.uidActividad = uidActividad;
			this.codPais = codPais;
			this.apiKey = apiKey;
		}

		// apiKey, uidActividad, descripcion, codTipoIdent, codPais, ident
		@Override
		protected List<ClienteBean> call() throws Exception {
			BricodepotConsultarClienteRequestRest consultaCliente = new BricodepotConsultarClienteRequestRest(apiKey,
					uidActividad, codTipoIdent, cif, desCliente, codPais);

			BricodepotResponseGetClientesRest response = BricodepotClientesRest.getClientePaisPOS(consultaCliente);
			List<ClienteBean> clientesPOS = new ArrayList<ClienteBean>();
			
			if (response != null && response.getClientes() != null && !response.getClientes().isEmpty()) {
				for (BricodepotClienteBean clienteBean : response.getClientes()) {
					clientesPOS.add(BricodepotClientesService.castClienteApiAPos(clienteBean));
				}
			}
			
			return clientesPOS;
		}

		@Override
		protected void failed() {
			log.error(getCMZException().getLocalizedMessage(), getCMZException());
			VentanaDialogoComponent.crearVentanaError(getStage(), getCMZException().getMessageI18N(),
					getCMZException());
			super.failed();
		}

		@Override
		protected void succeeded() {
			List<ClienteBean> clientesF = getValue();
			List<ClienteGui> clientesTabla = new ArrayList<ClienteGui>();

			for (ClienteBean c : clientesF) {
				clientesTabla.add(new ClienteGui(c));
			}

			if (clientesTabla.isEmpty()) {
				tbClientes.setPlaceholder(
						new Text(I18N.getTexto("No hay registros para los parámetros de búsqueda seleccionados")));
			} else {
				clientesBuscados.addAll(clientesTabla);
				tbClientes.getSelectionModel().selectFirst();
			}
			super.succeeded();
		}
	}

	@FXML
	public void accionBuscarPais() {
		getApplication().getMainView().showModalCentered(PaisesView.class, getDatos(), this.getStage());

		if (getDatos() != null && getDatos().containsKey(PaisesController.PARAMETRO_SALIDA_PAIS)) {
			PaisBean pais = (PaisBean) getDatos().get(PaisesController.PARAMETRO_SALIDA_PAIS);
			tfDesPais.setText(pais.getDesPais());
			tfCodPais.setText(pais.getCodPais());
			codPais = pais.getCodPais().toUpperCase();
			loadTiposIdentificacion();
			cbTipoDocIdent.getSelectionModel().selectFirst();
		}
	}

	protected void loadTiposIdentificacion() {
		try {
			cbTipoDocIdent.getSelectionModel().clearSelection();
			tiposIdent.clear();
			String codPais = "";
			if (StringUtils.isEmpty(tfCodPais.getText())) {
				codPais = sesion.getAplicacion().getTienda().getCliente().getCodpais();
			} else {
				codPais = tfCodPais.getText();
			}
			List<TiposIdentBean> tiposIdentificacion = tiposIdentService.consultarTiposIdent(null, true, codPais);
			if (tiposIdentificacion.isEmpty()) {
				// Añadimos elemento vacío
				tiposIdent.add(new TiposIdentBean());
			} else {
				tiposIdent.addAll(tiposIdentificacion);
			}
		} catch (TiposIdentNotFoundException ex) {
		} catch (TiposIdentServiceException ex) {
			log.error("Error consultando los tipos de identificación.", ex);
			VentanaDialogoComponent.crearVentanaAviso(
					I18N.getTexto("Error consultando los documentos de identificación de la tienda."), this.getStage());
		} catch (Exception ex) {
			log.error("Se produjo un error en el tratamiento de los tipos de identificacion", ex);
		}
	}
}

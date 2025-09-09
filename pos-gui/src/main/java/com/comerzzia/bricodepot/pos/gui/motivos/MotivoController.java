package com.comerzzia.bricodepot.pos.gui.motivos;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.comerzzia.bricodepot.pos.persistence.motivos.Motivo;
import com.comerzzia.bricodepot.pos.persistence.motivos.MotivoExample;
import com.comerzzia.bricodepot.pos.persistence.motivos.MotivoMapper;
import com.comerzzia.bricodepot.pos.services.ticket.lineas.BricodepotLineaTicket;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.services.ticket.lineas.LineaTicket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

@Controller
public class MotivoController extends WindowController {

	private static final Logger log = Logger.getLogger(MotivoController.class.getName());

	@Autowired
	protected MotivoMapper mapper;

	@FXML
	protected ComboBox<Motivo> cbMotivo;

	@FXML
	protected Label lbCodigo;
	@FXML
	protected Label lbArticulo;
	@FXML
	protected Label lbCantidad;
	@FXML
	protected Label lbPrecio;
	@FXML
	protected TextArea taComentario;

	protected BricodepotLineaTicket lineaOriginal;
	
	public static final String PARAMETRO_TIPO_MOTIVO = "tipoMotivo";
	public static final String PARAMETRO_MOTIVO = "motivo";
	public static final String PARAMETRO_LINEA = "linea";
	public static final Integer CODIGO_TIPO_DEVOLUCION = 1;
	public static final Integer CODIGO_TIPO_CAMBIO_PRECIO = 2;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

	}

	@Override
	public void initializeComponents() throws InitializeGuiException {

	}

	@Override
	public void initializeFocus() {

	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		Integer tipoMotivo = (Integer) getDatos().get(PARAMETRO_TIPO_MOTIVO);
		MotivoExample example = new MotivoExample();
		example.createCriteria().andCodigoTipoEqualTo(String.valueOf(tipoMotivo));

		List<Motivo> list = mapper.selectByExample(example);
		ObservableList<Motivo> ob = FXCollections.observableArrayList(list);
		cbMotivo.setConverter(new StringConverter<Motivo>(){

			@Override
			public String toString(Motivo arg0) {
				return arg0.getDescripcion();
			}

			@Override
			public Motivo fromString(String arg0) {
				return null;
			}
		});

		LineaTicket lineaSeleccionada = (LineaTicket) getDatos().get(PARAMETRO_LINEA);
		setLinea(lineaSeleccionada);
		cbMotivo.setItems(ob);
		lbCodigo.setText(lineaOriginal.getCodArticulo());
		lbArticulo.setText(lineaOriginal.getDesArticulo());
		lbCantidad.setText(lineaOriginal.getCantidadAsString());
		lbPrecio.setText(lineaOriginal.getPrecioTotalConDtoAsString());
		taComentario.setText("");
	}

	private void setLinea(LineaTicket linea) {
		this.lineaOriginal = (BricodepotLineaTicket) linea;
	}

	@FXML
	public void accionGuardar() {
		log.debug("accionGuardar() - Guardando motivo en la línea");
		Motivo mot = cbMotivo.getValue();
		if (mot != null) {
			mot.setComentario(taComentario.getText());
			mot.setPrecioSinDtoOriginal(lineaOriginal.getPrecioTotalTarifaOrigen());
			mot.setPrecioSinDtoAplicado(lineaOriginal.getPrecioTotalSinDto());
			getDatos().put(PARAMETRO_MOTIVO, mot);
			getStage().close();
		}
		else {
			taComentario.setText("");
			log.debug("accionGuardar() - No se ha seleccionado ningún motivo");
			getStage().close();
		}
	}

}

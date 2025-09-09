package com.comerzzia.bricodepot.pos.gui.ventas.devoluciones.mediosPago;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.core.gui.tablas.celdas.CellFactoryBuilder;
import com.comerzzia.pos.gui.ventas.devoluciones.IntroduccionArticulosController;
import com.comerzzia.pos.services.ticket.TicketVentaAbono;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

@Controller
public class BricoDepotVerMediosPagoController extends WindowController {

	private static final Logger log = Logger.getLogger(BricoDepotVerMediosPagoController.class.getName());
	@FXML
	protected TableView<BricoDepotMedioPagoGui> tbMedios;
	@FXML
	protected TableColumn<BricoDepotMedioPagoGui, String> tcMedio;
	protected ObservableList<BricoDepotMedioPagoGui> lineas;
	@FXML
	protected Label lbIdTicket;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initializeComponents() throws InitializeGuiException {
		tcMedio.setCellFactory(CellFactoryBuilder.createCellRendererCelda("tbMedios", "tcMedio", null, CellFactoryBuilder.ESTILO_ALINEACION_IZQ));

		// Asignamos las lineas a la tabla
		lineas = FXCollections.observableList(new ArrayList<BricoDepotMedioPagoGui>());
		tbMedios.setItems(lineas);

		// Definimos un factory para cada celda para aumentar el rendimiento
		tcMedio.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<BricoDepotMedioPagoGui, String>, ObservableValue<String>>(){

			@Override
			public ObservableValue<String> call(CellDataFeatures<BricoDepotMedioPagoGui, String> cdf) {
				return cdf.getValue().getDescPago();
			}
		});
	}

	@Override
	public void initializeFocus() {

	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		log.debug("initializeForm() - Inicializando formulario...");
		List<BricoDepotMedioPagoGui> lineasRes = new ArrayList<BricoDepotMedioPagoGui>();

		lineas = FXCollections.observableList(new ArrayList<BricoDepotMedioPagoGui>());
		TicketVentaAbono ticketOrigen = (TicketVentaAbono) getDatos().get(IntroduccionArticulosController.TICKET_KEY);
		List<String> mediosPago = ticketOrigen.getPagos().stream().map(x -> x.getDesMedioPago()).collect(Collectors.toList());
		for (String medio : mediosPago) {
			BricoDepotMedioPagoGui linea = new BricoDepotMedioPagoGui(medio);
			lineasRes.add(linea);
		}
		lineas.addAll(lineasRes);
		tbMedios.setItems(lineas);
		lbIdTicket.setText(ticketOrigen.getCabecera().getCodTicket());

	}

	@FXML
	public void accionAceptar() {
		getStage().close();
	}

}

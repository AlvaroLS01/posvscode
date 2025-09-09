package com.comerzzia.bricodepot.pos.gui.ventas.devoluciones.mediosPago;

import javafx.beans.property.SimpleStringProperty;

public class BricoDepotMedioPagoGui {

	private SimpleStringProperty descPago;

	public BricoDepotMedioPagoGui(String descMedioPago) {
		this.descPago = new SimpleStringProperty(descMedioPago);
	}

	public SimpleStringProperty getDescPago() {
		return descPago;
	}

	public void setDescPago(SimpleStringProperty descPago) {
		this.descPago = descPago;
	}

}

package com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.firma;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.Controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

@Component
public class PantallaSecundariaFirmaClienteController extends Controller {

	protected static final Logger log = Logger.getLogger(PantallaSecundariaFirmaClienteController.class.getName());

	private GraphicsContext gcB, gcF;
	double startX, startY, lastX, lastY, oldX, oldY;
	double hg;

	@FXML
	private Canvas TheCanvas, canvasGo;

	protected BricodepotVisorPantallaSecundaria visor;

	@Override
	public void initializeComponents() throws InitializeGuiException {
	}

	@Override
	public void initializeForm() throws InitializeGuiException {

	}

	@Override
	public void initializeFocus() {
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		gcB = TheCanvas.getGraphicsContext2D();
		gcF = canvasGo.getGraphicsContext2D();
	}

	@FXML
	private void onMousePressedListener(MouseEvent e) {
		this.startX = e.getX();
		this.startY = e.getY();
		this.oldX = e.getX();
		this.oldY = e.getY();
	}

	@FXML
	private void onMouseDraggedListener(MouseEvent e) {
		this.lastX = e.getX();
		this.lastY = e.getY();

		freeDrawing();
	}

	@FXML
	private void onMouseReleaseListener(MouseEvent e) {
	}

	@FXML
	private void onMouseExitedListener(MouseEvent event) {
	}

	private void freeDrawing() {
		gcB.setStroke(Color.BLACK);
		gcB.strokeLine(oldX, oldY, lastX, lastY);
		oldX = lastX;
		oldY = lastY;
	}

	public void refrescarDatosPantalla(BricodepotVisorPantallaSecundaria visor) {
		log.debug("refrescarDatosPantalla()");

		this.visor = visor;

		accionBorrarFirma();
	}

	@FXML
	public void accionFirmar() {
		log.debug("accionFirmar()");
		SnapshotParameters parameters = new SnapshotParameters();
		WritableImage wi = new WritableImage(800, 480);

		WritableImage snapshot = TheCanvas.snapshot(parameters, wi);
		try {
			BufferedImage bImage = SwingFXUtils.fromFXImage(snapshot, null);
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			ImageIO.write(bImage, "png", s);
			byte[] res = s.toByteArray();
			BricodepotVisorPantallaSecundaria.setFirma(res);
			s.close();
		}
		catch (IOException e) {
			log.debug("accionFirmar() - Ha ocurrido un error al realizar el parse de WritableImage a byte[]." + e.getMessage());
		}

		visor.confirmacionValidacionCliente();
	}

	@FXML
	public void accionBorrarFirma() {
		log.debug("accionBorrarFirma()");

		gcB.clearRect(0, 0, TheCanvas.getWidth(), TheCanvas.getHeight());
		gcF.clearRect(0, 0, TheCanvas.getWidth(), TheCanvas.getHeight());
	}
}

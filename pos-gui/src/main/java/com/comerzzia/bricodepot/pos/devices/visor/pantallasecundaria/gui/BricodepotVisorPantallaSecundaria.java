package com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui;

import java.awt.Rectangle;
import java.net.URL;
import java.util.Date;

import javax.swing.JFrame;

import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.email.PantallaSecundariaEmailClienteController;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.email.PantallaSecundariaEmailClienteView;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.firma.PantallaSecundariaFirmaClienteController;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.firma.PantallaSecundariaFirmaClienteView;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.info.PantallaSecundariaInfoClienteController;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.info.PantallaSecundariaInfoClienteView;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.leyproteccion.PantallaSecundariaLeyProteccionDatosClienteController;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.leyproteccion.PantallaSecundariaLeyProteccionDatosClienteView;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.consentimientos.PantallaSecundariaConsentimientosFidelizadoController;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.consentimientos.PantallaSecundariaConsentimientosFidelizadoView;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.firma.PantallaSecundariaFirmaFidelizadoController;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.firma.PantallaSecundariaFirmaFidelizadoView;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.info.PantallaSecundariaInfoFidelizadoController;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.info.PantallaSecundariaInfoFidelizadoView;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.leyproteccion.PantallaSecundariaLeyProteccionDatosFidelizadoController;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.leyproteccion.PantallaSecundariaLeyProteccionDatosFidelizadoView;
import com.comerzzia.bricodepot.pos.gui.mantenimientos.clientes.BricodepotMantenimientoClienteController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.email.EmailController;
import com.comerzzia.bricodepot.pos.services.cliente.ProcesoValidacionClienteListener;
import com.comerzzia.bricodepot.pos.services.fidelizado.ProcesoValidacionFidelizadoListener;
import com.comerzzia.core.model.notificaciones.Notificacion;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.POSApplication;
import com.comerzzia.pos.dispositivo.visor.VisorPantallaSecundaria;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.datosgenerales.PaneDatosGeneralesController;
import com.comerzzia.pos.services.notificaciones.Notificaciones;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;

import org.apache.log4j.Logger;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;

public class BricodepotVisorPantallaSecundaria extends VisorPantallaSecundaria {

	protected static final Logger log = Logger.getLogger(BricodepotVisorPantallaSecundaria.class.getName());

	public static int MODO_INFO_DATOS_FIDELIZADO = 5;
	public static int MODO_LEY_PROTECCION_DATOS_FIDELIZADO = 6;
	public static int MODO_FIRMA_FIDELIZADO = 7;
	public static int MODO_CONSENTIMIENTOS_FIDELIZADO = 8;

	public static int MODO_INFO_DATOS_CLIENTE = 9;
	public static int MODO_LEY_PROTECCION_DATOS_CLIENTE = 10;
	public static int MODO_FIRMA_CLIENTE = 11;
	public static int MODO_CONSENTIMIENTOS_CLIENTE = 12;
	public static int MODO_EMAIL_CLIENTE = 555;
	
	protected PantallaSecundariaInfoFidelizadoView infoFidelizadoView;
	protected PantallaSecundariaLeyProteccionDatosFidelizadoView leyProteccionDatosFidelizadoView;
	protected PantallaSecundariaFirmaFidelizadoView firmaFidelizadoView;
	protected PantallaSecundariaConsentimientosFidelizadoView consentimientosFidelizadoView;

	protected PaneDatosGeneralesController paneDatosGenerales;

	protected PantallaSecundariaInfoClienteView infoClienteView;
	protected PantallaSecundariaLeyProteccionDatosClienteView leyProteccionDatosClienteView;
	protected PantallaSecundariaFirmaClienteView firmaClienteView;
	protected PantallaSecundariaEmailClienteView emailClienteView;
	
	protected BricodepotMantenimientoClienteController mantenimientoCliente;

	protected static byte[] firma;

	protected ProcesoValidacionFidelizadoListener listenerFidelizadoOK;
	protected ProcesoValidacionClienteListener listenerClienteOK;

	public void inicializar() {
		// crear ventana Swing
		Rectangle r = new Rectangle();
		r.setLocation(Double.valueOf(segundaPantalla.getBounds().getMinX()).intValue(), Double.valueOf(segundaPantalla.getBounds().getMinY()).intValue());
		r.setSize(Double.valueOf(segundaPantalla.getBounds().getWidth()).intValue(), Double.valueOf(segundaPantalla.getBounds().getHeight()).intValue());

		frame = new JFrame();
		frame.setFocusableWindowState(false);
		frame.setBounds(r);
		frame.setUndecorated(true);
		frame.setEnabled(true);
		frame.setFocusable(true);

		webView = new WebView();
		webView.setDisable(true);
		WebEngine webEngine = webView.getEngine();
		URL url = getClass().getResource("/" + web);
		webEngine.load(url.toExternalForm());
		if (scene == null) {
			scene = new Scene(webView, segundaPantalla.getBounds().getWidth(), segundaPantalla.getBounds().getHeight());
		}
		POSApplication.getInstance().addBaseCSS(scene);

		// asignar vista/escena a ventana swing
		JFXPanel fxPanel = new JFXPanel();
		fxPanel.setFocusable(true);
		fxPanel.setEnabled(true);
		frame.add(fxPanel);

		fxPanel.setScene(scene);

		frame.setVisible(true);
	}

	public void modoInfoFidelizado(PaneDatosGeneralesController paneDatosGenerales) {
		log.debug("modoInfoFidelizado() - Inicio de la pantalla de informacion de fidelizados");
		try {
			this.paneDatosGenerales = paneDatosGenerales;

			if (infoFidelizadoView == null) {
				if (Screen.getScreens().size() > 1) {
					if (getEstado() == ENCENDIDO) {
						try {
							setModo(BricodepotVisorPantallaSecundaria.MODO_INFO_DATOS_FIDELIZADO);
							infoFidelizadoView = SpringContext.getBean(PantallaSecundariaInfoFidelizadoView.class);
							infoFidelizadoView.loadAndInitialize();
							scene.setRoot(infoFidelizadoView.getViewNode());
							// stage.show();
							((PantallaSecundariaInfoFidelizadoController) infoFidelizadoView.getController()).refrescarDatosPantalla(this, paneDatosGenerales);
						}
						catch (InitializeGuiException e) {
							log.error("modoInfoFidelizado() Error cargando vista:" + e.getMessage(), e);
						}
					}
					else {
						scene.setRoot(infoFidelizadoView.getViewNode());
						setModo(BricodepotVisorPantallaSecundaria.MODO_INFO_DATOS_FIDELIZADO);
						((PantallaSecundariaInfoFidelizadoController) infoFidelizadoView.getController()).refrescarDatosPantalla(this, paneDatosGenerales);
					}
				}
				else {
					if (getEstado() == ENCENDIDO) {
						Notificaciones.get().addNotification(new Notificacion(I18N.getTexto("Compruebe que el segundo monitor está encendido."), Notificacion.Tipo.WARN, new Date()));
					}
					setEstado(APAGADO);
				}
			}
			else {
				setModo(BricodepotVisorPantallaSecundaria.MODO_INFO_DATOS_FIDELIZADO);
				scene.setRoot(infoFidelizadoView.getViewNode());
				((PantallaSecundariaInfoFidelizadoController) infoFidelizadoView.getController()).refrescarDatosPantalla(this, paneDatosGenerales);
			}
		}
		catch (Exception e) {
			log.error("modoInfoFidelizado() - Ha habido un error al mostrar la pantalla de información de cupones en la pantalla secundaria: " + e.getMessage(), e);
		}
	}

	public void modoInfoCliente(BricodepotMantenimientoClienteController mantenimientoCliente) {
		log.debug("modoInfoCliente() - Inicio de la pantalla de informacion de clientes");
		try {
			this.mantenimientoCliente = mantenimientoCliente;

			if (infoClienteView == null) {
				if (Screen.getScreens().size() > 1) {
					if (getEstado() == ENCENDIDO) {
						try {
							setModo(MODO_INFO_DATOS_CLIENTE);
							infoClienteView = SpringContext.getBean(PantallaSecundariaInfoClienteView.class);
							infoClienteView.loadAndInitialize();
							scene.setRoot(infoClienteView.getViewNode());
							// stage.show();
							((PantallaSecundariaInfoClienteController) infoClienteView.getController()).refrescarDatosPantalla(this, mantenimientoCliente);
						}
						catch (InitializeGuiException e) {
							log.error("modoInfoCliente() Error cargando vista:" + e.getMessage(), e);
						}
					}
					else {
						scene.setRoot(infoClienteView.getViewNode());
						setModo(MODO_INFO_DATOS_CLIENTE);
						((PantallaSecundariaInfoClienteController) infoClienteView.getController()).refrescarDatosPantalla(this, mantenimientoCliente);
					}
				}
				else {
					if (getEstado() == ENCENDIDO) {
						Notificaciones.get().addNotification(new Notificacion(I18N.getTexto("Compruebe que el segundo monitor está encendido."), Notificacion.Tipo.WARN, new Date()));
					}
					setEstado(APAGADO);
				}
			}
			else {
				setModo(MODO_INFO_DATOS_CLIENTE);
				scene.setRoot(infoClienteView.getViewNode());
				((PantallaSecundariaInfoClienteController) infoClienteView.getController()).refrescarDatosPantalla(this, mantenimientoCliente);
			}
		}
		catch (Exception e) {
			log.error("modoInfoCliente() - Ha habido un error al mostrar la pantalla de confirmación de cliente en la pantalla secundaria: " + e.getMessage(), e);
		}
	}

	public void modoLeyProteccionDatosFidelizado() {
		log.debug("modoLeyProteccionDatosFidelizado() - Inicio de la pantalla de protección de datos");
		try {
			if (leyProteccionDatosFidelizadoView == null) {
				if (Screen.getScreens().size() > 1) {
					if (getEstado() == ENCENDIDO) {
						try {
							setModo(BricodepotVisorPantallaSecundaria.MODO_LEY_PROTECCION_DATOS_FIDELIZADO);
							leyProteccionDatosFidelizadoView = SpringContext.getBean(PantallaSecundariaLeyProteccionDatosFidelizadoView.class);
							leyProteccionDatosFidelizadoView.loadAndInitialize();
							scene.setRoot(leyProteccionDatosFidelizadoView.getViewNode());
							// stage.show();
							((PantallaSecundariaLeyProteccionDatosFidelizadoController) leyProteccionDatosFidelizadoView.getController()).refrescarDatosPantalla(this);
						}
						catch (InitializeGuiException e) {
							log.error("modoLeyProteccionDatosFidelizado() Error cargando vista:" + e.getMessage(), e);
						}
					}
					else {
						setModo(BricodepotVisorPantallaSecundaria.MODO_LEY_PROTECCION_DATOS_FIDELIZADO);
						scene.setRoot(leyProteccionDatosFidelizadoView.getViewNode());
						((PantallaSecundariaLeyProteccionDatosFidelizadoController) leyProteccionDatosFidelizadoView.getController()).refrescarDatosPantalla(this);
					}
				}
				else {
					if (getEstado() == ENCENDIDO) {
						Notificaciones.get().addNotification(new Notificacion(I18N.getTexto("Compruebe que el segundo monitor está encendido."), Notificacion.Tipo.WARN, new Date()));
					}
					setEstado(APAGADO);
				}
			}
			else {
				setModo(BricodepotVisorPantallaSecundaria.MODO_LEY_PROTECCION_DATOS_FIDELIZADO);
				scene.setRoot(leyProteccionDatosFidelizadoView.getViewNode());
				((PantallaSecundariaLeyProteccionDatosFidelizadoController) leyProteccionDatosFidelizadoView.getController()).refrescarDatosPantalla(this);
			}
		}
		catch (Exception e) {
			log.error("modoLeyProteccionDatosFidelizado() - Ha habido un error al mostrar la pantalla de información de cupones en la pantalla secundaria: " + e.getMessage(), e);
		}
	}

	public void modoLeyProteccionDatosCliente() {
		log.debug("modoLeyProteccionDatosCliente() - Inicio de la pantalla de protección de datos");
		try {
			if (leyProteccionDatosClienteView == null) {
				if (Screen.getScreens().size() > 1) {
					if (getEstado() == ENCENDIDO) {
						try {
							setModo(BricodepotVisorPantallaSecundaria.MODO_LEY_PROTECCION_DATOS_CLIENTE);
							leyProteccionDatosClienteView = SpringContext.getBean(PantallaSecundariaLeyProteccionDatosClienteView.class);
							leyProteccionDatosClienteView.loadAndInitialize();
							scene.setRoot(leyProteccionDatosClienteView.getViewNode());
							// stage.show();
							((PantallaSecundariaLeyProteccionDatosClienteController) leyProteccionDatosClienteView.getController()).refrescarDatosPantalla(this);
						}
						catch (InitializeGuiException e) {
							log.error("modoLeyProteccionDatosCliente() Error cargando vista:" + e.getMessage(), e);
						}
					}
					else {
						setModo(BricodepotVisorPantallaSecundaria.MODO_LEY_PROTECCION_DATOS_CLIENTE);
						scene.setRoot(leyProteccionDatosClienteView.getViewNode());
						((PantallaSecundariaLeyProteccionDatosClienteController) leyProteccionDatosClienteView.getController()).refrescarDatosPantalla(this);
					}
				}
				else {
					if (getEstado() == ENCENDIDO) {
						Notificaciones.get().addNotification(new Notificacion(I18N.getTexto("Compruebe que el segundo monitor está encendido."), Notificacion.Tipo.WARN, new Date()));
					}
					setEstado(APAGADO);
				}
			}
			else {
				setModo(BricodepotVisorPantallaSecundaria.MODO_LEY_PROTECCION_DATOS_CLIENTE);
				scene.setRoot(leyProteccionDatosClienteView.getViewNode());
				((PantallaSecundariaLeyProteccionDatosClienteController) leyProteccionDatosClienteView.getController()).refrescarDatosPantalla(this);
			}
		}
		catch (Exception e) {
			log.error("modoLeyProteccionDatosCliente() - Ha habido un error al mostrar la pantalla de información de cupones en la pantalla secundaria: " + e.getMessage(), e);
		}
	}

	public void modoFirmaFidelizado() {
		log.debug("modoFirmaFidelizado() - Inicio de la pantalla de firma");
		try {
			if (firmaFidelizadoView == null) {
				if (Screen.getScreens().size() > 1) {
					if (getEstado() == ENCENDIDO) {
						try {
							setModo(BricodepotVisorPantallaSecundaria.MODO_FIRMA_FIDELIZADO);
							firmaFidelizadoView = SpringContext.getBean(PantallaSecundariaFirmaFidelizadoView.class);
							firmaFidelizadoView.loadAndInitialize();
							scene.setRoot(firmaFidelizadoView.getViewNode());
							// stage.show();
							((PantallaSecundariaFirmaFidelizadoController) firmaFidelizadoView.getController()).refrescarDatosPantalla(this);
						}
						catch (InitializeGuiException e) {
							log.error("modoFirmaFidelizado() Error cargando vista:" + e.getMessage(), e);
						}
					}
					else {
						setModo(BricodepotVisorPantallaSecundaria.MODO_LEY_PROTECCION_DATOS_FIDELIZADO);
						scene.setRoot(firmaFidelizadoView.getViewNode());
						((PantallaSecundariaFirmaFidelizadoController) firmaFidelizadoView.getController()).refrescarDatosPantalla(this);
					}
				}
				else {
					if (getEstado() == ENCENDIDO) {
						Notificaciones.get().addNotification(new Notificacion(I18N.getTexto("Compruebe que el segundo monitor está encendido."), Notificacion.Tipo.WARN, new Date()));
					}
					setEstado(APAGADO);
				}
			}
			else {
				setModo(BricodepotVisorPantallaSecundaria.MODO_FIRMA_FIDELIZADO);
				scene.setRoot(firmaFidelizadoView.getViewNode());
				((PantallaSecundariaFirmaFidelizadoController) firmaFidelizadoView.getController()).refrescarDatosPantalla(this);
			}
		}
		catch (Exception e) {
			log.error("modoFirmaFidelizado() - Ha habido un error al mostrar la pantalla de firma: " + e.getMessage(), e);
		}
	}

	public void modoFirmaCliente() {
		log.debug("modoFirmaCliente() - Inicio de la pantalla de firma");
		try {
			if (firmaClienteView == null) {
				if (Screen.getScreens().size() > 1) {
					if (getEstado() == ENCENDIDO) {
						try {
							setModo(MODO_FIRMA_CLIENTE);
							firmaClienteView = SpringContext.getBean(PantallaSecundariaFirmaClienteView.class);
							firmaClienteView.loadAndInitialize();
							scene.setRoot(firmaClienteView.getViewNode());
							// stage.show();
							((PantallaSecundariaFirmaClienteController) firmaClienteView.getController()).refrescarDatosPantalla(this);
						}
						catch (InitializeGuiException e) {
							log.error("modoFirmaCliente() Error cargando vista:" + e.getMessage(), e);
						}
					}
					else {
						setModo(BricodepotVisorPantallaSecundaria.MODO_LEY_PROTECCION_DATOS_CLIENTE);
						scene.setRoot(firmaClienteView.getViewNode());
						((PantallaSecundariaFirmaClienteController) firmaClienteView.getController()).refrescarDatosPantalla(this);
					}
				}
				else {
					if (getEstado() == ENCENDIDO) {
						Notificaciones.get().addNotification(new Notificacion(I18N.getTexto("Compruebe que el segundo monitor está encendido."), Notificacion.Tipo.WARN, new Date()));
					}
					setEstado(APAGADO);
				}
			}
			else {
				setModo(BricodepotVisorPantallaSecundaria.MODO_FIRMA_CLIENTE);
				scene.setRoot(firmaClienteView.getViewNode());
				((PantallaSecundariaFirmaClienteController) firmaClienteView.getController()).refrescarDatosPantalla(this);
			}
		}
		catch (Exception e) {
			log.error("modoFirmaCliente() - Ha habido un error al mostrar la pantalla de firma: " + e.getMessage(), e);
		}
	}

	public void modoConsentimientos() {
		log.debug("modoConsentimientos() - Inicio de la pantalla de consentimientos");
		try {
			if (consentimientosFidelizadoView == null) {
				if (Screen.getScreens().size() > 1) {
					if (getEstado() == ENCENDIDO) {
						try {
							setModo(BricodepotVisorPantallaSecundaria.MODO_CONSENTIMIENTOS_FIDELIZADO);
							consentimientosFidelizadoView = SpringContext.getBean(PantallaSecundariaConsentimientosFidelizadoView.class);
							consentimientosFidelizadoView.loadAndInitialize();
							scene.setRoot(consentimientosFidelizadoView.getViewNode());
							// stage.show();
							((PantallaSecundariaConsentimientosFidelizadoController) consentimientosFidelizadoView.getController()).refrescarDatosPantalla(this);
						}
						catch (InitializeGuiException e) {
							log.error("modoConsentimientos() Error cargando vista:" + e.getMessage(), e);
						}
					}
					else {
						setModo(BricodepotVisorPantallaSecundaria.MODO_CONSENTIMIENTOS_FIDELIZADO);
						scene.setRoot(consentimientosFidelizadoView.getViewNode());
						((PantallaSecundariaConsentimientosFidelizadoController) consentimientosFidelizadoView.getController()).refrescarDatosPantalla(this);
					}
				}
				else {
					if (getEstado() == ENCENDIDO) {
						Notificaciones.get().addNotification(new Notificacion(I18N.getTexto("Compruebe que el segundo monitor está encendido."), Notificacion.Tipo.WARN, new Date()));
					}
					setEstado(APAGADO);
				}
			}
			else {
				setModo(BricodepotVisorPantallaSecundaria.MODO_CONSENTIMIENTOS_FIDELIZADO);
				scene.setRoot(consentimientosFidelizadoView.getViewNode());
				((PantallaSecundariaConsentimientosFidelizadoController) consentimientosFidelizadoView.getController()).refrescarDatosPantalla(this);
			}
		}
		catch (Exception e) {
			log.error("modoConsentimientos() - Ha habido un error al mostrar la pantalla de información de cupones en la pantalla secundaria: " + e.getMessage(), e);
		}
	}
	
	public void modoEmailCliente(EmailController correoController) {
		try {
			if (emailClienteView == null) {
				if (Screen.getScreens().size() > 1) {
					if (getEstado() == ENCENDIDO) {
						try {
							setModo(BricodepotVisorPantallaSecundaria.MODO_EMAIL_CLIENTE);
							emailClienteView = SpringContext.getBean(PantallaSecundariaEmailClienteView.class);
							emailClienteView.loadAndInitialize();
							scene.setRoot(emailClienteView.getViewNode());
							// stage.show();
							((PantallaSecundariaEmailClienteController) emailClienteView.getController()).refrescarDatosPantalla(this, correoController);
						}
						catch (InitializeGuiException e) {
							log.error("modoEmailCliente() Error cargando vista:" + e.getMessage(), e);
						}
					}
					else {
						scene.setRoot(emailClienteView.getViewNode());
						setModo(BricodepotVisorPantallaSecundaria.MODO_EMAIL_CLIENTE);
						((PantallaSecundariaEmailClienteController) emailClienteView.getController()).refrescarDatosPantalla(this, correoController);
					}
				}
				else {
					if (getEstado() == ENCENDIDO) {
						Notificaciones.get().addNotification(new Notificacion(I18N.getTexto("Compruebe que el segundo monitor está encendido."), Notificacion.Tipo.WARN, new Date()));
					}
					setEstado(APAGADO);
				}
			}
			else {
				setModo(BricodepotVisorPantallaSecundaria.MODO_EMAIL_CLIENTE);
				scene.setRoot(emailClienteView.getViewNode());
				((PantallaSecundariaEmailClienteController) emailClienteView.getController()).refrescarDatosPantalla(this, correoController);
			}
		}
		catch (Exception e) {
			log.error("modoEmailCliente() - Ha habido un error al mostrar la pantalla de confirmación de cliente en la pantalla secundaria: " + e.getMessage(), e);
		}
		
	}	

	public void confirmacionValidacionFidelizado(String consentimiento) {
		log.debug("confirmacionValidacionFidelizado()");

		if (consentimiento.equals("SI")) {
			paneDatosGenerales.getChNotifEmail().setSelected(true);
			paneDatosGenerales.getChNotifMovil().setSelected(true);
		}
		else {
			paneDatosGenerales.getChNotifEmail().setSelected(false);
			paneDatosGenerales.getChNotifMovil().setSelected(false);
		}

		log.debug("confirmacionValidacionFidelizado() - Se cambia el modo al modoEspera");
		modoEspera();

		listenerFidelizadoOK.procesoValidacionOK();
	}

	public void confirmacionValidacionCliente() {
		log.debug("confirmacionValidacionCliente()");

		log.debug("confirmacionValidacionCliente() - Se cambia el modo al modoEspera");
		modoEspera();

		listenerClienteOK.procesoValidacionOK();
	}

	public static byte[] getFirma() {
		return firma;
	}

	public static void setFirma(byte[] firma) {
		BricodepotVisorPantallaSecundaria.firma = firma;
	}

	public ProcesoValidacionFidelizadoListener getListenerFidelizadoOK() {
		return listenerFidelizadoOK;
	}

	public void setListenerFidelizadoOK(ProcesoValidacionFidelizadoListener listenerFidelizadoOK) {
		this.listenerFidelizadoOK = listenerFidelizadoOK;
	}

	public ProcesoValidacionClienteListener getListenerClienteOK() {
		return listenerClienteOK;
	}

	public void setListenerClienteOK(ProcesoValidacionClienteListener listenerClienteOK) {
		this.listenerClienteOK = listenerClienteOK;
	}
}

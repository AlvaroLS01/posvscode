package com.comerzzia.bricodepot.pos.gui.ventas.conversion.datosadicionales;

import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.gui.ventas.tickets.BricodepotTicketManager;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotCabeceraTicket;
import com.comerzzia.core.servicios.contadores.ServicioContadoresImpl;
import com.comerzzia.core.servicios.empresas.EmpresaException;
import com.comerzzia.core.servicios.sesion.DatosSesionBean;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.datepicker.DatePicker;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.services.core.documentos.DocumentoException;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@SuppressWarnings("deprecation")
@Component
public class PantallaDatosAdicionalesController extends WindowController{
	
	private static final Logger log = Logger.getLogger(PantallaDatosAdicionalesController.class.getName());
	
	@FXML
	protected TextField tfFecha, tfHora	, tfMinutos, tfFactura;
	@FXML
	protected DatePicker dpFecha;

	@FXML
	protected Label lbTitulo, lbFecha, labelHora, lbFactura, lbMinutos;
	
	@FXML
	protected Button btAceptar, btCancelar;
	
	protected BricodepotTicketManager ticketManager;

	@Autowired
	protected Sesion sesion;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		configurarTextField(tfHora,0, 23);
		configurarTextField(tfMinutos,0, 59);
	}
	public void limpiarFormulario() {
		tfHora.setText("");
		tfMinutos.setText("");
		tfFactura.setText("");
		dpFecha.getTextField().setText("");
	}
	public void configurarTextField(TextField textField, int min, int max) {

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }
            if (newText.matches("[0-9]*")) {
                if (newText.length() <= 2) {
                    int value = Integer.parseInt(newText);
                    if (value >= min && value <= max) {
                        return change;
                    }
                }
            }

            return null; 
        };

        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        textField.setTextFormatter(textFormatter);
	}
	@Override
	public void initializeComponents() throws InitializeGuiException {
		registrarAccionCerrarVentanaEscape();		
		lbFecha.setText(I18N.getTexto("Fecha:"));
		lbMinutos.setText(I18N.getTexto("Minutos:"));
		lbFactura.setText(I18N.getTexto("Factura:"));
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		initializeFocus();
		ticketManager = (BricodepotTicketManager) getDatos().get(FacturacionArticulosController.TICKET_KEY);		
	}
	@Override
	public void initializeFocus() {
		dpFecha.getTextField().setDisable(true);
	}

	@FXML
	public void accionAceptarIntro(KeyEvent e) throws Exception {
		log.debug("accionAceptarIntro()");

		if (e.getCode() == KeyCode.ENTER) {
			accionAceptar();
			e.consume();
		}
	}
	
	public void accionTeclas(KeyEvent event) throws Exception {
		if (event.getCode() == KeyCode.ESCAPE) {
			accionCancelar();
		}
		else if (event.getCode() == KeyCode.ENTER) {
			accionAceptar();
		}
	}

	public Date combinarFechaYHora(Date fecha, String horas, String minutos) throws ParseException {
		if (Integer.valueOf(horas) < 10) {
			horas = "0"+horas;
		}
		 // Crear un objeto Calendar e inicializarlo con la fecha original
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);

        // Establecer las nuevas horas y minutos
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horas));
        calendar.set(Calendar.MINUTE, Integer.parseInt(minutos));

        // Devolver el objeto Date resultante
        return calendar.getTime();
	}
	@FXML
	public void accionAceptar() throws Exception {
		
	
			if(validarDatos()) {
				Date fechaIndicada = combinarFechaYHora(dpFecha.getSelectedDate(),tfHora.getText(),tfMinutos.getText());
				ticketManager.crearTicketOrigenFlexPointParaConversion("FLEX",tfFactura.getText(),fechaIndicada);
				BricodepotCabeceraTicket cab = (BricodepotCabeceraTicket) ticketManager.getTicketOrigen().getCabecera();
	
				try {
					log.debug("accionAceptar() - seteando tipo documento a FT");
					ticketManager.setDocumentoActivo(sesion.getAplicacion().getDocumentos().getDocumento("FT"));
		
					cab.setIdTicket(ServicioContadoresImpl.get().obtenerValorContador(generarDatosSesion(),ticketManager.getDocumentoActivo().getIdContador()));
					cab.setCodTicket("FT " + cab.getFechaAsLocale().substring(6,10)+ cab.getCliente().getCodCliente()+cab.getCodCaja()+"/"+StringUtils.leftPad(cab.getIdTicket().toString(), 8, "0"));
					
					getDatos().put(FacturacionArticulosController.TICKET_KEY, ticketManager);
					limpiarFormulario();
					getStage().close();
					
				}
				catch (DocumentoException e) {
					log.error("accionAceptar() - Error cambiando tipo de documento activo");
				}
				
			}
		getStage().close();
	}

	private Boolean validarDatos() {
		
		if (dpFecha.getSelectedDate() == null) {
			return false;
		}
		if (StringUtils.isBlank(tfHora.getText())) {
			return false;
		}
		
		if (StringUtils.isBlank(tfMinutos.getText())) {
			return false;
		}
		return true;
	}

	@FXML
	public void accionCancelarEsc(KeyEvent e) {
		log.debug("accionCancelarEsc()");

		if (e.getCode() == KeyCode.ESCAPE) {
			accionCancelar();
			e.consume();
		}
	}

	public void accionCancelar() {
		getDatos().put("cancela", "cancela");
		super.accionCancelar();
		
	}
	
	public boolean comprobarFormatoHora(String hora) {
		String regex = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$";

        // Compila el patrón
        Pattern pattern = Pattern.compile(regex);

        // Crea un objeto Matcher para la cadena de entrada
        Matcher matcher = pattern.matcher(hora);
	
        if (matcher.matches()) {
        	log.debug("Hora valida");
        	return true;
        }
        log.debug("Hora NO valida");
        return false;

	}
	
	public boolean comprobarFormatoFecha(String fecha) {
		
		// Define el patrón de expresión regular para los formatos DD/MM/AAAA o DD-MM-AAAA
        String regex = "^(0[1-9]|[1-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/(\\d{4})$|^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-(\\d{4})$";

        // Compila el patrón
        Pattern pattern = Pattern.compile(regex);

        // Crea un objeto Matcher para la cadena de entrada
        Matcher matcher = pattern.matcher(fecha);

	
        if (matcher.matches()) {
        	log.debug("Fecha valida");
        	return true;
        }
        log.debug("Fecha NO valida");
        return false;

	}
	
	private DatosSesionBean generarDatosSesion() throws EmpresaException {
		DatosSesionBean datosSesion = new DatosSesionBean();
		datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
		datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
		return datosSesion;
	}

}

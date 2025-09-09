package com.comerzzia.bricodepot.pos.devices.visor.comun.tarjeta;

import java.math.BigDecimal;
import java.util.UUID;

import javax.ws.rs.BadRequestException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.api.model.loyalty.TarjetaBean;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.dispositivo.comun.tarjeta.CodigoTarjetaController;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.ticket.cabecera.TarjetaRegaloTicket;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import rest.client.tarjeta.RestTarjetaClient;

@Component
@Primary
public class BricodepotCodigoTarjetaController extends CodigoTarjetaController {

	private static final String TIPO_TARJETA_ABONO_COMERZZIA = "ABC";

	public static final String PARAMETRO_ES_DEVOLUCION = "Es venta";
	
	public static final String PARAMETRO_ES_DEVOLUCION_FLEXPOINT = "Es flexpoint";
	
	@Autowired
	private VariablesServices variablesServices;
	@Autowired
	private Sesion sesion;
	@FXML
	private Button btTarjetaAbono;
	
	@Override
	public void initializeForm() throws InitializeGuiException {
		super.initializeForm();
		if ((getDatos().containsKey(PARAMETRO_ES_DEVOLUCION) && (boolean) getDatos().get(PARAMETRO_ES_DEVOLUCION))
		        || (getDatos().containsKey(PARAMETRO_ES_DEVOLUCION_FLEXPOINT) && (boolean) getDatos().get(PARAMETRO_ES_DEVOLUCION_FLEXPOINT))) {
			btTarjetaAbono.setVisible(true);
			btTarjetaAbono.setDisable(false);
		}
		else {
			btTarjetaAbono.setVisible(false);
			btTarjetaAbono.setDisable(true);
		}
	}
	
	@FXML
	public void crearTarjetaAbono() throws BadRequestException, Exception {
		log.debug("crearTarjetaAbono() - Petición al servidor para crear la tarjeta regalo...");
		String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		TarjetaBean tarjeta = RestTarjetaClient.salvarTarjeta(apiKey, uidActividad, "0", TIPO_TARJETA_ABONO_COMERZZIA);
		TarjetaRegaloTicket tarjetaTicket = new TarjetaRegaloTicket();
		tarjetaTicket.setNumTarjetaRegalo(tarjeta.getNumeroTarjeta());
		tarjetaTicket.setSaldo(new BigDecimal(tarjeta.getSaldo()));
		tarjetaTicket.setSaldoProvisional(BigDecimal.ZERO);
		tarjetaTicket.setImporteRecarga(new BigDecimal(tarjeta.getSaldo()));
		tarjetaTicket.setUidTransaccion(UUID.randomUUID().toString());
		
		getDatos().put(PARAMETRO_NUM_TARJETA, tarjetaTicket.getNumTarjetaRegalo());
		getStage().close();
	}

}

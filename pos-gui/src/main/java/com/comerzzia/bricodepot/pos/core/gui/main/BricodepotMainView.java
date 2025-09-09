package com.comerzzia.bricodepot.pos.core.gui.main;

import java.net.NoRouteToHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.services.core.sesion.BricodepotSesionAplicacion;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.main.MainView;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.util.i18n.I18N;


@Component
@Primary
public class BricodepotMainView extends MainView{
	
	@Autowired	
	private BricodepotCajasService cajasService;
	
	@Autowired
	private Sesion sesion;
	
	@Autowired
	private BricodepotSesionAplicacion sesionAplicacion;
	
	
	@Override
	protected String getFXMLName() {
		return getFXMLName(MainView.class);
	}
	
	@Override
	protected void cierreSesion() {
//		if (sesionAplicacion.isCajaMasterActivada() && sesion.getAplicacion().getTiendaCaja().getIdTipoCaja() != 0L && sesion.getSesionCaja().isCajaAbierta()) {
		if (sesionAplicacion.isCajaMasterActivada() && sesion.getSesionCaja().isCajaAbierta()) {
			boolean reintentoGeneral = true;
			while (reintentoGeneral) {
				boolean reintento = true;
				while (reintento) {
					try {
						cajasService.transferirCaja(sesion.getSesionCaja().getCajaAbierta());
						reintento = false;
//						try {
							cajasService.marcarCajaTransferida(sesion.getSesionCaja().getCajaAbierta().getCajaBean());
							VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("Se ha transferido su caja a la caja máster."), getStage());
							reintentoGeneral = false;
							super.cierreSesion();
//						}
//						catch (Exception e) {
//							VentanaDialogoComponent.crearVentanaError(I18N.getTexto("Ha ocurrido un error al marcar la caja como transferida. Se va a reintentar."), getStage());
//						}
					}
					catch (RestException | RestHttpException | CajasServiceException e) {
						String mensaje = e.getMessage();
						if (e.getCause().getCause() instanceof IllegalStateException || e.getCause() instanceof NoRouteToHostException || e.getCause().getCause() instanceof NoRouteToHostException) {
							mensaje = I18N.getTexto("No se ha podido conectar al servidor de la caja máster.");
						}

						String textoAlerta = mensaje + System.lineSeparator() + System.lineSeparator() + I18N.getTexto("¿Desea volver a intentarlo?");

						reintento = VentanaDialogoComponent.crearVentanaConfirmacion(textoAlerta, getStage());
						if (!reintento) {
							reintentoGeneral = false;
							super.cierreSesion();
						}
					}
				}

			}
		}
		else {
			super.cierreSesion();
		}

	}
	
}

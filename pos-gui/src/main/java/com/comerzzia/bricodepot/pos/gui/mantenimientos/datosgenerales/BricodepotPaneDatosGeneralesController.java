package com.comerzzia.bricodepot.pos.gui.mantenimientos.datosgenerales;

import java.util.List;

import com.comerzzia.api.model.core.EtiquetaBean;
import com.comerzzia.api.model.loyalty.ColectivosFidelizadoBean;
import com.comerzzia.api.model.loyalty.EnlaceFidelizadoBean;
import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.bricodepot.pos.gui.mantenimientos.BricodepotFidelizadoController;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.datosgenerales.PaneDatosGeneralesController;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.variables.VariablesServices;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import rest.client.fidelizados.enlaces.BricodepotEnlacesFidelizadosRest;
import rest.client.fidelizados.enlaces.BricodepotResponseGetEnlacesRest;

@Component
@Primary
public class BricodepotPaneDatosGeneralesController extends PaneDatosGeneralesController {

	protected static final Logger log = Logger.getLogger(BricodepotPaneDatosGeneralesController.class);

	public static final String FIDELIZACION_COD_COLECTIVO_PROFESIONAL = "FIDELIZACION.COD_COLECTIVO_PROFESIONAL";

	@Autowired
	private Sesion sesion;

	@Override
	public void camposEditables(boolean editable) {
		super.camposEditables(editable);

		chNotifEmail.setDisable(true);
		chNotifMovil.setDisable(true);
		chPaperLess.setDisable(true);

		tfCodColectivo.setVisible(true);
		tfDesColectivo.setVisible(true);
		lbColectivo.setVisible(true);
		btBuscarColectivo.setVisible(true);

		deshabilitaEdicionEmailMagento(); // BRICO-266
		deshabilitaEdicionColectivo(); // BRICO-287

	}

	private void deshabilitaEdicionEmailMagento() { // BRICO-266
		try {
			log.debug("deshabilitaEdicionEmailMagento() - Comprobando si el fidelizado pertenece al colectivo MAGENTO o tiene etiqueta MAGENTO para deshabilitar edición de Email");

			boolean isColectivoMagento = fidelizadoColectivoMagento();
			boolean isEtiquetaMagento = fidelizadoEtiquetaMagento();
			boolean isCustomerLinkMagento = fidelizadoCustomerLinkMagento();
			boolean emailNoEditable = isColectivoMagento || isEtiquetaMagento || isCustomerLinkMagento;
			if (emailNoEditable) {
				log.debug("deshabilitaEdicionEmailMagento() - El fidelizado tiene etiqueta o colectivo: MAGENTO. Deshabilitando edición campo Email");
			}

			tfEmail.setDisable(emailNoEditable);
		}
		catch (Exception e) {
			log.error("deshabilitaEdicionEmailMagento() - Error comprobando si el fidelizado pertenece al colectivo MAGENTO o tiene etiqueta MAGENTO: " + e.getMessage(), e);
		}
	}

	private boolean fidelizadoEtiquetaMagento() { // BRICO-266
		if (fidelizado == null) {
			return false;
		}
		log.debug("fidelizadoEtiquetaMagento() - Comprobacion de etiqueta MAGENTO");
		List<EtiquetaBean> etiquetasCategorias = fidelizado.getEtiquetasCategorias();
		for (EtiquetaBean etiquetaBean : etiquetasCategorias) {
			if (etiquetaBean.getEtiqueta().equals("MAGENTO")) {
				log.debug("fidelizadoEtiquetaMagento() - El fidelizado tiene etiqueta MAGENTO");
				return true;
			}
		}
		return false;
	}

	private boolean fidelizadoColectivoMagento() { // BRICO-266
		if (fidelizado == null) {
			return false;
		}
		
		log.debug("fidelizadoColectivoMagento() - Comprobamos si el fidelizado es colectivo Magento");
		
		List<ColectivosFidelizadoBean> colectivos = fidelizado.getColectivos();
		for (ColectivosFidelizadoBean colectivosFidelizadoBean : colectivos) {
			if (colectivosFidelizadoBean.getCodColectivo().equalsIgnoreCase("MAGENTO")) {
				log.debug("fidelizadoEtiquetaMagento() - El fidelizado pertenece al colectivo MAGENTO");
				return true;
			}
		}
		return false;
	}

	private boolean fidelizadoCustomerLinkMagento() throws RestException, RestHttpException { // BRICO-287
		if (fidelizado == null) {
			return false;
		}
		
		log.debug("fidelizadoCustomerLinkMagento() - Comprobamos si tiene etiqueta de fidelizado MAGENTO");

		String apiKey = variablesService.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();

		BricodepotResponseGetEnlacesRest response = BricodepotEnlacesFidelizadosRest.getEnlacesDeFidelizado(fidelizado.getIdFidelizado(), uidActividad, apiKey);
		List<EnlaceFidelizadoBean> enlacesFidelizado = response.getEnlaces();
		for (EnlaceFidelizadoBean enlace : enlacesFidelizado) {
			if (enlace.getIdClase().contains("magento_linked") && enlace.getIdObjeto().equals("1")) {
				log.debug("fidelizadoCustomerLinkMagento() - El fidelizado tiene etiqueta de fidelizado MAGENTO");
				return true;
			}
		}

		return false;

	}

	private void deshabilitaEdicionColectivo() { // BRICO-287 Edición de fidelizados con datos incompletos
		if (getTabParentController() instanceof BricodepotFidelizadoController) {
			BricodepotFidelizadoController bricodepotFidelizadoController = (BricodepotFidelizadoController) getTabParentController();
			if ("EDICION".equals(bricodepotFidelizadoController.getModo())) {
				boolean sinColectivos = fidelizado.getColectivos().isEmpty();
				tfCodColectivo.setEditable(sinColectivos);
				tfDesColectivo.setEditable(sinColectivos);
				btBuscarColectivo.setDisable(!sinColectivos);
			}
		}
	}

	@Override
	protected void cargarDatosGenerales() {
		super.cargarDatosGenerales();
		log.debug("cargarDatosGenerales()");

		if (fidelizado.getColectivos() != null && !fidelizado.getColectivos().isEmpty()) {
			String codColectivoProfesional = variablesService.getVariableAsString(FIDELIZACION_COD_COLECTIVO_PROFESIONAL);

			for (ColectivosFidelizadoBean colectivo : fidelizado.getColectivos()) {
				if (!colectivo.getCodColectivo().equalsIgnoreCase(codColectivoProfesional)) {
					tfCodColectivo.setText(colectivo.getCodColectivo());
					tfDesColectivo.setText(colectivo.getDesColectivo());

					tfCodColectivo.setEditable(false);
					tfDesColectivo.setEditable(false);
					btBuscarColectivo.setDisable(true);
				}
			}
		}
		else {
			tfCodColectivo.setEditable(true);
			tfDesColectivo.setEditable(true);
			btBuscarColectivo.setDisable(false);
		}
	}

}

package com.comerzzia.bricodepot.pos.services.core.sesion;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.cashmanagement.client.CashManagementApi;
import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.posservices.client.ConversionApi;
import com.comerzzia.bricodepot.posservices.client.PresupuestosApi;
import com.comerzzia.bricodepot.posservices.client.TarjetasApi;
import com.comerzzia.core.servicios.api.ComerzziaApiManager;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.sesion.SesionAplicacion;
import com.comerzzia.pos.services.core.sesion.SesionInitException;
import com.comerzzia.pos.util.config.AppConfig;

@Component
@Primary
public class BricodepotSesionAplicacion extends SesionAplicacion {

	public static String FILENAME_MASTER_HOSTNAME = "master_url";

	private boolean cajaMasterActivada;
	@Autowired
	protected Sesion sesion;
	@Autowired
	protected ComerzziaApiManager comerzziaApiManager;

	@Autowired
	protected BricodepotCajasService cajasService;
	
	private String urlCajaMaster;

	@Override
	public void init() throws SesionInitException {
		super.init();

		Scanner scanner = null;
		try {
			log.info("Buscando la URL de la caja máster en el fichero " + FILENAME_MASTER_HOSTNAME);
			URL urlFile = Thread.currentThread().getContextClassLoader().getResource(FILENAME_MASTER_HOSTNAME);
			if (urlFile != null) {
				File fileUrlMaster = new File(urlFile.getPath());
				if (fileUrlMaster.exists()) {
					scanner = new Scanner(fileUrlMaster);
					if (scanner.hasNextLine()) {
						urlCajaMaster = scanner.nextLine();

						if (StringUtils.isNotBlank(urlCajaMaster)) {
							log.info("URL de la caja máster: " + urlCajaMaster);
							com.comerzzia.instoreengine.master.rest.path.InStoreEngineWebservicesMasterPath.initPath(urlCajaMaster);
							cajaMasterActivada = true;
						}
						else {
							log.error("El fichero está vacío.");
						}
					}
				}
			}
			
		} catch (Exception e) {
			log.error("init() - Ha habido un error al buscar el fichero del hostaname de la caja master: "
					+ e.getMessage(), e);
		} finally {
			if (scanner != null) {
				scanner.close();
				
			}
		}

		Dimension resolucion = Toolkit.getDefaultToolkit().getScreenSize();
		
		comerzziaApiManager.registerAPI("CashManagementApi",CashManagementApi.class, "cashmanagement");

		comerzziaApiManager.registerAPI("ConversionApi",ConversionApi.class, "posservices");
		comerzziaApiManager.registerAPI("PresupuestoApi",PresupuestosApi.class, "posservices");
		
		comerzziaApiManager.registerAPI("TarjetasApi",TarjetasApi.class, "posservices");
		
		if (resolucion.width == 800 && resolucion.height == 600)
			AppConfig.skin = "reducido";
	}
	
	public boolean isCajaMasterActivada() {
		return cajaMasterActivada;
	}
}
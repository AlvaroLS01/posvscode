package com.comerzzia.bricodepot.pos.gui.mantenimientos.fidelizados.resumen;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.comerzzia.api.model.core.TiendaBean;
import com.comerzzia.api.model.loyalty.FidelizadoBean;
import com.comerzzia.api.model.loyalty.TarjetaBean;
import com.comerzzia.api.model.sales.ArticuloAlbaranVentaBean;
import com.comerzzia.api.rest.client.fidelizados.ConsultarFidelizadoRequestRest;
import com.comerzzia.pos.core.gui.RestBackgroundTask;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.ConsultarTarjetasFidelizadoTask;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.ConsultarTiendaFavoritaFidelizadoTask;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.ConsultarUltimasVentasFidelizadoTask;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.VentaGui;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.resumen.PaneResumenFidelizadoController;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.format.FormatUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javafx.collections.FXCollections;

@Component
@Primary
public class BricodepotPaneResumenFidelizadoController extends PaneResumenFidelizadoController{

	@Autowired
	protected Sesion sesion;
	
	protected ConsultarUltimasVentasFidelizadoTask consultarVentasTask;
	protected ConsultarTarjetasFidelizadoTask consultarTarjetasTask;
	protected ConsultarTiendaFavoritaFidelizadoTask consultarTiendaFavoritaTask;
	
	@Override
	protected void cargarTiendaFavorita(final FidelizadoBean fidelizado){
		String apiKey = variablesService.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
			ConsultarFidelizadoRequestRest consulta = new ConsultarFidelizadoRequestRest(apiKey, uidActividad);
			consulta.setIdFidelizado(String.valueOf(fidelizado.getIdFidelizado()));
			
			consultarTiendaFavoritaTask = SpringContext.getBean(ConsultarTiendaFavoritaFidelizadoTask.class, 
					consulta, 
					new RestBackgroundTask.FailedCallback<TiendaBean>() {
						@Override
						public void succeeded(TiendaBean result) {						
							tiendaFavorita = result;	
							cargarResumen(fidelizado);
						}
						@Override
						public void failed(Throwable throwable) {
//							getTabParentController().getApplication().getMainView().close();
						}
					}, getTabParentController().getStage());
			consultarTiendaFavoritaTask.start();
			
			consultarTarjetasTask = SpringContext.getBean(ConsultarTarjetasFidelizadoTask.class, 
					consulta, 
					new RestBackgroundTask.FailedCallback<List<TarjetaBean>>() {
						@Override
						public void succeeded(List<TarjetaBean> result) {													
							   Set<Long> cuentas = new HashSet<Long>();		
							   List<TarjetaBean> tarjetasFidelizado = new ArrayList<TarjetaBean>();
							   Double saldoAcumulado = Double.valueOf(0);
							   if(result != null) {
								   for(TarjetaBean tarjeta: result) {
									   if(!tarjeta.getPermitePago()) {
										   if(!cuentas.contains(tarjeta.getIdCuentaTarjeta())){ //Comprobamos que las tarjetas sean de cuentas independientes
											   saldoAcumulado = saldoAcumulado + tarjeta.getSaldo();
										   }								  
										   cuentas.add(tarjeta.getIdCuentaTarjeta());
									   }
									   if(tarjeta.getPermiteVincular() && !tarjeta.getPermitePago() &&tarjeta.isActivo()){
										   tarjetasFidelizado.add(tarjeta);
									   }
								   }
								   tfSaldo.setText(FormatUtil.getInstance().formateaNumero(new BigDecimal(saldoAcumulado), 2));
								   if(StringUtils.isNotBlank(getTabParentController().getNumeroTarjetaFidelizado())){
									   tfNumeroTarjeta.setText(getTabParentController().getNumeroTarjetaFidelizado());
								   }else{
									   tfNumeroTarjeta.setText("");
								   }
								   
							   }

						}
						@Override
						public void failed(Throwable throwable) {
//							getTabParentController().getApplication().getMainView().close();
						}
					}, getTabParentController().getStage());
			consultarTarjetasTask.start();
			
			consulta.setIdFidelizado(String.valueOf(fidelizado.getIdFidelizado()));
			Date fechaHoy = new Date();
			Calendar calDesde = Calendar.getInstance();
			calDesde.set(Calendar.YEAR, calDesde.get(Calendar.YEAR)-1);
			Date fechaDesde = calDesde.getTime();
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			consulta.setFechaDesde(format.format(fechaDesde));
			consulta.setFechaHasta(format.format(fechaHoy));
			consultarVentasTask = SpringContext.getBean(ConsultarUltimasVentasFidelizadoTask.class, 
					consulta, 
						new RestBackgroundTask.FailedCallback<List<ArticuloAlbaranVentaBean>>() {
							@Override
							public void succeeded(List<ArticuloAlbaranVentaBean> result) {
								if(isPuedeVerVentas()){
									ventas = FXCollections.observableArrayList();
									for(ArticuloAlbaranVentaBean art : result){
										VentaGui artGui = new VentaGui(art);
										ventas.add(artGui);
									}							
								}else{
									ventas = null;
								}
								tableVentas.setItems(ventas);
							}
							@Override
							public void failed(Throwable throwable) {
//								getTabParentController().getApplication().getMainView().close();
							}
						}, getTabParentController().getStage());
			consultarVentasTask.start();
		
	}

	public void closeAllTasks() {
		if (consultarVentasTask != null) {
			consultarVentasTask.cancel();
		}

		if (consultarTarjetasTask != null) {
			consultarTarjetasTask.cancel();
		}

		if (consultarTiendaFavoritaTask != null) {
			consultarTiendaFavoritaTask.cancel();
		}

	}
}

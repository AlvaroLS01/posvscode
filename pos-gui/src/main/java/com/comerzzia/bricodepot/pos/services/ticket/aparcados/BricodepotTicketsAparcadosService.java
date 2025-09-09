package com.comerzzia.bricodepot.pos.services.ticket.aparcados;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.core.util.mybatis.session.SqlSession;
import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.persistence.mybatis.SessionFactory;
import com.comerzzia.pos.persistence.tickets.aparcados.TicketAparcadoExample;
import com.comerzzia.pos.persistence.tickets.aparcados.TicketAparcadoExample.Criteria;
import com.comerzzia.pos.services.ticket.aparcados.TicketsAparcadosService;
import com.comerzzia.pos.services.ticket.copiaSeguridad.CopiaSeguridadTicketService;

@Primary
@Component
@SuppressWarnings({ "deprecation" })
public class BricodepotTicketsAparcadosService extends TicketsAparcadosService {
	
	@Override
	public int countTicketsAparcados(Long idTipoDocumento) {
		SqlSession sqlSession = new SqlSession();
		TicketAparcadoExample example = null;
		try {
			log.debug("countTicketsAparcados() - Consultando ticket aparcado en base de datos...");
			sqlSession.openSession(SessionFactory.openSession());
			example = new TicketAparcadoExample();
			
			Criteria exampleCriteria1 = example.or();
			Criteria exampleCriteria2 = example.or();
			
			exampleCriteria1.andUidActividadEqualTo(sesion.getAplicacion().getUidActividad()).andCodAlmacenEqualTo(sesion.getAplicacion().getCodAlmacen())
	        .andCodCajaEqualTo(sesion.getAplicacion().getCodCaja());
			
			exampleCriteria2.andUidActividadEqualTo(sesion.getAplicacion().getUidActividad()).andCodAlmacenEqualTo(sesion.getAplicacion().getCodAlmacen())
	        .andCodCajaEqualTo(sesion.getAplicacion().getCodCaja());
			
			if (idTipoDocumento != null) {
			    TipoDocumentoBean documento = documentos.getDocumento(idTipoDocumento);
			    
			    String codTipoDocumentoFacturaCompleta = documento.getTipoDocumentoFacturaDirecta();
			    if (codTipoDocumentoFacturaCompleta != null) {
			        TipoDocumentoBean tipoDocumentoFacturaCompleta = documentos.getDocumento(codTipoDocumentoFacturaCompleta);
			        Long idTipoDocumentoFacturaCompleta = tipoDocumentoFacturaCompleta.getIdTipoDocumento();

			        exampleCriteria1.andIdTipoDocumentoEqualTo(idTipoDocumento);
			        exampleCriteria2.andIdTipoDocumentoEqualTo(idTipoDocumentoFacturaCompleta);
			    }
			}

			
			exampleCriteria1.andUsuarioNotEqualTo(CopiaSeguridadTicketService.USUARIO_BACKUP_TICKET);
			exampleCriteria2.andUsuarioNotEqualTo(CopiaSeguridadTicketService.USUARIO_BACKUP_TICKET);
			
		} catch (Exception e) {
			String msg = "Se ha producido un error durante el recuento de tickets apartados - " + e.getMessage();;
			log.error("countTicketsAparcados() - " + msg, e);
		}
		finally {
			sqlSession.close();
		}
		
		return ticketAparcadoMapper.countByExample(example);
	}

}

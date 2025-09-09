package com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle;

import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.CajaDet;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.CajaDetExample;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.CajaDetKey;
import com.comerzzia.pos.persistence.cajas.movimientos.CajaMovimientoBean;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface CajaDetMapper {
	
	Double sumByExample(CajaDetExample example);
	
    long countByExample(CajaDetExample example);

    int deleteByExample(CajaDetExample example);

    int deleteByPrimaryKey(CajaDetKey key);

    int insert(CajaDet record);

    int insertSelective(CajaDet record);

    List<CajaDet> selectByExampleWithRowbounds(CajaDetExample example, RowBounds rowBounds);

    List<CajaMovimientoBean> selectByExample(CajaDetExample example);

    CajaDet selectByPrimaryKey(CajaDetKey key);

    int updateByExampleSelective(@Param("record") CajaDet record, @Param("example") CajaDetExample example);

    int updateByExample(@Param("record") CajaDet record, @Param("example") CajaDetExample example);

    int updateByPrimaryKeySelective(CajaDet record);

    int updateByPrimaryKey(CajaDet record);
    
    List<CajaMovimientoBean> selectMovCaja(@Param("uidActividad")String uidActividad, @Param("codCaja")String codCaja, @Param("codconcepto_mov")String codconcepto_mov);
}
package com.comerzzia.bricodepot.pos.persistence.cajas.apuntes.movimientos;

import com.comerzzia.bricodepot.pos.persistence.cajas.apuntes.movimientos.CajasMovimientos;
import com.comerzzia.bricodepot.pos.persistence.cajas.apuntes.movimientos.CajasMovimientosExample;
import com.comerzzia.bricodepot.pos.persistence.cajas.apuntes.movimientos.CajasMovimientosKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CajasMovimientosMapper {
    long countByExample(CajasMovimientosExample example);

    int deleteByExample(CajasMovimientosExample example);

    int deleteByPrimaryKey(CajasMovimientosKey key);

    int insert(CajasMovimientos record);

    int insertSelective(CajasMovimientos record);

    List<CajasMovimientos> selectByExample(CajasMovimientosExample example);

    CajasMovimientos selectByPrimaryKey(CajasMovimientosKey key);

    int updateByExampleSelective(@Param("record") CajasMovimientos record, @Param("example") CajasMovimientosExample example);

    int updateByExample(@Param("record") CajasMovimientos record, @Param("example") CajasMovimientosExample example);

    int updateByPrimaryKeySelective(CajasMovimientos record);

    int updateByPrimaryKey(CajasMovimientos record);
}
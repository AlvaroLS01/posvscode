package com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.retirada;

import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.retirada.DetMovRetiradaExample;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.retirada.DetMovRetiradaKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface DetMovRetiradaMapper {
    long countByExample(DetMovRetiradaExample example);

    int deleteByExample(DetMovRetiradaExample example);

    int deleteByPrimaryKey(DetMovRetiradaKey key);

    int insert(DetMovRetiradaKey record);

    int insertSelective(DetMovRetiradaKey record);

    List<DetMovRetiradaKey> selectByExampleWithRowbounds(DetMovRetiradaExample example, RowBounds rowBounds);

    List<DetMovRetiradaKey> selectByExample(DetMovRetiradaExample example);

    int updateByExampleSelective(@Param("record") DetMovRetiradaKey record, @Param("example") DetMovRetiradaExample example);

    int updateByExample(@Param("record") DetMovRetiradaKey record, @Param("example") DetMovRetiradaExample example);
}
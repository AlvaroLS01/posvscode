package com.comerzzia.bricodepot.pos.persistence.cajas.tiendascajas;

import com.comerzzia.bricodepot.pos.persistence.cajas.tiendascajas.TiendasCajas;
import com.comerzzia.bricodepot.pos.persistence.cajas.tiendascajas.TiendasCajasExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface TiendasCajasMapper {
    long countByExample(TiendasCajasExample example);

    int deleteByExample(TiendasCajasExample example);

    int deleteByPrimaryKey(String uidCaja);

    int insert(TiendasCajas record);

    int insertSelective(TiendasCajas record);

    List<TiendasCajas> selectByExampleWithBLOBsWithRowbounds(TiendasCajasExample example, RowBounds rowBounds);

    List<TiendasCajas> selectByExampleWithBLOBs(TiendasCajasExample example);

    List<TiendasCajas> selectByExampleWithRowbounds(TiendasCajasExample example, RowBounds rowBounds);

    List<TiendasCajas> selectByExample(TiendasCajasExample example);

    TiendasCajas selectByPrimaryKey(String uidCaja);

    int updateByExampleSelective(@Param("record") TiendasCajas record, @Param("example") TiendasCajasExample example);

    int updateByExampleWithBLOBs(@Param("record") TiendasCajas record, @Param("example") TiendasCajasExample example);

    int updateByExample(@Param("record") TiendasCajas record, @Param("example") TiendasCajasExample example);

    int updateByPrimaryKeySelective(TiendasCajas record);

    int updateByPrimaryKeyWithBLOBs(TiendasCajas record);

    int updateByPrimaryKey(TiendasCajas record);
}
package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinetBind;
import org.apache.ibatis.annotations.Delete;

/**
 * (ElectricityCabinetBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCabinetBindMapper extends BaseMapper<ElectricityCabinetBind> {

    @Delete("delete  FROM t_electricity_cabinet_bind  WHERE uid = #{id}")
    void deleteByUid(Long id);
}
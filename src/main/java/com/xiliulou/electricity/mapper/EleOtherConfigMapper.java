package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleOtherConfig;
import org.apache.ibatis.annotations.Update;

/**
 * (CupboardOtherConfig)表数据库访问层
 *
 * @author Hardy
 * @since 2021-07-21 15:22:56
 */
public interface EleOtherConfigMapper extends BaseMapper<EleOtherConfig> {

    @Update("update t_ele_other_config set tenant_id=#{tenantId},update_time=#{updateTime} where eid=#{eid}")
    Integer updateByEid(EleOtherConfig eleOtherConfig);

}

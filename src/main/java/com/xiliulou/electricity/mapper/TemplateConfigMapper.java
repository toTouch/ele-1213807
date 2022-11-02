package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zgw
 * @date 2021/11/30 19:29
 * @mood
 */
@Mapper
public interface TemplateConfigMapper extends BaseMapper<TemplateConfigEntity> {
    
    Integer update(TemplateConfigEntity templateConfig);
    
    Integer deleteById(@Param("id") Long id, @Param("tenantId") Integer tenantId);
}

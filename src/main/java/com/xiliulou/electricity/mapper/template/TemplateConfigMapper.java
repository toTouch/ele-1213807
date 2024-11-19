/**
 *  Create date: 2024/7/23
 */

package com.xiliulou.electricity.mapper.template;

import cn.hutool.extra.template.TemplateConfig;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/23 14:44
 */
public interface TemplateConfigMapper {
    
    /**
     * 新增
     *
     * @param templateConfig
     * @author caobotao.cbt
     * @date 2024/7/23 14:47
     */
    int insert(TemplateConfigEntity templateConfig);
    
    
    /**
     * 更新
     *
     * @param templateConfig
     * @author caobotao.cbt
     * @date 2024/7/23 14:47
     */
    int update(TemplateConfigEntity templateConfig);
    
    
    /**
     * 根据渠道+租户id查询
     *
     * @author caobotao.cbt
     * @date 2024/7/23 14:48
     */
    List<TemplateConfigEntity> selectListByTenantIdAndChannels(@Param("tenantId") Integer tenantId,@Param("channels") List<String> channels);
    
    
    /**
     * 删除
     *
     * @param tenantId
     * @param id
     * @author caobotao.cbt
     * @date 2024/7/23 16:05
     */
    int deleteById(@Param("tenantId") Integer tenantId, @Param("id") Long id);
    
    /**
     * 根据id+租户id查询
     *
     * @author caobotao.cbt
     * @date 2024/7/23 14:48
     */
    TemplateConfigEntity selectById(@Param("tenantId") Integer tenantId, @Param("id") Long id);
}

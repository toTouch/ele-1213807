/**
 * Create date: 2024/7/23
 */

package com.xiliulou.electricity.service.template;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.request.template.TemplateConfigOptRequest;

import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/23 15:14
 */
public interface TemplateConfigService {
    
    /**
     * 根据租户id+渠道查询
     *
     * @param tenantId
     * @param channel
     * @author caobotao.cbt
     * @date 2024/7/23 15:20
     */
    TemplateConfigEntity queryByTenantIdAndChannelFromCache(Integer tenantId, String channel);
    
    
    /**
     * 根据租户id+渠道集合查询
     *
     * @param tenantId
     * @param channels
     * @author caobotao.cbt
     * @date 2024/7/23 15:20
     * @return
     */
    List<TemplateConfigEntity> queryByTenantIdAndChannelListFromCache(Integer tenantId, List<String> channels);
    
    /**
     * 根据租户id+渠道查询模版id
     *
     * @param tenantId
     * @param channel
     * @author caobotao.cbt
     * @date 2024/7/23 15:23
     */
    List<String> queryTemplateIdByTenantIdChannel(Integer tenantId, String channel);
    
    
    /**
     * 新增
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/7/23 15:24
     */
    R insert(TemplateConfigOptRequest request);
    
    /**
     * 更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/7/23 15:24
     */
    R update(TemplateConfigOptRequest request);
    
    /**
     * 删除
     *
     * @param tenantId
     * @param id
     * @author caobotao.cbt
     * @date 2024/7/23 15:24
     */
    R delete(Integer tenantId,Long id);
    
}
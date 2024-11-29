/**
 * Create date: 2024/7/23
 */

package com.xiliulou.electricity.converter;

import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.request.template.TemplateConfigOptRequest;

import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/23 15:58
 */
public class TemplateConfigConverter {
    
    
    /**
     * 操作参数转换
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/7/23 15:58
     */
    public static TemplateConfigEntity optReqToDo(TemplateConfigOptRequest request) {
        if (Objects.isNull(request)){
            return null;
        }
        TemplateConfigEntity templateConfigEntity = new TemplateConfigEntity();
        templateConfigEntity.setId(request.getId());
        templateConfigEntity.setBatteryOuttimeTemplate(request.getBatteryOuttimeTemplate());
        templateConfigEntity.setElectricQuantityRemindTemplate(request.getElectricQuantityRemindTemplate());
        templateConfigEntity.setBatteryMemberCardExpiringTemplate(request.getBatteryMemberCardExpiringTemplate());
        templateConfigEntity.setCarMemberCardExpiringTemplate(request.getCarMemberCardExpiringTemplate());
        templateConfigEntity.setTenantId(request.getTenantId());
        templateConfigEntity.setChannel(request.getChannel());
        templateConfigEntity.setCreateTime(System.currentTimeMillis());
        templateConfigEntity.setUpdateTime(System.currentTimeMillis());
        return templateConfigEntity;
    }
    
}

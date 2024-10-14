package com.xiliulou.electricity.request.template;

import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.core.base.enums.ChannelEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class TemplateConfigOptRequest implements Serializable {
    
    @NotNull(groups = UpdateGroup.class)
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 电池超时未归还
     */
    private String batteryOuttimeTemplate;
    
    /**
     * 低电量模板
     */
    private String electricQuantityRemindTemplate;
    
    /**
     * 电池月卡快过期提醒模板
     */
    private String batteryMemberCardExpiringTemplate;
    
    /**
     * 租车月卡快过期提醒模板
     */
    private String carMemberCardExpiringTemplate;
    
    
    /**
     * 渠道
     *
     * @see com.xiliulou.core.base.enums.ChannelEnum
     */
    private String channel = ChannelEnum.WECHAT.getCode();
    
}

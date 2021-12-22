package com.xiliulou.electricity.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.List;

/**
 * (WechatTemplateAdminNotification)实体类
 *
 * @author Eclair
 * @since 2021-11-25 16:50:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_wechat_template_admin_notification")
public class WechatTemplateAdminNotification {
    
    private Long id;
    
    private Integer tenantId;

    private Long uid;
    
    private String openIds;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


}

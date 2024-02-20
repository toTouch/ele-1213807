package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserInfoExtra)实体类
 *
 * @author Eclair
 * @since 2024-02-18 10:39:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_info_extra")
public class UserInfoExtra {
    
    private Long uid;
    
    /**
     * 商户ID
     */
    private Long merchantId;
    
    /**
     * 渠道员uid
     */
    private Long channelEmployeeUid;
    
    /**
     * 场地员工uid
     */
    private Long placeUid;
    
    private Long placeId;
    
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    
    private Integer tenantId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
}

package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 客服电话表
 * @date 2024/10/24 17:34:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_service_phone")
public class ServicePhone {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 客服电话
     */
    private String phone;
    
    /**
     * 电话文案
     */
    private String remark;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    public static final Integer LIMIT_NUM = 5;
}

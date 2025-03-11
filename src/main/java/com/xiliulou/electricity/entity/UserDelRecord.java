package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户删除(注销)记录表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_del_record")
public class UserDelRecord {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long uid;
    
    /**
     * 被删老用户的手机号
     */
    private String delPhone;
    
    /**
     * 被删老用户的身份证号
     */
    private String delIdNumber;
    
    /**
     * 删除/注销时间
     */
    private Long delTime;
    
    /**
     * 注销延迟天数，默认30天
     */
    private Integer delayDay;
    
    /**
     * 账号状态:1-已删除 2-注销中 3-已注销
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
}
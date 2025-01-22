package com.xiliulou.electricity.entity.userinfo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 紧急联系人
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_emergency_contact")
public class EmergencyContact {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 紧急联系人
     */
    private String emergencyName;
    
    /**
     * 紧急联系电话
     */
    private String emergencyPhone;
    
    /**
     * 联系人关系：0-其他(默认)，1-家人，2-朋友，3-同事
     */
    private Integer relation;
    
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
}
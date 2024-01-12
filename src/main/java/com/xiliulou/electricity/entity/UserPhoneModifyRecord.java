package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (UserPhoneModifyRecord)实体类
 *
 * @author zhangyongbo
 * @since 2024-01-09 21:17:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_ele_phone_modify_record")
public class UserPhoneModifyRecord {
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 用户id
     */
    private Long uid;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    private String oldPhone;
    
    private String newPhone;
    
    private Long createTime;
    
    private Long updateTime;
}

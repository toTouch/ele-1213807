package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserActiveInfo)实体类
 *
 * @author zgw
 * @since 2023-03-01 10:15:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_active_info")
public class UserActiveInfo {
    
    private Long id;
    
    private Long activeTime;
    
    private Long batteryId;
    
    private String batteryName;
    
    private Long uid;
    
    private String phone;
    
    private String userName;
    
    private Integer tenantId;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}

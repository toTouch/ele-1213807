package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserChannel)实体类
 *
 * @author Eclair
 * @since 2023-03-22 15:34:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_channel")
public class UserChannel {
    
    private Long id;
    
    private Long uid;
    
    private Long operateUid;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Long tenantId;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}

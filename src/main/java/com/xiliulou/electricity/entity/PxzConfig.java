package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (PxzConfig)实体类
 *
 * @author Eclair
 * @since 2023-02-15 16:23:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_pxz_config")
public class PxzConfig {
    
    private Long id;
    
    private Integer tenantId;
    
    private String aesKey;
    
    private String merchantCode;
    
    private Long createTime;
    
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}

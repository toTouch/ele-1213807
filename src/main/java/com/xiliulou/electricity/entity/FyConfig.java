package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_fy_config")
public class FyConfig {
    
    private Long id;
    
    private Integer tenantId;
    
    private String merchantCode;
    
    private String storeCode;
    
    private String channelCode;
    
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    
    public static final String FREE_ORDER_DATE = "12";
}

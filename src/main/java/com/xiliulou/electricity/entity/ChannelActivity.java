package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ChannelActivity)实体类
 *
 * @author zgw
 * @since 2023-03-22 10:42:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_channel_activity")
public class ChannelActivity {
    
    private Long id;
    
    private String name;
    
    /**
     * 有效天数
     */
    private Integer validDay;
    
    /**
     * 是否限制时间 0--限制 1--不限制
     */
    private Integer validDayLimit;
    
    /**
     * 上下架状态  0--上架 1--下架
     */
    private Integer status;
    
    /**
     * 绑定活动id
     */
    private Long bindActivityId;
    
    /**
     * 绑定活动类型 0--不绑定 1--返现 2--优惠券
     */
    private Integer bindActivityType;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Long tenantId;
    
    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    
    public static final Integer STATUS_START_USING = 0;
    
    public static final Integer STATUS_FORBIDDEN = 1;
}

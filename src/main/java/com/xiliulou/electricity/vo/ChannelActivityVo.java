package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/22 11:19
 * @mood
 */
@Data
public class ChannelActivityVo {
    
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
}

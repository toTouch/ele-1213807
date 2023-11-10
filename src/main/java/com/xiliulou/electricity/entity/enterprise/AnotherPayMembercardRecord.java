package com.xiliulou.electricity.entity.enterprise;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 代付记录表(AnotherPayMembercardRecord)实体类
 *
 * @author Eclair
 * @since 2023-10-10 15:07:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_another_pay_membercard_record")
public class AnotherPayMembercardRecord {
    
    private Long id;
    
    /**
     * 订单Id
     */
    private String orderId;
    
    private Long uid;
    
    /**
     * 套餐开始时间
     */
    private Long beginTime;
    
    /**
     * 套餐结束时间
     */
    private Long endTime;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 创建时间
     */
    private Long updateTime;
    
    private Integer tenantId;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}

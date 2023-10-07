package com.xiliulou.electricity.entity.enterprise;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserBehaviorRecord)实体类
 *
 * @author Eclair
 * @since 2023-09-27 17:08:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_behavior_record")
public class UserBehaviorRecord {
    
    private Long id;
    
    /**
     * 订单Id
     */
    private String orderId;
    
    private Long uid;
    
    /**
     * 订单类型(1--租电池,2--还电池,3--交押金,4--买套餐)
     */
    private Integer type;
    
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
    
    //订单类型(1--租电池,2--还电池,3--交押金,4--买套餐)
    public static final Integer TYPE_RENT_BATTERY = 1;
    public static final Integer TYPE_RETURN_BATTERY = 2;
    public static final Integer TYPE_PAY_DEPOSIT = 3;
    public static final Integer TYPE_PAY_MEMBERCARD = 4;
    
}

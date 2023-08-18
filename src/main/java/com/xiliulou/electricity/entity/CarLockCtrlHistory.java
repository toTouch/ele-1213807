package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (CarLockCtrlHistory)实体类
 *
 * @author Eclair
 * @since 2023-04-04 16:11:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_car_lock_ctrl_history")
public class CarLockCtrlHistory {
    
    private Long id;
    
    private Long uid;
    
    private String name;
    
    private String phone;
    
    /**
     * 状态  1--解锁成功 2--解锁失败  3--加锁成功  4--加锁失败
     */
    private Integer status;
    
    /**
     * 类型  1--套餐过期加锁  2--套餐续费解锁  3--绑定用户解锁 4--解绑用户加锁
     */
    private Integer type;
    
    private Long carId;
    
    private String carSn;
    
    private String carModel;
    
    private Long carModelId;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer tenantId;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    public static final Integer STATUS_UN_LOCK_SUCCESS = 1;
    
    public static final Integer STATUS_UN_LOCK_FAIL = 2;
    
    public static final Integer STATUS_LOCK_SUCCESS = 3;
    
    public static final Integer STATUS_LOCK_FAIL = 4;
    
    public static final Integer TYPE_MEMBER_CARD_LOCK = 1;
    
    public static final Integer TYPE_MEMBER_CARD_UN_LOCK = 2;
    
    public static final Integer TYPE_BIND_USER_UN_LOCK = 3;
    
    public static final Integer TYPE_UN_BIND_USER_LOCK = 4;

    /** 滞纳金加锁 */
    public static final Integer TYPE_SLIPPAGE_LOCK = 5;

    /** 滞纳金解锁 */
    public static final Integer TYPE_SLIPPAGE_UN_LOCK = 6;
}

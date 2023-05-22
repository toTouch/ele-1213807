package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/4/6 10:54
 * @mood
 */
@Data
public class CarLockCtrlHistoryVo {
    
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
    
}

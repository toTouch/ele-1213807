package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (UserBatteryServiceFee)表实体类
 *
 * @author zzlong
 * @since 2022-12-06 13:39:50
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_battery_service_fee")
public class UserBatteryServiceFee {
    
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户id
     */
    private Long uid;
    
    /**
     * 服务费订单id
     */
    private String batteryServiceFeeOrderId;
    
    /**
     * 电池服务费状态 (0--未支付1--已支付)
     */
    private Integer serviceFeeStatus;
    
    /**
     * 电池服务费产生时间
     */
    private Long serviceFeeGenerateTime;
    
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}

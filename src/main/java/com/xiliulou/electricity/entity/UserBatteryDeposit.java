package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


/**
 * (UserBatteryDeposit)表实体类
 *
 * @author zzlong
 * @since 2022-12-06 13:40:21
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_battery_deposit")
public class UserBatteryDeposit {
    
    /**
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * uid
     */
    private Long uid;
    
    /**
     * 交押金 对应的套餐id
     */
    private Long did;
    
    /**
     * 押金订单编号
     */
    private String orderId;
    
    /**
     * 押金金额
     */
    private BigDecimal batteryDeposit;
    
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
    
    /**
     * 押金类型
     */
    private Integer depositType;
    
    /**
     * 缴纳押金的时间
     */
    private Long applyDepositTime;
    
    private Integer depositModifyFlag;
    
    private BigDecimal beforeModifyDeposit;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    /**
     * 缴纳押金类型
     */
    public static final Integer DEPOSIT_TYPE_DEFAULT = 0;
    
    /**
     * 免押类型
     */
    public static final Integer DEPOSIT_TYPE_FREE = 1;
    
    public static final Integer DEPOSIT_MODIFY_NO = 0;
    
    public static final Integer DEPOSIT_MODIFY_YES = 1;
    
    /**
     * 为了兼容灵活续费将一部分缴纳押金与当前套餐不匹配的用户的押金修改为编辑过的状态，其标识为2
     */
    public static final Integer DEPOSIT_MODIFY_SPECIAL = 2;
}

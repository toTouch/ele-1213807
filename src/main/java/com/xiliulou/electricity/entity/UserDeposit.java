package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;


/**
 * (UserDeposit)表实体类
 *
 * @author zzlong
 * @since 2022-12-06 13:40:21
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_deposit")
public class UserDeposit {
    
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * uid
     */
    private Long uid;
    
    /**
     * 押金订单表id
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
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}

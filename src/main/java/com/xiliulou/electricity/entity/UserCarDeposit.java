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
 * (UserCarDeposit)表实体类
 *
 * @author zzlong
 * @since 2022-12-07 17:35:45
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_car_deposit")
public class UserCarDeposit {
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
     * 租车订单表id
     */
    private Long did;
    /**
     * 租车押金订单编号
     */
    private String orderId;
    /**
     * 押金金额
     */
    private BigDecimal carDeposit;

    /**
     * 押金类型
     */
    private Integer depositType;

    /**
     * 缴纳押金的时间
     */
    private Long applyDepositTime;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

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

}

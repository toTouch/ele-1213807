package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


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
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}

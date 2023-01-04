package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2022-12-20 15:10
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_member_card_failure_record")
public class MemberCardFailureRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 套餐名称
     */
    private String cardName;

    private Long uid;

    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 失效套餐类型 1:换电套餐,2:租车套餐
     */
    private Integer type;

    /**
     * 电池押金
     */
    private BigDecimal deposit;

    /**
     * 套餐到期时间
     */
    private Long memberCardExpireTime;

    /**
     * 车辆SN码
     */
    private String carSn;

    /**
     * 车辆型号名称
     */
    private String carModelName;

    /**
     * 车辆套餐id
     */
    private String carMemberCardOrderId;

    /**
     * 门店Id
     */
    private Long storeId;

    /**
     * 租车套餐类型
     */
    private String carMemberCardType;

    /**
     * 车辆租赁周期
     */
    private Integer validDays;

    private Long createTime;

    private Long updateTime;

    //租户id
    private Integer tenantId;

    public static final Integer FAILURE_TYPE_FOR_BATTERY = 1;
    public static final Integer FAILURE_TYPE_FOR_RENT_CAR = 2;


}

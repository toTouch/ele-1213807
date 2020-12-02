package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 15:10
 **/
@Data
@TableName("t_electricity_member_card")
public class ElectricityMemberCard {
    private Integer id;
    //代理商id
    private Integer agentId;
    //类型
    @NotNull(message = "套餐类型不能为空!")
    private Integer type;
    //原价
    @NotNull(message = "套餐原价不能为空!")

    private BigDecimal price;
    //优惠价
    @NotNull(message = "套餐活动价格不能为空!")
    private BigDecimal holidayPrice;
    //有效天数
    private Integer validDays;
    @NotNull(message = "使用次数不能为空!")
    //最大使用次数
    private Long maxUseCount;
    //状态
    private Integer status;
    private Long createTime;
    private Long updateTime;

    //禁用状态
    public static final Integer STATUS_UN_USEABLE = 1;
    //可用状态
    public static final Integer STATUS_USEABLE = 0;

    public static final Integer TYPE_MONTH = 0;
    public static final Integer TYPE_QUARTERLY = 1;
    public static final Integer TYPE_YEAR = 2;
    //无限制使用次数
    public static final Integer UN_LIMITED_COUNT = -1;


}

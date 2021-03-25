package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
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
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 套餐名称
     */
    @NotEmpty(message = "套餐名称不能为空!")
    private String name;
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

    //最大使用次数
    private Long maxUseCount;
    //是否限制使用次数  0:不限制,1:限制
    @NotNull(message = "是否限制使用次数不能为空!")
    private Integer limitCount;

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
    public static final Long UN_LIMITED_COUNT = -1L;


    public static final Integer UN_LIMITED_COUNT_TYPE = 0;

}

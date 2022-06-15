package com.xiliulou.electricity.vo;

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
public class ElectricityMemberCardVO {

    private Integer id;
    /**
     * 套餐名称
     */

    private String name;
    //类型

    private Integer type;
    //原价

    private BigDecimal price;
    //优惠价

    private BigDecimal holidayPrice;
    //有效天数
    private Integer validDays;

    //最大使用次数
    private Long maxUseCount;
    //是否限制使用次数  0:不限制,1:限制

    private Integer limitCount;

    //状态
    private Integer status;
    private Long createTime;
    private Long updateTime;

    //租户id
    private Integer tenantId;

    /**
     * franchiseeId
     */

    private Integer franchiseeId;

    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    /**
     * 加盟商套餐类型 1--老（不分型号） 2--新（分型号）
     * */

    private Integer modelType;

    /**
     * 电池类型套餐
     */
    private String batteryType;

    /**
     * 是否绑定活动 0--绑定  1--未绑定
     */

    private Integer isBindActivity;


    /**
     * 活动id
     */
    private Integer activityId;

    private OldUserActivityVO  oldUserActivityVO;

    /**
     * 套餐类型
     */
    private Integer cardModel;

    /**
     * 车辆型号Id
     */
    private Integer carModelId;

    /**
     * 车辆型号
     */
    private String carModel;



}

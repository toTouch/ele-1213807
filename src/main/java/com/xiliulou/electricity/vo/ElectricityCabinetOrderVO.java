package com.xiliulou.electricity.vo;

import lombok.Data;


/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
public class ElectricityCabinetOrderVO {

    private Long id;
    /**
    * 订单编号--时间戳+柜子id+仓门号+用户id+5位随机数,20190203 21 155 1232)
    */
    private String orderId;
    /**
    * 换电人手机号
    */
    private String phone;
    /**
    * 换电人id
    */
    private Long uid;
    /**
    * 支付金额
    */
    private Double payAmount;
    /**
    * 换电柜id
    */
    private Long electricityCabinetId;
    /**
    * 老电池编号
    */
    private String oldElectricityBatterySn;
    /**
    * 新电池编号
    */
    private String newElectricityBatterySn;
    /**
    * 换电柜的旧仓门号
    */
    private Integer oldCellNo;
    /**
    * 换电柜的新仓门号
    */
    private Integer newCellNo;
    /**
    * 订单的状态
    */
    private Integer status;
    /**
    * 支付方式 1--月卡抵扣 2--年卡抵扣
    */
    private Integer paymentMethod;
    /**
    * 下单的来源 1--微信公众号 2--小程序
    */
    private Integer source;
    /**
    * 备注
    */
    private String remark;
    /**
    * 换电开始时间
    */
    private Long switchBeginningTime;
    /**
    * 换电结束时间
    */
    private Long switchEndTime;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;


}
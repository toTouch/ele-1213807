package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;



/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_order")
public class ElectricityCabinetOrder {

    @TableId(value = "id",type = IdType.AUTO)
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
    private Integer electricityCabinetId;
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
    * 类型(0:月卡,1:季卡,2:年卡)
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
    private Long switchBeginTime;
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

    //已支付未开门
    public static final Integer STATUS_ORDER_PAY = 1;
    //旧电池开门
    public static final Integer STATUS_ORDER_OLD_BATTERY_OPEN_DOOR = 2;
    //旧电池检测(旧电池关门)
    public static final Integer STATUS_ORDER_OLD_BATTERY_DETECT = 3;
    //旧电池已存入
    public static final Integer STATUS_ORDER_OLD_BATTERY_DEPOSITED = 4;
    //新电池开门
    public static final Integer STATUS_ORDER_NEW_BATTERY_OPEN_DOOR = 5;
    //订单完成(新电池关门)
    public static final Integer STATUS_ORDER_COMPLETE = 6;
    //订单异常结束
    public static final Integer STATUS_ORDER_EXCEPTION_CANCEL = 7;
    //订单取消
    public static final Integer STATUS_ORDER_CANCEL = 8;


}
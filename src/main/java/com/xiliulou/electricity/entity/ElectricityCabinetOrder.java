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

    @TableId(value = "id", type = IdType.AUTO)
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

    //订单状态序号
    private Double orderSeq;
    /**
     * 订单的状态
     */
    private String status;
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
    //租户id
    private Integer tenantId;

    private Long franchiseeId;

    //门店id
    private Long storeId;

    /**
     * 换电订单生成 0.0
     */
    public static final String INIT = "INIT";
    /**
     * 换电订单生成 0.0
     */
    public static final Double STATUS_INIT = 0.0;
    /**
     * 换电过程放入没电电池检测失败 1.1
     */
    public static final String INIT_CHECK_FAIL = "INIT_CHECK_FAIL";
    /**
     * 换电柜放入没电电池开门发现有电池存在 2.1
     */
    public static final String INIT_CHECK_BATTERY_EXISTS = "INIT_CHECK_BATTERY_EXISTS";
    /**
     * 换电柜放入没电电池开门成功 3.0
     */
    public static final String INIT_OPEN_SUCCESS = "INIT_OPEN_SUCCESS";
    /**
     * 换电柜放入没电电池开门失败 3.1
     */
    public static final String INIT_OPEN_FAIL = "INIT_OPEN_FAIL";
    /**
     * 换电柜检测没电电池成功 4.0
     */
    public static final String INIT_BATTERY_CHECK_SUCCESS = "INIT_BATTERY_CHECK_SUCCESS";
    /**
     * 换电柜检测没电电池成功 4.0
     */
    public static final Double STATUS_INIT_BATTERY_CHECK_SUCCESS = 4.0;
    /**
     * 换电柜检测没电电池失败 4.1
     */
    public static final String INIT_BATTERY_CHECK_FAIL = "INIT_BATTERY_CHECK_FAIL";
    /**
     * 换电柜检测没电电池超时 4.2
     */
    public static final String INIT_BATTERY_CHECK_TIMEOUT = "INIT_BATTERY_CHECK_TIMEOUT";


    /**
     * 新旧门状态区分
     */
    public static final Double STATUS_CHECK_OLD_AND_NEW = 5.0;

    /**
     * 换电柜开满电电池前置检测失败 5.1
     */
    public static final String COMPLETE_CHECK_FAIL = "COMPLETE_CHECK_FAIL";
    /**
     * 换电柜开满电电池发现电池不存在 5.2
     */
    public static final String COMPLETE_CHECK_BATTERY_NOT_EXISTS = "COMPLETE_CHECK_BATTERY_NOT_EXISTS";
    /**
     * 换电柜开满电电池仓门成功 6.0
     */
    public static final String COMPLETE_OPEN_SUCCESS = "COMPLETE_OPEN_SUCCESS";

    /**
     * 换电柜开满电电池仓门成功 6.0
     */
    public static final Double STATUS_COMPLETE_OPEN_SUCCESS = 6.0;
    /**
     * 换电柜开满电电池仓门失败 6.1
     */
    public static final String COMPLETE_OPEN_FAIL = "COMPLETE_OPEN_FAIL";
    /**
     * 换电柜满电电池成功取走，流程结束 7.0
     */
    public static final String COMPLETE_BATTERY_TAKE_SUCCESS = "COMPLETE_BATTERY_TAKE_SUCCESS";
    /**
     * 换电柜取走满电电池超时 7.1
     */
    public static final String COMPLETE_BATTERY_TAKE_TIMEOUT = "COMPLETE_BATTERY_TAKE_TIMEOUT";


    //订单取消 11.0
    public static final String ORDER_CANCEL = "ORDER_CANCEL";

    //订单取消 11.0
    public static final Double STATUS_ORDER_CANCEL = 11.0;

    //订单异常结束 12.0
    public static final String ORDER_EXCEPTION_CANCEL = "ORDER_EXCEPTION_CANCEL";

    //订单异常结束 12.0
    public static final Double STATUS_ORDER_EXCEPTION_CANCEL = 12.0;


    //月卡
    public static final Integer PAYMENT_METHOD_MONTH_CARD = 0;
    //季卡
    public static final Integer PAYMENT_METHOD_SEASON_CARD = 1;
    //年卡
    public static final Integer PAYMENT_METHOD_YEAR_CARD = 2;

    public static final Integer SELF_EXCHANGE_ELECTRICITY = 1;

    public static final Integer SELF_EXCHANGE_ELECTRICITY_UNUSABLE_CELL = 2;
    public static final Integer SELF_EXCHANGE_ELECTRICITY_NOT_BATTERY_SN = 3;

    /**
     * 订单来源 自助开仓
     */
    public static final Integer ORDER_SOURCE_FOR_SELF_OPEN_CELL = 4;


}

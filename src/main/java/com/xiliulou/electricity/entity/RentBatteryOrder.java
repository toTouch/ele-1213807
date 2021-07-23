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
 * 租电池记录(TRentBatteryOrder)实体类
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_rent_battery_order")
public class RentBatteryOrder {


    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 订单Id
     */
    private String orderId;

    private Long uid;
    /**
    * 用户名
    */
    private String name;
    /**
    * 手机号
    */
    private String phone;
    /**
    * 电池编号
    */
    private String electricityBatterySn;
    /**
    * 租电池押金
    */
    private BigDecimal batteryDeposit;
    /**
     * 换电柜id
     */
    private Integer electricityCabinetId;
    /**
     * 仓门号
     */
    private Integer cellNo;
    /**
     * 订单类型(1--租电池,2--还电池,3--后台绑电池,4--后台解绑电池)
     */
    private Integer type;
    //订单状态序号
    private Double orderSeq;
    /**
     * 订单的状态
     */
    private String status;
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

    //租电池
    public static final Integer TYPE_USER_RENT = 1;
    //还电池
    public static final Integer TYPE_USER_RETURN = 2;

    public static final Integer TYPE_WEB_BIND = 3;

    public static final Integer TYPE_WEB_UNBIND = 4;


    //初始化
    public static final Double STATUS_INIT = 0.0;

    //初始化
    public static final String INIT = "INIT";


    /**
     * 租电池前置检测 1.1
     */
    public static final String RENT_INIT_CHECK="RENT_INIT_CHECK";
    /**
     * 租电池格挡是空仓 1.2
     */
    public static final String RENT_BATTERY_NOT_EXISTS="RENT_BATTERY_NOT_EXISTS";

    /**
     * 租电池开门成功  2.0
     */
    public static final String RENT_OPEN_SUCCESS="RENT_OPEN_SUCCESS";
    /**
     * 租电池开门失败 2.1
     */
    public static final String RENT_OPEN_FAIL="RENT_OPEN_FAIL";
    /**
     * 租电池成功取走 3.0
     */
    public static final String RENT_BATTERY_TAKE_SUCCESS="RENT_BATTERY_TAKE_SUCCESS";
    /**
     * 租电池超时 3.1
     */
    public static final String RENT_BATTERY_TAKE_TIMEOUT="RENT_BATTERY_TAKE_TIMEOUT";


    /**
     * 状态开门成功  2.0
     */
    public static final Double STATUS_OPEN_SUCCESS=2.0;

    /**
     * 状态开门  2.0
     */
    public static final Double STATUS_OPEN_FAIL=3.0;

    /**
     * 还电池前置检测 1.1
     */
    public static final String RETURN_INIT_CHECK="RETURN_INIT_CHECK";
    /**
     * 还电池仓内有电池 1.2
     */
    public static final String RETURN_BATTERY_EXISTS="RETURN_BATTERY_EXISTS";

    /**
     * 还电池开门成功 2.0
     */
    public static final String RETURN_OPEN_SUCCESS="RETURN_OPEN_SUCCESS";
    /**
     * 还电池开门失败 2.1
     */
    public static final String RETURN_OPEN_FAIL="RETURN_OPEN_FAIL";
    /**
     * 还电池成功 3.0
     */
    public static final String RETURN_BATTERY_CHECK_SUCCESS="RETURN_BATTERY_CHECK_SUCCESS";
    /**
     * 还电池检测失败 3.1
     */
    public static final String RETURN_BATTERY_CHECK_FAIL="RETURN_BATTERY_CHECK_FAIL";
    /**
     * 还电池检测超时 3.2
     */
    public static final String RETURN_BATTERY_CHECK_TIMEOUT="RETURN_BATTERY_CHECK_TIMEOUT";


    //订单取消
    public static final Double STATUS_ORDER_CANCEL = 5.0;

    //订单取消
    public static final String ORDER_CANCEL = "ORDER_CANCEL";


    //订单异常结束
    public static final Double STATUS_ORDER_EXCEPTION_CANCEL = 6.0;

    //订单异常结束
    public static final String ORDER_EXCEPTION_CANCEL = "ORDER_EXCEPTION_CANCEL";

}

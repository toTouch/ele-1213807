package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)实体类
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_order_oper_history")
public class ElectricityCabinetOrderOperHistory {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
    * 订单id
    */
    private Long oId;
    /**
     * 订单编号--时间戳+柜子id+仓门号+用户id+5位随机数,20190203 21 155 1232)
     */
    private String orderId;
    /**
     *订单的类型 1--换电 2--租电 3--还电
     */
    private Integer orderType;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 换电柜id
    */
    private Integer electricityCabinetId;
    /**
    * 换电柜的格挡号
    */
    private Integer cellNo;
    /**
    * 操作订单的状态
    */
    private Integer status;
    /**
    * 下单的用户
    */
    private Long uid;
    /**
     *操作订单的类型 1--旧电池开门 2--旧电池关门 3--旧电池检测不通过开门 4--新电池开门 5--新电池关门
     */
    private Integer type;

    //错误信息
    private String msg;

    //开门系统错误
    public static final Integer STATUS_ERROR = 1;
    //开门成功
    public static final Integer STATUS_OPEN_DOOR_SUCCESS = 2;
    //开门失败
    public static final Integer STATUS_OPEN_DOOR_FAIL = 3;
    //开门订单不匹配
    public static final Integer STATUS_EXISTS_ORDER_ILLEGAL = 4;
    //电池检测失败
    public static final Integer STATUS_BATTERY_CHECK_ERROR = 1;
    //电池检测成功
    public static final Integer STATUS_BATTERY_CHECK_SUCCESS = 2;


    //旧电池开门
    public static final Integer TYPE_OLD_BATTERY_OPEN_DOOR = 1;
    //旧电池检测
    public static final Integer TYPE_OLD_BATTERY_CHECK = 2;
    //新电池开门
    public static final Integer TYPE_NEW_BATTERY_OPEN_DOOR = 3;
    //新电池检测
    public static final Integer TYPE_NEW_BATTERY_CHECK = 4;
    //租电池开门
    public static final Integer TYPE_RENT_BATTERY_OPEN_DOOR = 5;
    //租电池检测
    public static final Integer TYPE_RENT_BATTERY_CHECK = 6;
    //还电池开门
    public static final Integer TYPE_RETURN_BATTERY_OPEN_DOOR = 7;
    //还电池检测
    public static final Integer TYPE_RETURN_BATTERY_CHECK = 8;


    //换电
    public static final Integer ORDER_TYPE_ELE = 1;
    //租电
    public static final Integer  ORDER_TYPE_RENT = 2;
    //还电
    public static final Integer  ORDER_TYPE_RETURN = 3;


}
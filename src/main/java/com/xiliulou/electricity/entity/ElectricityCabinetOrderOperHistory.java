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
    * 创建时间
    */
    private Long createTime;
    /**
    * 换电柜id
    */
    private Long electricityCabinetId;
    /**
    * 换电柜的格挡号
    */
    private Integer cellNo;
    /**
    * 操作订单的状态 1--旧电池开门 2--旧电池关门 3--旧电池检测不通过开门 4--新电池开门 5--新电池关门
    */
    private Object status;
    /**
    * 下单的用户
    */
    private Integer uid;

    //旧电池开门
    public static final Integer STATUS_OLD_BATTERY_OPEN_DOOR = 1;
    //旧电池关门
    public static final Integer STATUS_OLD_BATTERY_CLOSE_DOOR = 2;
    //旧电池弹出
    public static final Integer STATUS_OLD_BATTERY_WEB_OPEN_DOOR = 3;
    //新电池开门
    public static final Integer STATUS_NEW_BATTERY_OPEN_DOOR = 4;
    //新电池关门
    public static final Integer STATUS_NEW_BATTERY_CLOSE_DOOR = 5;


}
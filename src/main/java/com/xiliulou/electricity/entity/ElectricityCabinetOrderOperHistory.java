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
    private Integer electricityCabinetId;
    /**
    * 换电柜的格挡号
    */
    private Integer cellNo;
    /**
    * 操作订单的状态 0--成功 1--失败
    */
    private Object status;
    /**
    * 下单的用户
    */
    private Long uid;
    /**
     *操作订单的类型 1--旧电池开门 2--旧电池关门 3--旧电池检测不通过开门 4--新电池开门 5--新电池关门
     */
    private Integer type;

    //状态--成功
    public static final Integer STATUS_SUCCESS = 0;
    //失败
    public static final Integer STATUS_FAIL = 1;

    //旧电池开门
    public static final Integer TYPE_OLD_BATTERY_OPEN_DOOR = 1;
    //旧电池检测
    public static final Integer TYPE_OLD_BATTERY_CHECK = 2;
    //新电池开门
    public static final Integer TYPE_NEW_BATTERY_OPEN_DOOR = 3;
    //新电池检测
    public static final Integer TYPE_NEW_BATTERY_CHECK = 4;


}
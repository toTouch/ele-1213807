package com.xiliulou.electricity.vo;
import lombok.Builder;
import lombok.Data;

/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)实体类
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
@Data
@Builder
public class ElectricityCabinetOrderOperHistoryVO {

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
     * 换电柜id
     */
    private Integer electricityCabinetName;
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


}
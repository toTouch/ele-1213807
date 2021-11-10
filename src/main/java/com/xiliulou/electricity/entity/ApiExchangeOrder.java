package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (ApiExchangeOrder)实体类
 *
 * @author Eclair
 * @since 2021-11-10 14:10:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_api_exchange_order")
public class ApiExchangeOrder {

    private Long id;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 柜机id
     */
    private Integer eid;
    /**
     * 放入的电池编号
     */
    private String putBatterySn;
    /**
     * 取走的电池编号
     */
    private String takeBatterySn;
    /**
     * 电池类型
     */
    private String batteryType;
    /**
     * 放入电池的仓门
     */
    private Integer putCellNo;
    /**
     * 取走的电池编号
     */
    private Integer takeCellNo;
    /**
     * 订单状态
     */
    private String status;
    /**
     * 订单序列号
     */
    private Double orderSeq;

    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    /**
     * 下单成功
     */
    public static final String STATUS_INIT = "INIT";
    /**
     * | 放入电池开门失败 |
     */
    public static final String STATUS_PLACE_OPEN_FAIL = "PLACE_OPEN_FAIL";

    /**
     * 换电柜放入没电电池开门成功
     */
    public static final String STATUS_PLACE_OPEN_SUCCESS = "PLACE_OPEN_SUCCESS";

    /**
     * 放入电池超时
     */
    public static final String STATUS_PLACE_TIME_OUT = "PLACE_TIME_OUT";
    /**
     * 放入电池不匹配
     */
    public static final String STATUS_PLACE_BATTERY_NOT_MATCH = "PLACE_BATTERY_NOT_MATCH";
    /**
     * 放入电池不属于系统
     */
    public static final String STATUS_PLACE_BATTERY_NOT_BELONG = "PLACE_BATTERY_NOT_BELONG";

    /**
     * 换电柜检测没电电池成功
     */
    public static final String STATUS_PLACE_BATTERY_CHECK_SUCCESS = "PLACE_BATTERY_CHECK_SUCCESS";

    /**
     * 换电柜开满电电池仓门成功
     */
    public static final String STATUS_TAKE_OPEN_SUCCESS = "TAKE_OPEN_SUCCESS";

    /**
     * 取电池超时
     */
    public static final String STATUS_TAKE_TIME_OUT = "TAKE_TIME_OUT";

    /**
     * 取电池开门失败
     */
    public static final String STATUS_TAKE_OPEN_FAIL = "TAKE_OPEN_FAIL";

    /**
     * 换电柜满电电池成功取走，流程结束
     */
    public static final String STATUS_TAKE_BATTERY_SUCCESS = "TAKE_BATTERY_SUCCESS";

}

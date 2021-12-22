package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (ApiReturnOrder)实体类
 *
 * @author Eclair
 * @since 2021-11-10 10:15:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_api_return_order")
public class ApiReturnOrder {

    private Long id;

    private String orderId;

    private Integer tenantId;
    /**
     * 归还的电池编号
     */
    private String batterySn;
    /**
     * 电池类型
     */
    private String batteryType;
    /**
     * 换电柜id
     */
    private Integer eid;
    /**
     * 柜门号
     */
    private Integer cellNo;
    /**
     * 订单序列号
     */
    private Double orderSeq;
    /**
     * 订单编号
     */
    private String status;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    /**
     * 下单成功
     */
    public static final String STATUS_INIT = "INIT";
    /**
     * 异常订单
     */
    public static final String STATUS_EXCEPTION_ORDER = "EXCEPTION_ORDER";

    /**
     * 开门失败
     */
    public static final String STATUS_RETURN_OPEN_FAIL = "RETURN_OPEN_FAIL";

    /**
     * 退电池超时
     */
    public static final String STATUS_RETURN_TIME_OUT = "RETURN_TIME_OUT";

    /**
     * 放入电池不属于系统
     */
    public static final String STATUS_RETURN_BATTERY_NOT_BELONG = "RETURN_BATTERY_NOT_BELONG";

    /**
     * 退电池开门成功
     */
    public static final String STATUS_RETURN_OPEN_SUCCESS = "RETURN_OPEN_SUCCESS";

    /**
     * 退电池检测电池不匹配
     */
    public static final String STATUS_RETURN_BATTERY_NOT_MATCH = "RETURN_BATTERY_NOT_MATCH";

    /**
     * 退电池成功
     */
    public static final String STATUS_RETURN_BATTERY_CHECK_SUCCESS = "RETURN_BATTERY_CHECK_SUCCESS";

}

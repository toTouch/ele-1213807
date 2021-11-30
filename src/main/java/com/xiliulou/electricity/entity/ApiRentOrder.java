package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (ApiRentOrder)实体类
 *
 * @author Eclair
 * @since 2021-11-09 13:32:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_api_rent_order")
public class ApiRentOrder {

    private Long id;

    private String orderId;

    private Integer tenantId;
    /**
     * 租借的电池编号
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
     * 开始下单
     */
    public static String STATUS_INIT = "INIT";
    /**
     * 异常订单
     */
    public static String STATUS_EXCEPTION_ORDER = "EXCEPTION_ORDER";
    /**
     * 开门失败
     */
    public static String STATUS_RENT_OPEN_FAIL = "RENT_OPEN_FAIL";
    /**
     * 租电池超时
     */
    public static String STATUS_RENT_TIME_OUT = "RENT_TIME_OUT";
    /**
     * 租电池开门成功
     */
    public static String STATUS_RENT_OPEN_SUCCESS = "RENT_OPEN_SUCCESS";
    /**
     * 租电池成功取走
     */
    public static String STATUS_RENT_BATTERY_TAKE_SUCCESS = "RENT_BATTERY_TAKE_SUCCESS";


}

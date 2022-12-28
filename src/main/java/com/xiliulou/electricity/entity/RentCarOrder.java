package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 租车订单表(RentCarOrder)表实体类
 *
 * @author zzlong
 * @since 2022-12-21 09:47:57
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_rent_car_order")
public class RentCarOrder {

    private Long id;
    /**
     * 订单Id
     */
    private String orderId;

    private Long uid;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 车辆编号
     */
    private String carSn;
    /**
     * 租车押金
     */
    private Double carDeposit;
    /**
     * 车辆型号
     */
    private Long carModelId;
    /**
     * 订单类型(1--租车,2--还车)
     */
    private Integer type;
    /**
     * 交易方式
     */
    private Integer transactionType;
    /**
     * 订单状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 创建时间
     */
    private Long updateTime;
    /**
     * 门店id
     */
    private Long storeId;
    /**
     * 加盟商id
     */
    private Long franchiseeId;

    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final Integer STATUS_SUCCESS = 0;
    public static final Integer STATUS_FAIL = 1;

    //0:租车  1：还车
    public static final Integer TYPE_RENT = 0;
    public static final Integer TYPE_RETURN = 1;

    public static final Integer TYPE_TRANSACTION_ONLINE = 0;
    public static final Integer TYPE_TRANSACTION_OFFLINE = 1;

}

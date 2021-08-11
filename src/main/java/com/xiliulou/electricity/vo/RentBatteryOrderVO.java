package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 租电池记录(TRentBatteryOrder)实体类
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
@Data
public class RentBatteryOrderVO {


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
     * 换电柜名称
     */
    private String electricityCabinetName;
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


}

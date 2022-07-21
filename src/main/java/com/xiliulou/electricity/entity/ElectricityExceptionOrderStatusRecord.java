package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 订单表(TElectricityExceptionOrderStatusRecord)实体类
 *
 * @author makejava
 * @since 2022-07-21 16:00:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_exception_order_status_record")
public class ElectricityExceptionOrderStatusRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 订单编号
     */
    private String orderId;

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

    //租户id
    private Integer tenantId;

}

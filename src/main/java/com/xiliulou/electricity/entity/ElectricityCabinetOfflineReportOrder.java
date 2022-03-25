package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * (TElectricityCabinetOfflineReportOrder)实体类
 *
 * @author Hrp
 * @since 2022-03-11 14:27:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_offline_report_order")
public class ElectricityCabinetOfflineReportOrder {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 创建时间
     */
    private Long createTime;

}

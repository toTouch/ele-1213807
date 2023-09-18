package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/8/7 11:29
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_car_rental_and_refund_protocol")
public class CarRentalAndRefundProtocol {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 内容
     */
    private String content;

    private Long createTime;

    private Long updateTime;

    private Long tenantId;

}

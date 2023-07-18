package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/11 10:37
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_esign_capacity_recharge_record")
public class EsignCapacityRechargeRecord {

    private Long id;

    private Integer esignCapacity;

    private Long operator;

    private Long tenantId;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;


}

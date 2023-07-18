package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/11 10:34
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_esign_capacity_data")
public class EsignCapacityData {

    private Long id;

    private Integer esignCapacity;

    private Long rechargeTime;

    private Long tenantId;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;


}

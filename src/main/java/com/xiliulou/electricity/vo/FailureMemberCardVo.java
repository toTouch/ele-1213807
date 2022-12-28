package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 失效套餐表(memberCardFailureRecord)实体类
 *
 * @author makejava
 * @since 2022-12-19 10:16:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureMemberCardVo {

    private Long id;
    /**
    * 押金
    */
    private BigDecimal batteryDeposit;
    /**
    * 用户Id
    */
    private Long uid;


    //租户id
    private Integer tenantId;


    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 套餐过期时间
     */
    private Long memberCardExpireTime;


}

package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 换电柜电池表(InsuranceOrder)实体类
 *
 * @author makejava
 * @since 2022-11-04 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsuranceOrderVO {

    private String orderId;

    private String franchiseeName;

    private String insuranceName;

    private Integer insuranceType;

    private String userName;

    private String idCard;

    private String phone;

    private Integer cid;

    private Long insuranceExpireTime;

    private BigDecimal insuranceAmount;

    private Long createTime;

    private Integer validDays;

    private String cityName;

    private Integer status;

    private BigDecimal forehead;
}

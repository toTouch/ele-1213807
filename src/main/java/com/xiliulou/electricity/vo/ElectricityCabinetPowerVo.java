package com.xiliulou.electricity.vo;


import java.math.BigDecimal;
import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 换电柜电池表(ElectricityCabinetPower)实体类
 *
 * @author makejava
 * @since 2022-08-24 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityCabinetPowerVo {

    private Long id;

    private Integer electricityCabinetId;

    private BigDecimal sameDayPower;

    private BigDecimal sumPower;

    private Date date;

    private Long createTime;

    private Long updateTime;

    private String electricityCabinetName;

}

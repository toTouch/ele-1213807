package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 换电柜电量表(ElectricityCabinetPower)实体类
 *
 * @author Eclair
 * @since 2021-01-27 16:22:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_power")
public class ElectricityCabinetPower {
    /**
    * 主键Id
    */
    private Long id;
    /**
    * 换电柜Id
    */
    private Long eid;
    /**
    * 当天使用电量
    */
    private Double sameDayPower;
    /**
    * 总共使用电量
    */
    private Double sumPower;
    /**
    * 日期
    */
    private LocalDate date;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
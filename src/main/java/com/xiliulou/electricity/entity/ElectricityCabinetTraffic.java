package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * zgw
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_traffic")
public class ElectricityCabinetTraffic {

    private Long id;
    /**
     * 换电柜Id
     */
    private Long eid;
    /**
     * 当天使用电量
     */
    private Double sameDayTraffic;
    /**
     * 总共使用电量
     */
    private Double sumTraffic;
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

    //租户id
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}

package com.xiliulou.electricity.entity;


import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Arrays;
import java.util.List;

/**
 * (DivisionAccountBatteryMembercard)实体类
 *
 * @author Eclair
 * @since 2023-04-23 17:59:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_division_account_battery_membercard")
public class DivisionAccountBatteryMembercard {

    private Long id;
    /**
     * 分帐配置id
     */
    private Long divisionAccountId;
    /**
     * 套餐id
     */
    private Long refId;

    private Integer type;

    private Integer tenantId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    /**
     * 业务类型， 1-换电套餐， 2-租车套餐， 3-车电一体
     */
    public static final Integer TYPE_BATTERY = 1;

    public static final Integer TYPE_CAR_RENTAL = 2;

    public static final Integer TYPE_CAR_BATTERY = 3;

    public static final List<Integer> PACKAGE_TYPES = Lists.newArrayList(TYPE_BATTERY,TYPE_CAR_RENTAL,TYPE_CAR_BATTERY);

}

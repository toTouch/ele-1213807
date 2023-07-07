package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (MemberCardBatteryType)实体类
 *
 * @author Eclair
 * @since 2023-07-07 14:07:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_member_card_battery_type")
public class MemberCardBatteryType {

    private Long id;
    /**
     * 电池型号
     */
    private String batteryType;
    /**
     * 套餐Id
     */
    private Long mid;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 租户id
     */
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}

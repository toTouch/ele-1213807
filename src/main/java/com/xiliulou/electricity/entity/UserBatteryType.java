package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserBatteryType)实体类
 *
 * @author Eclair
 * @since 2023-07-14 16:02:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_battery_type")
public class UserBatteryType {
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户id
     */
    private Long uid;
    /**
     * 电池类型
     */
    private String batteryType;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}

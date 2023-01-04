package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (EleBatterySnapshot)实体类
 *
 * @author Eclair
 * @since 2023-01-04 09:21:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_battery_snapshot")
public class EleBatterySnapshot {
    
    private Long id;
    
    private Integer eId;
    
    private String jsonBatteries;
    
    private Long createTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}

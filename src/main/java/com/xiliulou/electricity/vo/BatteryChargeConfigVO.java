package com.xiliulou.electricity.vo;

import com.google.api.client.util.Lists;
import com.xiliulou.electricity.dto.BatteryMultiConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-14-14:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryChargeConfigVO {
    
    private Long id;
    /**
     * 柜机模式
     */
    private String applicationModel;
    /**
     * 电池充电配置
     */
    private List<BatteryMultiConfigDTO> configList= Lists.newArrayList();
    /**
     * 柜机id
     */
    private Long electricityCabinetId;
    
    private Long createTime;
    
    private Long updateTime;
    
}

package com.xiliulou.electricity.query;

import com.xiliulou.electricity.dto.BatteryMultiConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-12-15:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryChargeConfigQuery {

    private Long id;
    /**
     * 柜机模式
     */
    private String applicationModel;
    /**
     * 电池充电配置
     */
    private List<BatteryMultiConfigDTO> configList;

    @NotNull(message = "electricityCabinetId不能为空!")
    private Long electricityCabinetId;
    /**
     * 租户id
     */
    private Integer tenantId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;
}

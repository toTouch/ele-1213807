package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-11-11:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryModelQuery {
    private Long size;

    private Long offset;

    private Integer tenantId;

    @NotNull(message = "id不能为空!", groups = {UpdateGroup.class})
    private Long id;

    @NotNull(message = "电池材质不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Long mid;
    /**
     * 电池型号
     */
    @NotBlank(message = "电池型号不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String batteryType;
    /**
     * 电池电压
     */
    @NotNull(message = "电池电压不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Double batteryV;
    /**
     * 电池短型号
     */
    @NotBlank(message = "电池短型号不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String batteryVShort;
}

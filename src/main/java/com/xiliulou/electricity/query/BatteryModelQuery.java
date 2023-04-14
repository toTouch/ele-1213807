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

    private String batteryType;

    @NotNull(message = "id不能为空!", groups = {UpdateGroup.class})
    private Long id;

    @NotNull(message = "电池材质不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Long mid;

    /**
     * 电池电压
     */
    @NotNull(message = "电池电压不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer standardV;

    /**
     * 默认充电电压
     */
    @NotNull(message = "默认充电电压电压不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Double chargeV;

    /**
     * 电池串数
     */
    @NotNull(message = "电池串数不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer number;

}

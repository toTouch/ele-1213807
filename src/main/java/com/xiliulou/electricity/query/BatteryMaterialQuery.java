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
 * @date 2023-04-11-14:13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryMaterialQuery {
    private Long size;

    private Long offset;

    @NotNull(message = "id不能为空!", groups = {UpdateGroup.class})
    private Long id;
    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    /**
     * 类型
     */
    @NotBlank(message = "类型不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String type;

}

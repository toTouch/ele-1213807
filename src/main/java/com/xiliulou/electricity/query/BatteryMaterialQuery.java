package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;

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

    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Length(min = 1, max = 20, message = "名称不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    /**
     * 类型
     */
    @NotBlank(message = "类型不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Length(min = 1, max = 20, message = "类型不合法", groups = {CreateGroup.class, UpdateGroup.class})
    @Pattern(regexp = "^\\w+$", message = "类型不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private String type;

    @NotNull(message = "材料体系不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Range(min = 0, max = 20, message = "材料体系不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer kind;

}

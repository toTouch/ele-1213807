package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-14-14:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreTagQuery {
    private Long size;
    private Long offset;
    /**
     * Id
     */
    @NotNull(message = "id不能为空", groups = UpdateGroup.class)
    private Long id;
    /**
     * 标签
     */
    @NotEmpty(message = "标签名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String title;
    /**
     * 门店Id
     */
    @NotNull(message = "门店id不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long storeId;
    /**
     * 排序
     */
    @NotNull(message = "排序不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer seq;
    /**
     * 状态(0为启用,1为禁用)
     */
    private Integer status;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer tenantId;


}

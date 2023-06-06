package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-01-16:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationActivityQuery {

    private Long size;
    private Long offset;

    private Integer tenantId;

    @NotBlank(message = "活动id不能为空", groups = {UpdateGroup.class})
    private Long id;

    private Integer status;

    @NotBlank(message = "活动名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 1, max = 50, message = "活动名称不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    /**
     * 描述
     */
    private String description;
    /**
     * 有效时间
     */
    @NotBlank(message = "有效时间不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer hours;

    /**
     * 奖励类型  1--固定金额  2--套餐比例
     */
    private Integer discountType;

    /**
     * 首次购买返现
     */
    @NotBlank(message = "首次购买返现不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal firstReward;

    /**
     * 非首次购买返现
     */
    @NotBlank(message = "非首次购买返现不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal otherReward;

    /**
     * 可参与活动的套餐
     */
    @NotEmpty(message = "套餐不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private List<Long> membercardIds;
}

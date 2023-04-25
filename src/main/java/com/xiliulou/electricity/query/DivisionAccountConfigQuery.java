package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-23-18:20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionAccountConfigQuery {
    private Long size;

    private Long offset;

    private Integer tenantId;

    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 分帐配置名称
     */
    @NotBlank(message = "分帐配置名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    /**
     * 分帐层级
     */
    @NotNull(message = "分帐层级不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Range(min = 1, max = 2, message = "分帐层级不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer hierarchy;
    /**
     * 加盟商id
     */
    @NotNull(message = "加盟商不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long franchiseeId;
    /**
     * 门店id
     */
    private Long storeId;
    /**
     *
     */
    @NotNull(message = "运营商收益率不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal operatorRate;
    /**
     * 加盟商收益率
     */
    @NotNull(message = "加盟商收益率不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal franchiseeRate;
    /**
     * 门店收益率
     */
    private BigDecimal storeRate;
    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;
    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Range(min = 1, max = 2, message = "业务类型不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer type;

    private List<Long> membercards;

    private List<Long> carModels;

    private Integer isAll;

}

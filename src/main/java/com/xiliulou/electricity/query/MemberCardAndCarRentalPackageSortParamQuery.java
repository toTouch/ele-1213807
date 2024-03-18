package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author  SongJinpan
 * @description:
 * @Date  2024/03/11 15:25
 */
@Data
public class MemberCardAndCarRentalPackageSortParamQuery {
    
    /**
     * 套餐id
     */
    @NotNull(message = "套餐id不能为空")
    private Long id;
    
    /**
     * 套餐排序参数
     */
    @NotNull(message = "套餐排序参数不能为空")
    private Integer sortParam;
}

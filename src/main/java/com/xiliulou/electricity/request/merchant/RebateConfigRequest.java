package com.xiliulou.electricity.request.merchant;

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
 * @date 2024-02-05-9:30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RebateConfigRequest {
    
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 商户等级
     */
    @NotBlank(message = "商户等级不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String level;
    
    /**
     * 套餐Id
     */
    @NotNull(message = "套餐不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long mid;
    
    /**
     * 渠道员拉新返现
     */
    private Double channelerInvitation;
    
    /**
     * 渠道员续费返现
     */
    private Double channelerRenewal;
    
    /**
     * 商户拉新返现
     */
    private Double merchantInvitation;
    
    /**
     * 商户续费返现
     */
    private Double merchantRenewal;
    
    /**
     * 状态 0:关闭,1:开启
     */
    private Integer status;
    
    private Integer delFlag;
}

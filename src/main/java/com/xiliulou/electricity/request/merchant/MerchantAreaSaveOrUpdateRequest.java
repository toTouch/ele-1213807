package com.xiliulou.electricity.request.merchant;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author HeYafeng
 * @description
 * @date 2024/2/6 13:42:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantAreaSaveOrUpdateRequest {
    
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 区域名称
     */
    @Size(min = 1, max = 10, message = "区域名称不合法", groups = {CreateGroup.class, UpdateGroup.class})
    @NotBlank(message = "区域名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    /**
     * 备注
     */
    @Size(max = 50, message = "备注内容超出最大限制50字", groups = {CreateGroup.class, UpdateGroup.class})
    private String remark;
    
    @NotNull(message = "加盟商id不能为空", groups = {CreateGroup.class})
    private Long franchiseeId;
    
    /**
     * 绑定加盟商id
     */
    private Long bindFranchiseeId;
}

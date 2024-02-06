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
 * @description TODO
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
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 备注
     */
    @Size(max = 50, message = "备注内容超出最大限制50字", groups = {CreateGroup.class, UpdateGroup.class})
    private String remark;
}

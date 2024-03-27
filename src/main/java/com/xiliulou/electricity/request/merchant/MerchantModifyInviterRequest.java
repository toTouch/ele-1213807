package com.xiliulou.electricity.request.merchant;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @description 修改邀请人请求
 * @date 2024/3/27 10:37:25
 */
@Data
public class MerchantModifyInviterRequest {
    
    @NotNull(message = "用户uid不能为空", groups = {UpdateGroup.class})
    private Long uid;
    
    @NotNull(message = "现邀请人uid不能为空", groups = {UpdateGroup.class})
    private Long oldInviterUid;
    
    @NotBlank(message = "现邀请人名称不能为空", groups = {UpdateGroup.class})
    private String oldInviterName;
    
    @NotNull(message = "现邀请人来源不能为空", groups = {UpdateGroup.class})
    private Integer oldInviterSource;
    
    @NotNull(message = "修改后邀请人(商户)id不能为空", groups = {UpdateGroup.class})
    private Long merchantId;
    
    @Range(min = 1, max = 100, message = "修改原因输入不合法", groups = {UpdateGroup.class})
    @NotBlank(message = "修改原因不能为空", groups = {UpdateGroup.class})
    private String remark;
}

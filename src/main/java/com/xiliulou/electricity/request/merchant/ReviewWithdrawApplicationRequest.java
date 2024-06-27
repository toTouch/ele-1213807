package com.xiliulou.electricity.request.merchant;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 14:59
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReviewWithdrawApplicationRequest {
    
    @NotNull(message = "请选择审批条目")
    private Long id;
    
    @NotNull(message = "审批状态不能为空")
    private Integer status;
    
    private String remark;
    
    private Long bindFranchiseeId;
    
}

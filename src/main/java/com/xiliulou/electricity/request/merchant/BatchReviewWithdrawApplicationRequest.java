package com.xiliulou.electricity.request.merchant;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 15:00
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BatchReviewWithdrawApplicationRequest {
    
    @NotEmpty(message = "请选择审批条目")
    private List<Long> ids;
    
    @NotNull(message = "审批状态不能为空")
    private Integer status;
    
    private String remark;
    
    private String batchNo;
    
    private Integer tenantId;
    
    /**
     * 登录用户绑定的加盟商id
     */
    private List<Long> bindFranchiseeIdList;
}

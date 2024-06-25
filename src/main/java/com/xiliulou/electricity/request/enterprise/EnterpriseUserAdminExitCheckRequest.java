package com.xiliulou.electricity.request.enterprise;

import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author maxiaodong
 * @date 2024/1/11 11:48
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EnterpriseUserAdminExitCheckRequest {
    /**
     * 被邀请用户ID
     */
    @NotNull(message = "用户id不能为空")
    private Long uid;
    
    /**
     * 解绑原因
     * @see RenewalStatusEnum
     */
    @NotNull(message = "解绑原因不能为空")
    private String reason;
    
    private Long enterpriseId;
}

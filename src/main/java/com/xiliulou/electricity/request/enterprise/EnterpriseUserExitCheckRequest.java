package com.xiliulou.electricity.request.enterprise;

import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
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
public class EnterpriseUserExitCheckRequest {
    /**
     * 被邀请用户ID
     */
    @NotNull(message = "用户id不能为空", groups = {UpdateGroup.class})
    private Long uid;
    
    /**
     * 自主续费状态 0:不自主续费, 1:自主续费
     * @see RenewalStatusEnum
     */
    @NotNull(message = "自主续费状态不能为空", groups = {CreateGroup.class})
    private Integer renewalStatus;
}

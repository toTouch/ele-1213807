package com.xiliulou.electricity.request.tenantNote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author maxiaodong
 * @date 2023/12/28 9:58
 * @desc
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantRechargeRequest {
    /**
     * 主键ID
     */
    @NotNull(message = "[运营商ID]不能为空")
    private Integer tenantId;
    
    /**
     * 充值次数
     */
    @NotNull(message = "次数不能为空")
    @Range(min = 1, message = "次数必须大于零")
    private Long rechargeNum;
}

package com.xiliulou.electricity.queryModel.supper;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @description 清除用户删除标记请求（测试环境内部使用）
 * @date 2025/2/28 11:09:03
 */
@Builder
@Data
public class ClearUserDelMarkQueryModel {
    
    /**
     * 租户ID
     */
    @NotNull(message = "租户ID不能为空")
    private Integer tenantId;
    
    /**
     * 清除手机号标记
     */
    private String phoneDelMark;
    
    /**
     * 清除身份证号标记
     */
    private String idNumberDelMark;
    
}

package com.xiliulou.electricity.query.enterprise;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-18-17:12
 */
@Data
public class UserCloudBeanRechargeQuery {
    /**
     * 企业云豆总数
     */
    @NotNull(message = "企业云豆总数不能为空")
    private BigDecimal totalBeanAmount;
}

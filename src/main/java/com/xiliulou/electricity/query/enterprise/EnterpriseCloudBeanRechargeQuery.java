package com.xiliulou.electricity.query.enterprise;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-11:19
 */
@Data
public class EnterpriseCloudBeanRechargeQuery {

    @NotNull(message = "id不能为空")
    private Long id;
    /**
     * 操作类型 0:赠送,1:后台充值,2:后台扣除
     */
    @NotNull(message = "类型不能为空")
    private Integer type;
    /**
     * 企业云豆总数
     */
    @NotNull(message = "企业云豆总数不能为空")
    private Double totalBeanAmount;
    /**
     * 备注
     */
    private String remark;


}

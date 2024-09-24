package com.xiliulou.electricity.query;


import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * Description: This class is FreeDepositFyRequest!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/26
 **/
@Data
public class FreeDepositFyRequest {
    
    /**
     * <p>
     *    Description: 租户id
     * </p>
    */
    @NotNull(message = "租户不能为空!")
    private Integer tenantId;
    
    /**
     * <p>
     *    Description: 分期签约次数
     * </p>
    */
    @Min(value = 1,message = "充值次数不能小于1!")
    private Integer byStagesCapacity;
    
    /**
     * <p>
     *    Description: 蜂云免押次数
     * </p>
    */
    @Min(value = 1,message = "充值次数不能小于1!")
    private Integer freeDepositCapacity;
}

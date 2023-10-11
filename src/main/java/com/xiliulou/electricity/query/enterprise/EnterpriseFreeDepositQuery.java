package com.xiliulou.electricity.query.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/11 16:21
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseFreeDepositQuery {
    
    @NotNull(message = "骑手id不能为空")
    private Long uid;
    
    @NotNull(message = "套餐id不能为空")
    private Long membercardId;
    
    @NotEmpty(message = "手机号不能为空")
    private String phoneNumber;
    
    @NotEmpty(message = "身份证不能为空")
    private String idCard;
    
    @NotEmpty(message = "真实姓名不能为空")
    private String realName;
    
    private String productKey;
    
    private String deviceName;
    
}

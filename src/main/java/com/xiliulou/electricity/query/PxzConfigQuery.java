package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author : eclair
 * @date : 2023/2/16 11:26
 */
@Data
public class PxzConfigQuery {
    @NotEmpty(message = "aes不能为空")
    private String aesKey;
    
    @NotEmpty(message = "商户code不可以为空")
    private String merchantCode;
}

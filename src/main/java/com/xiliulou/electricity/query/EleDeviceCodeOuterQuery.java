package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleDeviceCodeOuterQuery {
    
    @NotBlank(message = "productKey不能为空")
    @Length(min = 1, max = 32,message = "参数不合法")
    private String productKey;
    
    @NotBlank(message = "deviceName不能为空")
    @Length(min = 1, max = 32,message = "参数不合法")
    private String deviceName;
    
    private String deviceSecret;
}

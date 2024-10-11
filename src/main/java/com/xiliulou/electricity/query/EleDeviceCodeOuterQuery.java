package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleDeviceCodeOuterQuery {
    
    @NotBlank(message = "productKey不能为空")
    private String productKey;
    
    @NotBlank(message = "deviceName不能为空")
    private String deviceName;
}

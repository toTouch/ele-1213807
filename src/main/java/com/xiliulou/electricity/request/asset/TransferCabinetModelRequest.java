package com.xiliulou.electricity.request.asset;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TransferCabinetModelRequest {
    @NotBlank(message = "productKey不能为空!")
    private String productKey;
    
    @NotBlank(message = "deviceName不能为空!")
    private String deviceName;
}

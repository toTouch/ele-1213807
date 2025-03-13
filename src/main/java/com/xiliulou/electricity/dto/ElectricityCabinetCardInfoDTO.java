package com.xiliulou.electricity.dto;


import lombok.Data;

/**
 * @author : renhang
 * @description ElectricityCabinetCardInfoDTO
 * @date : 2025-03-13 16:07
 **/
@Data
public class ElectricityCabinetCardInfoDTO {

    private String tenantName;

    private String cabinetName;

    private String cardNumber;

    private Long createTime;

    private Long expireTime;

    private String dataBalance;

    private String dataTrafficAmount;

    private String code;

    private String expiryDate;

    private String codeType;
}

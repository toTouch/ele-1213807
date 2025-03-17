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

    private String createTime;

    /**
     * 柜机过期时间
     */
    private String expireTime;

    private String dataBalance;

    private String dataTrafficAmount;

    private String code;

    /**
     * 卡过期时间
     */
    private String expiryDate;

    private String codeType;
}

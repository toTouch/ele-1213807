package com.xiliulou.electricity.bo;


import lombok.Data;

/**
 * @author : renhang
 * @description ElectricityCabinetCardInfoBO
 * @date : 2025-03-13 14:32
 **/
@Data
public class ElectricityCabinetCardInfoBO {


    private String tenantName;

    private String cabinetName;

    private String cardNumber;

    private Long createTime;

    private Long expireTime;
}

package com.xiliulou.electricity.dto;


import lombok.Builder;
import lombok.Data;

/**
 * @author : renhang
 * @description CabinetCardDTO
 * @date : 2025-03-13 15:24
 **/
@Data
@Builder
public class CabinetCardDTO {

    private String dataBalance;

    private String dataTrafficAmount;

    private String code;

    private String expiryDate;

    private Integer codeType;
}

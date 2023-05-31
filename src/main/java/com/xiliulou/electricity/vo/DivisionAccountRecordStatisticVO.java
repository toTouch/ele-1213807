package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-25-10:43
 */
@Data
public class DivisionAccountRecordStatisticVO {

    private Long divisionAccountConfigId;


    private String divisionAccountConfigName;
    /**
     * 运营商收益
     */
    private Double operatorIncome;
    private String operatorName;
    /**
     * 加盟商收益
     */
    private Double franchiseeIncome;
    private String franchiseeName;
    /**
     * 门店收益
     */
    private Double storeIncome;
    private String storeName;


}

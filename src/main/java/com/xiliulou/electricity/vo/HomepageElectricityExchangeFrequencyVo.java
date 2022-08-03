package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author hrp
 * @date 2022/08/03 14:22
 * @mood 首页换电频次分析
 */
@Data
public class HomepageElectricityExchangeFrequencyVo {

    /**
     * 换电总频次
     */
    private Integer sumFrequency;

    /**
     * 换电柜名称
     */
    private String electricityName;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 换电频次
     */
    private Integer exchangeFrequency;

    /**
     * 换电柜Id
     */
    private Integer eleId;

    /**
     * 门店Id
     */
    private Long storeId;
}

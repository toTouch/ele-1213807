package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/7/18 10:38
 */
@Data
public class ChargeConfigVo {

    private String name;

    private Long id;

    private String jsonRule;

    private String franchiseeName;

    private Long franchiseeId;

    private String storeName;

    private Long storeId;

    private String electricityName;

    private Long eid;

    private Long createTime;

    private Integer type;
}

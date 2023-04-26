package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-23-18:20
 */
@Data
public class DivisionAccountConfigVO {

    private Long id;
    /**
     * 分帐配置名称
     */
    private String name;
    /**
     * 分帐层级
     */
    private Integer hierarchy;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    private String  franchiseeName;
    /**
     * 门店id
     */
    private Long storeId;
    private String storeName;

    private BigDecimal operatorRate;
    private BigDecimal franchiseeRate;
    private BigDecimal storeRate;
    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;
    /**
     * 业务类型
     */
    private Integer type;

    private List<String> membercardNames;

    private List<Long> membercardIds;

    private Long updateTime;



}

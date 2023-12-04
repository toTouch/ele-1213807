package com.xiliulou.electricity.dto.asset;

import lombok.Data;

import java.util.List;

/**
 * 柜机批量出库
 */
@Data
public class CabinetBatchOutWarehouseDTO {
    
    /**
     * 柜机ID
     */
    private List<Integer> idList;
    
    /**
     * 加盟商
     */
    private Long franchiseeId;
    
    /**
     * 门店
     */
    private Long storeId;
    
    /**
     * 换电柜地址
     */
    private String address;
    
    /**
     * 地址经度
     */
    private Double longitude;
    
    /**
     * 地址纬度
     */
    private Double latitude;
    
    /**
     * 柜机名称
     */
    private String name;
    
    /**
     * 门店
     */
    private Long updateTime;
}

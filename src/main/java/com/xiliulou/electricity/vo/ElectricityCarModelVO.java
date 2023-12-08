package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 缴纳电池服务费订单表(tEleBatteryServiceFeeOrder)实体类
 *
 * @author makejava
 * @since 2022-04-19 10:16:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityCarModelVO {

    /**
     * 车辆型号Id
     */
    private Integer id;
    
    /**
     * 厂家名称
     */
    private String manufacturerName;

    /**
     * 型号名称
     */
    private String name;

    /**
     * 加盟商Id
     */
    private Long franchiseeId;
    /**
     * 加盟商名称
     */
    private String franchiseeName;
    /**
     * 租赁方式
     */
    private String rentType;

    private Integer delFlag;
    /**
     * 租车押金
     */
    private BigDecimal carDeposit;
    /**
     * 其它参数
     */
    private String otherProperties;

    /**
     * 已租数量
     */
    private Integer rentedQuantity;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    /**
     * 门店Id
     */
    private Long storeId;

    private String storeName;


    private Integer tenantId;

    /**
     * 车辆型号图片
     */
    List<PictureVO> pictures;
    /**
     * 车辆标签
     */
    List<String> carModelTag;


}

package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 门店商品表(StoreShops)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
public class StoreGoodsVO {

    List<ElectricityCabinetFile> electricityCabinetFiles;

    private Long id;

    private Long storeId;

    /**
     * 门店商品名称
     */
    private String name;

    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    private BigDecimal price;

    private BigDecimal discountPrice;

    private Integer tenantId;

    /**
     * 车辆型号
     */
    private String carModel;

    /**
     * 车辆型号Id
     */
    private Integer carModelId;

    /**
     * 车辆库存
     */
    private Integer carInventory;

}

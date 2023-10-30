package com.xiliulou.electricity.vo.recommend;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 车辆型号推荐 VO
 *
 * @author xiaohui.song
 **/
@Data
public class CarModelRecommendVO implements Serializable {

    private static final long serialVersionUID = -4196356263744952946L;

    /**
     * 车辆型号标签
     */
    List<String> carModelTagNames;

    /**
     * 车辆型号ID
     */
    private Integer carModelId;

    /**
     * 车辆型号名称
     */
    private String carModelName;

    /**
     * 车辆型号已租数量
     */
    private Integer carModelRentedQuantity;

    /**
     * 车辆型号创建时间
     */
    private Long carModelCreateTime;

    /**
     * 车辆型号图片地址
     */
    private String carModelPictureOSSUrl;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 门店距离(m)
     */
    private Double storeDistance;
}

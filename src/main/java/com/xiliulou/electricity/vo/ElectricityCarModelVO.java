package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.Picture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Integer delFlag;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    private Integer tenantId;

    /**
     * 车辆型号图片
     */
    List<Picture> pictures;

}

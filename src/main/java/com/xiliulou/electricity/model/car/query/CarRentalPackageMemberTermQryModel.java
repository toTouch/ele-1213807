package com.xiliulou.electricity.model.car.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐会员期限表，DB层查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageMemberTermQryModel implements Serializable {

    private static final long serialVersionUID = 9161102960471862351L;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 取值数量
     */
    private Integer size = 10;

    /**
     * 租户ID
     */
    private Integer tenantId;
}

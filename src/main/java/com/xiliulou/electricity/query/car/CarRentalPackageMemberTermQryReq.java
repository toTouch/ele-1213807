package com.xiliulou.electricity.query.car;

import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐会员期限表，查询模型
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageMemberTermQryReq implements Serializable {

    private static final long serialVersionUID = -969190155200865583L;

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

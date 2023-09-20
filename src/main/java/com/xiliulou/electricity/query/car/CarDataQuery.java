package com.xiliulou.electricity.query.car;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 车辆数据查询条件
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CarDataQuery implements Serializable {

    /**
     * 车辆sn
     */
    private String sn;

    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    /**
     * 门店ID
     */
    private Long storeId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 手机号码
     */
    private String phone;


    private Integer tenantId;
    /**
     * 型号id
     */
    private Integer modelId;

    /**
     * 车辆状态 0--空闲 1--租借
     */
    private Integer status;


    /**
     * 逾期时间
     */
    private Long overdueTime;

    private Long uid;

    /**
     * 加盟商Id
     */
    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}

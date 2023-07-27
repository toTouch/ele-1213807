package com.xiliulou.electricity.service.carmodel;

/**
 * 车辆型号业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarModelBizService {

    /**
     * 检测是否允许购买此车辆型号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param carModelId 车辆型号ID
     * @return true(允许)、false(不允许)
     */
    boolean checkBuyByCarModelId(Integer tenantId, Long uid, Integer carModelId);
}

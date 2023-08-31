package com.xiliulou.electricity.service.carmodel;

import com.xiliulou.electricity.vo.car.CarModelDetailVo;

/**
 * 车辆型号业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarModelBizService {

    /**
     * 根据车辆型号ID获取车辆型号信息<br />
     * 包含：基本信息、图片信息、门店信息
     * @param carModelId 车辆型号ID
     * @return 车辆型号详细信息
     */
    CarModelDetailVo queryDetailByCarModelId(Integer carModelId);

    /**
     * 检测是否允许购买此车辆型号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param carModelId 车辆型号ID
     * @return true(允许)、false(不允许)
     */
    boolean checkBuyByCarModelId(Integer tenantId, Long uid, Integer carModelId);
}

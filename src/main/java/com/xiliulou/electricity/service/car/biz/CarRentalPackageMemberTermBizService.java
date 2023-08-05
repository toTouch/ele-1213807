package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.electricity.vo.userinfo.UserMemberInfoVo;

/**
 * 租车套餐会员期限业务聚合 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageMemberTermBizService {

    /**
     * 根据用户ID获取会员的全量信息（套餐订单信息、保险信息、车辆信息、电池信息）
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 用户会员全量信息
     */
    UserMemberInfoVo queryUserMemberInfo(Integer tenantId, Long uid);

    /**
     * 根据用户ID获取当前用户的绑定车辆型号ID<br />
     * 可能为null
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return 车辆型号ID
     */
    Integer queryCarModelByUid(Integer tenantId, Long uid);

    /**
     * 套餐购买订单过期处理<br />
     * 用于定时任务
     * @param offset 偏移量
     * @param size 取值数量
     */
    void expirePackageOrder(Integer offset, Integer size);

}

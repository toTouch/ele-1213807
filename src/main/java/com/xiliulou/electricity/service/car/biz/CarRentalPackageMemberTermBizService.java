package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.electricity.reqparam.opt.carpackage.MemberCurrPackageOptReq;
import com.xiliulou.electricity.vo.userinfo.UserMemberInfoVo;

/**
 * 租车套餐会员期限业务聚合 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageMemberTermBizService {

    /**
     * 增加余量次数<br />
     * 只有状态正常，增加成功返回为true
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @return true(成功)、false(失败)
     */
    boolean addResidue(Integer tenantId, Long uid);

    /**
     * 扣减余量次数<br />
     * 只有状态正常，扣减成功返回为true
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @return true(成功)、false(失败)
     */
    boolean substractResidue(Integer tenantId, Long uid);

    /**
     * 判定租户的套餐是否过期<br />
     * 只有状态正常且过期，返回为true
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @return true(过期)、false(未过期)
     */
    boolean isExpirePackageOrder(Integer tenantId, Long uid);


    /**
     * 编辑会员当前套餐信息
     * @param tenantId 租户ID
     * @param optReq 操作数据模型
     * @param optUid 操作用户UID
     * @return true(成功)、false(失败)
     */
    boolean updateCurrPackage(Integer tenantId, MemberCurrPackageOptReq optReq, Long optUid);


    /**
     * 根据用户ID获取会员的全量信息（套餐订单信息、车辆信息）
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

package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ShareActivityRule;

import java.util.List;

/**
 * 加盟商活动绑定表(TActivityBindCoupon)表服务接口
 *
 * @author makejava
 * @since 2021-04-23 16:43:23
 */
public interface ShareActivityRuleService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivityRule queryByIdFromDB(Long id);


    /**
     * 新增数据
     *
     * @param shareActivityRule 实例对象
     * @return 实例对象
     */
    ShareActivityRule insert(ShareActivityRule shareActivityRule);

    /**
     * 修改数据
     *
     * @param shareActivityRule 实例对象
     * @return 实例对象
     */
    Integer update(ShareActivityRule shareActivityRule);


    List<ShareActivityRule> queryByActivity(Integer id);

    ShareActivityRule queryByCouponId(Integer activityId, Integer couponId);

    void updateByCoupon(Integer id);

    void updateByActivity(Integer id);
}

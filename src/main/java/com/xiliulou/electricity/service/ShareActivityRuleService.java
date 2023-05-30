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
     * 新增数据
     *
     * @param shareActivityRule 实例对象
     * @return 实例对象
     */
    ShareActivityRule insert(ShareActivityRule shareActivityRule);


    List<ShareActivityRule> queryByActivity(Integer id);


    void updateByActivity(Integer id);

    ShareActivityRule selectByCouponId(Long id);
}

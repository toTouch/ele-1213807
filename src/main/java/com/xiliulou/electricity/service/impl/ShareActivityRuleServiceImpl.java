package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.ShareActivityRule;
import com.xiliulou.electricity.mapper.ShareActivityRuleMapper;
import com.xiliulou.electricity.service.ShareActivityRuleService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 加盟商活动绑定表(TActivityBindCoupon)表服务实现类
 *
 * @author makejava
 * @since 2021-04-23 16:43:23
 */
@Service("activityBindCouponService")
public class ShareActivityRuleServiceImpl implements ShareActivityRuleService {
    @Resource
    private ShareActivityRuleMapper shareActivityRuleMapper;



    /**
     * 新增数据
     *
     * @param shareActivityRule 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareActivityRule insert(ShareActivityRule shareActivityRule) {
        this.shareActivityRuleMapper.insert(shareActivityRule);
        return shareActivityRule;
    }


    @Override
    public List<ShareActivityRule> queryByActivity(Integer id) {
        return shareActivityRuleMapper.selectList(new LambdaQueryWrapper<ShareActivityRule>().eq(ShareActivityRule::getActivityId, id)
                .eq(ShareActivityRule::getDelFlg, ShareActivityRule.DEL_NORMAL));
    }



    @Override
    public void updateByActivity(Integer id) {
        shareActivityRuleMapper.updateByActivity(id,System.currentTimeMillis());
    }
}

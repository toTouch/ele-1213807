package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserAmountHistory;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserAmountHistoryMapper;
import com.xiliulou.electricity.query.UserAmountHistoryQuery;
import com.xiliulou.electricity.service.UserAmountHistoryService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.vo.UserAmountHistoryVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * (FranchiseeSplitAccountHistoryService)表服务实现类
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
@Service("userAmountHistoryService")
@Slf4j
public class UserAmountHistoryServiceImpl implements UserAmountHistoryService {
    @Resource
    private UserAmountHistoryMapper userAmountHistoryMapper;

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 新增数据
     *
     * @param userAmountHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAmountHistory insert(UserAmountHistory userAmountHistory) {
        this.userAmountHistoryMapper.insert(userAmountHistory);
        return userAmountHistory;
    }

    @Override
    public R queryList(UserAmountHistoryQuery userAmountHistoryQuery) {
        return R.ok(userAmountHistoryMapper.queryList(userAmountHistoryQuery));
    }

    @Override
    public R queryCount(UserAmountHistoryQuery userAmountHistoryQuery) {
        return R.ok(userAmountHistoryMapper.queryCount(userAmountHistoryQuery));
    }

    @Override
    public List<UserAmountHistoryVO> selectRewardList(UserAmountHistoryQuery userAmountHistoryQuery) {
        List<UserAmountHistoryVO> userAmountHistoryVOS = userAmountHistoryMapper.selectRewardList(userAmountHistoryQuery);
        if (CollectionUtils.isEmpty(userAmountHistoryVOS)) {
            return Collections.emptyList();
        }

        userAmountHistoryVOS.forEach(item -> {
            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getJoinUid());
            item.setJoinPhone(Objects.isNull(userInfo) ? "" : userInfo.getPhone());
        });

        return userAmountHistoryVOS;
    }
}

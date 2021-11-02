package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FranchiseeSplitAccountHistory;
import com.xiliulou.electricity.mapper.FranchiseeSplitAccountHistoryMapper;
import com.xiliulou.electricity.query.FranchiseeAccountQuery;
import com.xiliulou.electricity.service.FranchiseeSplitAccountHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * (FranchiseeSplitAccountHistoryService)表服务实现类
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
@Service("franchiseeSplitAccountHistoryService")
@Slf4j
public class FranchiseeSplitAccountHistoryServiceImpl implements FranchiseeSplitAccountHistoryService {
    @Resource
    private FranchiseeSplitAccountHistoryMapper franchiseeSplitAccountHistoryMapper;


    /**
     * 新增数据
     *
     * @param franchiseeSplitAccountHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FranchiseeSplitAccountHistory insert(FranchiseeSplitAccountHistory franchiseeSplitAccountHistory) {
        this.franchiseeSplitAccountHistoryMapper.insert(franchiseeSplitAccountHistory);
        return franchiseeSplitAccountHistory;
    }

    @Override
    public R queryList(FranchiseeAccountQuery franchiseeAccountQuery) {
        return R.ok(franchiseeSplitAccountHistoryMapper.queryList(franchiseeAccountQuery));
    }

    @Override
    public R queryCount(FranchiseeAccountQuery franchiseeAccountQuery) {
        return R.ok(franchiseeSplitAccountHistoryMapper.queryCount(franchiseeAccountQuery));
    }

}

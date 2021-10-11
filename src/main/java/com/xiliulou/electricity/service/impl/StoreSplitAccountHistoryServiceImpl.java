package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreSplitAccountHistory;
import com.xiliulou.electricity.mapper.StoreSplitAccountHistoryMapper;
import com.xiliulou.electricity.query.StoreAccountQuery;
import com.xiliulou.electricity.service.StoreSplitAccountHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * (StoreSplitAccountHistory)表服务实现类
 *
 * @author makejava
 * @since 2021-05-06 20:09:27
 */
@Service("storeSplitAccountHistoryService")
@Slf4j
public class StoreSplitAccountHistoryServiceImpl implements StoreSplitAccountHistoryService {
    @Resource
    private StoreSplitAccountHistoryMapper storeSplitAccountHistoryMapper;


    /**
     * 新增数据
     *
     * @param storeSplitAccountHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoreSplitAccountHistory insert(StoreSplitAccountHistory storeSplitAccountHistory) {
        this.storeSplitAccountHistoryMapper.insert(storeSplitAccountHistory);
        return storeSplitAccountHistory;
    }

    @Override
    public R queryList(StoreAccountQuery storeAccountQuery) {
        List<StoreSplitAccountHistory> storeSplitAccountHistoryList=storeSplitAccountHistoryMapper.queryList(storeAccountQuery);
        return R.ok(storeSplitAccountHistoryList);
    }

    @Override
    public R queryCount(StoreAccountQuery storeAccountQuery) {
        return R.ok(storeSplitAccountHistoryMapper.queryCount(storeAccountQuery));
    }


}

package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.StoreAmount;
import com.xiliulou.electricity.entity.StoreSplitAccountHistory;
import com.xiliulou.electricity.mapper.StoreAmountMapper;
import com.xiliulou.electricity.service.SplitAccountFailRecordService;
import com.xiliulou.electricity.service.StoreAmountService;
import com.xiliulou.electricity.service.StoreSplitAccountHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * (StoreAmount)表服务实现类
 *
 * @author makejava
 * @since 2021-05-06 20:09:26
 */
@Service("storeAmountService")
@Slf4j
public class StoreAmountServiceImpl implements StoreAmountService {
    @Resource
    private StoreAmountMapper storeAmountMapper;
    @Autowired
    RedisService redisService;

    @Autowired
    StoreSplitAccountHistoryService storeSplitAccountHistoryService;

    @Autowired
    SplitAccountFailRecordService splitAccountFailRecordService;


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public StoreAmount queryByStoreIdFromCache(Long id) {
        StoreAmount cacheStoreAmount = redisService.getWithHash(ElectricityCabinetConstant.CACHE_STORE_AMOUNT + id, StoreAmount.class);
        if (Objects.nonNull(cacheStoreAmount)) {
            return cacheStoreAmount;
        }

        StoreAmount storeAmount = storeAmountMapper.selectById(id);
        if (Objects.isNull(storeAmount)) {
            return null;
        }

        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE_AMOUNT + id, storeAmount);
        return storeAmount;
    }


    /**
     * 新增数据
     *
     * @param storeAmount 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoreAmount insert(StoreAmount storeAmount) {
        int i = this.storeAmountMapper.insert(storeAmount);
        if (i > 0) {
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE_AMOUNT + storeAmount.getStoreId(), storeAmount);
        }
        return storeAmount;
    }

    /**
     * 修改数据
     *
     * @param storeAmount 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(StoreAmount storeAmount) {
        int result = this.storeAmountMapper.updateById(storeAmount);
        if (result > 0) {
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_STORE_AMOUNT + storeAmount.getStoreId(), storeAmount);
        }
        return result;

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByStoreId(Long id) {
        int i = this.storeAmountMapper.deleteByStoreId(id);
        if (i > 0) {
            redisService.delete(ElectricityCabinetConstant.CACHE_STORE_AMOUNT + id);
        }
        return i > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Klock(name = "handleStoreSplitAccount", keys = {"#store.id"}, waitTime = 5, customLockTimeoutStrategy = "createStoreSplitAccountLockFail")
    public void handleSplitAccount(Store store, ElectricityTradeOrder payRecord, int percent) {
        StoreAmount storeAmount = queryByStoreIdFromCache(store.getId());
        if (Objects.isNull(storeAmount)) {
            log.error("ELE ORDER ERROR! not found storeAmount! storeId={}", store.getId());
            return;
        }
        BigDecimal payAmount = payRecord.getTotalFee();
        if (payAmount.doubleValue() < 0.01) {
            log.warn("ELE ORDER WARN,payAmount is less 0.01,storeId={},payAmount={}", store.getId(), payAmount);
            return;
        }

        BigDecimal shouldSplitPayAmount = payAmount.multiply(BigDecimal.valueOf(percent / 100.0));
        if (shouldSplitPayAmount.doubleValue() < 0.01) {
            log.warn("ELE ORDER WARN,split store account is less 0.01,storeId={},payAmount={},percent={}", store.getId(), payAmount, percent);
            return;
        }


        storeAmount.setBalance(storeAmount.getBalance().add(shouldSplitPayAmount));
        storeAmount.setTotalIncome(storeAmount.getTotalIncome().add(shouldSplitPayAmount));
        storeAmount.setUpdateTime(System.currentTimeMillis());
        update(storeAmount);

        StoreSplitAccountHistory history = StoreSplitAccountHistory.builder()
                .createTime(System.currentTimeMillis())
                .currentTotalIncome(storeAmount.getTotalIncome())
                .type(StoreSplitAccountHistory.TYPE_MEMBER)
                .orderId(payRecord.getOrderNo())
                .storeId(store.getId())
                .tenantId(payRecord.getTenantId())
                .splitAmount(shouldSplitPayAmount)
                .payAmount(payAmount)
                .percent(percent)
                .build();
        storeSplitAccountHistoryService.insert(history);
    }


}

package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeAmount;
import com.xiliulou.electricity.entity.FranchiseeSplitAccountHistory;
import com.xiliulou.electricity.entity.SplitAccountFailRecord;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.StoreAmount;
import com.xiliulou.electricity.entity.StoreSplitAccountHistory;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.StoreAmountMapper;
import com.xiliulou.electricity.query.StoreAccountQuery;
import com.xiliulou.electricity.service.FranchiseeSplitAccountHistoryService;
import com.xiliulou.electricity.service.SplitAccountFailRecordService;
import com.xiliulou.electricity.service.StoreAmountService;
import com.xiliulou.electricity.service.StoreSplitAccountHistoryService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.FranchiseeAmountVO;
import com.xiliulou.electricity.vo.StoreAmountVO;
import com.xiliulou.pay.weixinv3.dto.Amount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    UserService userService;



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

        StoreAmount storeAmount = storeAmountMapper.selectOne(new LambdaQueryWrapper<StoreAmount>().eq(StoreAmount::getStoreId,id));
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


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Klock(name = "handleStoreSplitAccount", keys = {"#store.id"}, waitTime = 5, customLockTimeoutStrategy = "createStoreSplitAccountLockFail")
    public void handleSplitAccount(Store store, ElectricityMemberCardOrder electricityMemberCardOrder, int percent) {
        StoreAmount storeAmount = queryByStoreIdFromCache(store.getId());
        if (Objects.isNull(storeAmount)) {
            log.error("ELE ORDER ERROR! not found storeAmount! storeId={}", store.getId());
            return;
        }
        BigDecimal payAmount = electricityMemberCardOrder.getPayAmount();
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
                .orderId(electricityMemberCardOrder.getOrderId())
                .storeId(store.getId())
                .tenantId(electricityMemberCardOrder.getTenantId())
                .splitAmount(shouldSplitPayAmount)
                .payAmount(payAmount)
                .percent(percent)
                .build();
        storeSplitAccountHistoryService.insert(history);
    }

    @Override
    public R queryList(StoreAccountQuery storeAccountQuery) {
        List<StoreAmount> storeAmountList=storeAmountMapper.queryList(storeAccountQuery);
        ArrayList<StoreAmountVO> list = new ArrayList<>();
        storeAmountList.forEach(item -> {
            StoreAmountVO storeAmountVO = new StoreAmountVO();
            BeanUtils.copyProperties(item, storeAmountVO);
            if (Objects.nonNull(item.getUid())) {
                User user = userService.queryByUidFromCache(item.getUid());
                if (Objects.nonNull(user)) {
                    storeAmountVO.setUserName(user.getName());
                }
            }
            list.add(storeAmountVO);
        });
        return R.ok(list);
    }

    @Override
    public R queryCount(StoreAccountQuery storeAccountQuery) {
        return R.ok(storeAmountMapper.queryCount(storeAccountQuery));
    }

    @Override
    public R modifyBalance(Long storeId, BigDecimal modifyBalance) {
        StoreAmount storeAmount = queryByStoreIdFromCache(storeId);
        if (Objects.isNull(storeAmount)) {
            return R.fail("ELECTRICITY.00111", "金额不存在！");
        }

        if (modifyBalance.compareTo(storeAmount.getBalance())>=0) {
            return R.fail("ELECTRICITY.00112", "修改余额不可以超过总余额！");
        }

        StoreAmount updateStoreAmount = new StoreAmount();
        updateStoreAmount.setId(storeAmount.getId());
        updateStoreAmount.setBalance(storeAmount.getBalance().add(modifyBalance));
        updateStoreAmount.setUpdateTime(System.currentTimeMillis());

        update(updateStoreAmount);


        StoreSplitAccountHistory history =  StoreSplitAccountHistory.builder()
                .type(StoreSplitAccountHistory.TYPE_OPERATOR)
                .storeId(storeId)
                .createTime(System.currentTimeMillis())
                .createTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId())
                .currentTotalIncome(storeAmount.getTotalIncome())
                .orderId("-1")
                .splitAmount(modifyBalance)
                .payAmount(BigDecimal.valueOf(0.00))
                .percent(-1)
                .build();
        storeSplitAccountHistoryService.insert(history);

        return R.ok();
    }

    @Override
    public void deleteByStoreId(Long id) {
        storeAmountMapper.deleteByStoreId(id);
    }

    private void createStoreSplitAccountLockFail(Store store, ElectricityTradeOrder payRecord, int percent) {
        log.error("ELE ORDER ERROR! handleSplitAccount error! storeId={}", store.getId());
        SplitAccountFailRecord record = SplitAccountFailRecord.builder()
                .accountId(store.getId())
                .payAmount(payRecord.getTotalFee().doubleValue())
                .createTime(System.currentTimeMillis())
                .type(SplitAccountFailRecord.TYPE_STORE)
                .percent(percent)
                .build();

        splitAccountFailRecordService.insert(record);
    }

}

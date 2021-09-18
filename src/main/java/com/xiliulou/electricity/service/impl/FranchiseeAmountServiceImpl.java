package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeAmount;
import com.xiliulou.electricity.entity.FranchiseeSplitAccountHistory;
import com.xiliulou.electricity.mapper.FranchiseeAmountMapper;
import com.xiliulou.electricity.service.FranchiseeAmountService;
import com.xiliulou.electricity.service.FranchiseeSplitAccountHistoryService;
import com.xiliulou.electricity.service.SplitAccountFailRecordService;
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
 * (FranchiseeAmount)表服务实现类
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
@Service("franchiseeAmountService")
@Slf4j
public class FranchiseeAmountServiceImpl implements FranchiseeAmountService {
    @Resource
    private FranchiseeAmountMapper franchiseeAmountMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    FranchiseeSplitAccountHistoryService franchiseeSplitAccountHistoryService;

    @Autowired
    SplitAccountFailRecordService splitAccountFailRecordService;


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param franchiseeId 主键
     * @return 实例对象
     */
    @Override
    public FranchiseeAmount queryByAgentIdFromCache(Long franchiseeId) {
        FranchiseeAmount cacheFranchiseeAmount = redisService.getWithHash(ElectricityCabinetConstant.CACHE_FRANCHISEE_AMOUNT + franchiseeId, FranchiseeAmount.class);
        if (Objects.nonNull(cacheFranchiseeAmount)) {
            return cacheFranchiseeAmount;
        }

        FranchiseeAmount franchiseeAmount = franchiseeAmountMapper.selectById(franchiseeId);
        if (Objects.isNull(franchiseeAmount)) {
            return null;
        }

        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_FRANCHISEE_AMOUNT + franchiseeId, franchiseeAmount);
        return franchiseeAmount;
    }


    /**
     * 新增数据
     *
     * @param franchiseeAmount 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FranchiseeAmount insert(FranchiseeAmount franchiseeAmount) {
        int result = this.franchiseeAmountMapper.insert(franchiseeAmount);
        if (result > 0) {
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_FRANCHISEE_AMOUNT + franchiseeAmount.getFranchiseeId(), franchiseeAmount);
        }
        return franchiseeAmount;
    }

    /**
     * 修改数据
     *
     * @param franchiseeAmount 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FranchiseeAmount franchiseeAmount) {
        int update = this.franchiseeAmountMapper.updateById(franchiseeAmount);
        if (update > 0) {
            redisService.delete(ElectricityCabinetConstant.CACHE_FRANCHISEE_AMOUNT + franchiseeAmount.getFranchiseeId());

        }
        return update;

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByAgentId(Long id) {
        int i = this.franchiseeAmountMapper.deleteByFranchiseeId(id);
        if (i > 0) {
            redisService.delete(ElectricityCabinetConstant.CACHE_FRANCHISEE_AMOUNT + id);
        }
        return i > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Klock(name = "handleAgentSplitAccount", keys = {"#agent.id"}, waitTime = 5, customLockTimeoutStrategy = "createAgentSplitAccountLockFail")
    public void handleSplitAccount(Franchisee franchisee, ElectricityTradeOrder payRecord,int percent) {
        FranchiseeAmount franchiseeAmount = queryByAgentIdFromCache(franchisee.getId());
        if (Objects.isNull(franchiseeAmount)) {
            log.error("ELE ORDER ERROR! not found franchiseeAmount! franchiseeId={}", franchisee.getId());
            return;
        }
        BigDecimal payAmount = payRecord.getTotalFee();
        if (payAmount.doubleValue() < 0.01) {
            log.warn("ELE ORDER WARN,payAmount is less 0.01,franchiseeId={},payAmount={}", franchisee.getId(), payAmount);
            return;
        }

        BigDecimal shouldSplitPayAmount = payAmount.multiply(BigDecimal.valueOf(percent / 100.0));
        if (shouldSplitPayAmount.doubleValue() < 0.01) {
            log.warn("ELE ORDER WARN,split store account is less 0.01,franchiseeId={},payAmount={},percent={}", franchisee.getId(), payAmount, percent);
            return;
        }

        franchiseeAmount.setBalance(franchiseeAmount.getBalance().add(shouldSplitPayAmount));
        franchiseeAmount.setTotalIncome(franchiseeAmount.getTotalIncome().add(shouldSplitPayAmount));
        franchiseeAmount.setUpdateTime(System.currentTimeMillis());
        update(franchiseeAmount);

        FranchiseeSplitAccountHistory history = FranchiseeSplitAccountHistory.builder()
                .createTime(System.currentTimeMillis())
                .currentTotalIncome(franchiseeAmount.getTotalIncome())
                .orderId(payRecord.getOrderNo())
                .franchiseeId(franchisee.getId())
                .type(FranchiseeSplitAccountHistory.TYPE_MEMBER)
                .tenantId(payRecord.getTenantId())
                .splitAmount(shouldSplitPayAmount)
                .payAmount(payAmount)
                .percent(percent)
                .build();
        franchiseeSplitAccountHistoryService.insert(history);
    }

}

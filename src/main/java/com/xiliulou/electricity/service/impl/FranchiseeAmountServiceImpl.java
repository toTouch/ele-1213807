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
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.FranchiseeAmountMapper;
import com.xiliulou.electricity.query.FranchiseeAccountQuery;
import com.xiliulou.electricity.service.FranchiseeAmountService;
import com.xiliulou.electricity.service.FranchiseeSplitAccountHistoryService;
import com.xiliulou.electricity.service.SplitAccountFailRecordService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.FranchiseeAmountVO;
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


    @Autowired
    UserService userService;


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param franchiseeId 主键
     * @return 实例对象
     */
    @Override
    public FranchiseeAmount queryByFranchiseeIdFromCache(Long franchiseeId) {
        FranchiseeAmount cacheFranchiseeAmount = redisService.getWithHash(ElectricityCabinetConstant.CACHE_FRANCHISEE_AMOUNT + franchiseeId, FranchiseeAmount.class);
        if (Objects.nonNull(cacheFranchiseeAmount)) {
            return cacheFranchiseeAmount;
        }

        FranchiseeAmount franchiseeAmount = franchiseeAmountMapper.selectOne(new LambdaQueryWrapper<FranchiseeAmount>().eq(FranchiseeAmount::getFranchiseeId,franchiseeId));
        if (Objects.isNull(franchiseeAmount)) {
            return null;
        }

        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_FRANCHISEE_AMOUNT + franchiseeId, franchiseeAmount);
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



    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Klock(name = "handleSplitAccount", keys = {"#franchisee.id"}, waitTime = 5, customLockTimeoutStrategy = "createFranchiseeSplitAccountLockFail")
    public void handleSplitAccount(Franchisee franchisee, ElectricityMemberCardOrder electricityMemberCardOrder,int percent) {
        log.info("payRecord ");
        FranchiseeAmount franchiseeAmount = queryByFranchiseeIdFromCache(franchisee.getId());
        if (Objects.isNull(franchiseeAmount)) {
            log.error("ELE ORDER ERROR! not found franchiseeAmount! franchiseeId={}", franchisee.getId());
            return;
        }
        BigDecimal payAmount = electricityMemberCardOrder.getPayAmount();
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
                .orderId(electricityMemberCardOrder.getOrderId())
                .franchiseeId(franchisee.getId())
                .type(FranchiseeSplitAccountHistory.TYPE_MEMBER)
                .tenantId(electricityMemberCardOrder.getTenantId())
                .splitAmount(shouldSplitPayAmount)
                .payAmount(payAmount)
                .percent(percent)
                .build();
        franchiseeSplitAccountHistoryService.insert(history);
    }

    @Override
    public R queryList(FranchiseeAccountQuery franchiseeAccountQuery) {
        List<FranchiseeAmount> franchiseeAmountList=franchiseeAmountMapper.queryList(franchiseeAccountQuery);
        ArrayList<FranchiseeAmountVO> list = new ArrayList<>();
        franchiseeAmountList.forEach(item -> {
            FranchiseeAmountVO franchiseeAmountVO = new FranchiseeAmountVO();
            BeanUtils.copyProperties(item, franchiseeAmountVO);
            if (Objects.nonNull(item.getUid())) {
                User user = userService.queryByUidFromCache(item.getUid());
                if (Objects.nonNull(user)) {
                    franchiseeAmountVO.setUserName(user.getName());
                }
            }
            list.add(franchiseeAmountVO);
        });
        return R.ok(list);
    }

    @Override
    public R queryCount(FranchiseeAccountQuery franchiseeAccountQuery) {
        return R.ok(franchiseeAmountMapper.queryCount(franchiseeAccountQuery));
    }

    @Override
    public void insert(FranchiseeAmount franchiseeAmount) {
        franchiseeAmountMapper.insert(franchiseeAmount);
    }

    @Override
    @Transactional
    public R modifyBalance(Long franchiseeId, BigDecimal modifyBalance) {
        FranchiseeAmount franchiseeAmount = queryByFranchiseeIdFromCache(franchiseeId);
        if (Objects.isNull(franchiseeAmount)) {
            return R.fail("ELECTRICITY.00111", "金额不存在！");
        }

        if (modifyBalance.compareTo(franchiseeAmount.getBalance())>=0) {
            return R.fail("ELECTRICITY.00112", "修改余额不可以超过总余额！");
        }

        FranchiseeAmount updateFranchiseeAmount = new FranchiseeAmount();
        updateFranchiseeAmount.setId(franchiseeAmount.getId());
        updateFranchiseeAmount.setBalance(franchiseeAmount.getBalance().add(modifyBalance));
        updateFranchiseeAmount.setUpdateTime(System.currentTimeMillis());
        updateFranchiseeAmount.setFranchiseeId(franchiseeId);

        update(updateFranchiseeAmount);


        FranchiseeSplitAccountHistory history =  FranchiseeSplitAccountHistory.builder()
                .type(FranchiseeSplitAccountHistory.TYPE_OPERATOR)
                .franchiseeId(franchiseeId)
                .createTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId())
                .currentTotalIncome(franchiseeAmount.getTotalIncome())
                .orderId("-1")
                .splitAmount(modifyBalance)
                .payAmount(BigDecimal.valueOf(0.00))
                .percent(-1)
                .build();
        franchiseeSplitAccountHistoryService.insert(history);

        return R.ok();
    }

    @Override
    public void deleteByFranchiseeId(Long id) {
        franchiseeAmountMapper.deleteByFranchiseeId(id);
    }

    private void createFranchiseeSplitAccountLockFail(Franchisee franchisee, ElectricityTradeOrder payRecord, int percent) {
        log.error("ELE ORDER ERROR! handleSplitAccount error! franchiseeId={}", franchisee.getId());
        SplitAccountFailRecord record = SplitAccountFailRecord.builder()
                .accountId(franchisee.getId())
                .payAmount(payRecord.getTotalFee().doubleValue())
                .createTime(System.currentTimeMillis())
                .type(SplitAccountFailRecord.TYPE_FRANCHISEE)
                .percent(percent)
                .build();

        splitAccountFailRecordService.insert(record);
    }



}

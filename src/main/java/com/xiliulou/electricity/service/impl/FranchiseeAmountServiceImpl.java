package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.entity.SplitAccountFailRecord;
import com.xiliulou.electricity.mapper.FranchiseeAmountMapper;
import com.xiliulou.electricity.service.FranchiseeAmountService;
import com.xiliulou.electricity.service.FranchiseeSplitAccountHistoryService;
import com.xiliulou.electricity.service.SplitAccountFailRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
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
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public AgentAmount queryByAgentFromDB(Long id) {
        return this.agentAmountMapper.queryByAgentId(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param agentId 主键
     * @return 实例对象
     */
    @Override
    public AgentAmount queryByAgentIdFromCache(Long agentId) {
        AgentAmount cacheAgent = redisService.getWithHash(LockerCabinetConstant.CACHE_AGENT_AMOUNT + agentId, AgentAmount.class);
        if (Objects.nonNull(cacheAgent)) {
            return cacheAgent;
        }

        AgentAmount agentAmount = queryByAgentFromDB(agentId);
        if (Objects.isNull(agentAmount)) {
            return null;
        }

        redisService.saveWithHash(LockerCabinetConstant.CACHE_AGENT_AMOUNT + agentId, agentAmount);
        return agentAmount;
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<AgentAmount> queryAllByLimit(int offset, int limit) {
        return this.agentAmountMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param agentAmount 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentAmount insert(AgentAmount agentAmount) {
        int result = this.agentAmountMapper.insertOne(agentAmount);
        if (result > 0) {
            redisService.saveWithHash(LockerCabinetConstant.CACHE_AGENT_AMOUNT + agentAmount.getAgentId(), agentAmount);
        }
        return agentAmount;
    }

    /**
     * 修改数据
     *
     * @param agentAmount 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(AgentAmount agentAmount) {
        int update = this.agentAmountMapper.update(agentAmount);
        if (update > 0) {
            redisService.delete(LockerCabinetConstant.CACHE_AGENT_AMOUNT + agentAmount.getAgentId());

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
        int i = this.agentAmountMapper.deleteByAgentId(id);
        if (i > 0) {
            redisService.delete(LockerCabinetConstant.CACHE_AGENT_AMOUNT + id);
        }
        return i > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Klock(name = "handleAgentSplitAccount", keys = {"#agent.id"}, waitTime = 5, customLockTimeoutStrategy = "createAgentSplitAccountLockFail")
    public void handleSplitAccount(AgentEntity agent, LockerOrderPayRecord payRecord, int agentPercent) {
        AgentAmount agentAmount = queryByAgentIdFromCache(agent.getId());
        if (Objects.isNull(agentAmount)) {
            log.error("LOCKER ORDER ERROR! not found agentAmount! agentId={}", agent.getId());
            return;
        }
        Double payAmount = payRecord.getPayAmount();
        if (payAmount < 0.01) {
            log.warn("LOCKER ORDER WARN,payAmount is less 0.01,agentId={},payAmount={}", agent.getId(), payAmount);
            return;
        }

        double shouldSplitPayAmount = BigDecimal.valueOf(payAmount).multiply(BigDecimal.valueOf(agentPercent / 100.0)).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue();
        if (shouldSplitPayAmount < 0.01) {
            log.warn("LOCKER ORDER WARN,split agent account is less 0.01,agentId={},payAmount={},percent={}", agent.getId(), payAmount, agentPercent);
            return;
        }

        agentAmount.setBalance(BigDecimal.valueOf(agentAmount.getBalance()).add(BigDecimal.valueOf(shouldSplitPayAmount)).doubleValue());
        agentAmount.setTotalIncome(BigDecimal.valueOf(agentAmount.getTotalIncome()).add(BigDecimal.valueOf(shouldSplitPayAmount)).doubleValue());
        agentAmount.setUpdateTime(System.currentTimeMillis());
        update(agentAmount);

        AgentSplitAccountHistory history = AgentSplitAccountHistory.builder()
                .createTime(System.currentTimeMillis())
                .currentTotalIncome(agentAmount.getTotalIncome())
                .oid(payRecord.getOid())
                .agentId(agent.getId())
                .splitRatio(agentPercent)
                .type(AgentSplitAccountHistory.TYPE_ORDER)
                .tenantId(payRecord.getTenantId())
                .splitAmount(shouldSplitPayAmount)
                .paySumAmount(payAmount)
                .payOrderId(payRecord.getPayId())
                .build();
        agentSplitAccountHistoryService.insert(history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Klock(name = "handleAgentSplitAccountByCombo", keys = {"#agent.id"}, waitTime = 5, customLockTimeoutStrategy = "createAgentSplitAccountByComboLockFail")
    public void handleSplitAccountByCombo(AgentEntity agent, UserComboPayRecordEntity payRecord, int agentPercent) {
        AgentAmount agentAmount = queryByAgentIdFromCache(agent.getId());
        if (Objects.isNull(agentAmount)) {
            log.error("COMBO ORDER ERROR! not found agentAmount! shopId={}", agent.getId());
            return;
        }
        Double payAmount = payRecord.getPayMoney().doubleValue();
        if (payAmount < 0.01) {
            log.warn("COMBO ORDER WARN,payAmount is less 0.01,agentId={},payAmount={}", agent.getId(), payAmount);
            return;
        }

        double shouldSplitPayAmount = BigDecimal.valueOf(payAmount).multiply(BigDecimal.valueOf(agentPercent / 100.0)).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue();
        if (shouldSplitPayAmount < 0.01) {
            log.warn("COMBO ORDER WARN,split agent account is less 0.01,agentId={},payAmount={},percent={}", agent.getId(), payAmount, agentPercent);
            return;
        }

        agentAmount.setBalance(BigDecimal.valueOf(agentAmount.getBalance()).add(BigDecimal.valueOf(shouldSplitPayAmount)).doubleValue());
        agentAmount.setTotalIncome(BigDecimal.valueOf(agentAmount.getTotalIncome()).add(BigDecimal.valueOf(shouldSplitPayAmount)).doubleValue());
        agentAmount.setUpdateTime(System.currentTimeMillis());
        update(agentAmount);

        AgentSplitAccountHistory history = AgentSplitAccountHistory.builder()
                .createTime(System.currentTimeMillis())
                .currentTotalIncome(agentAmount.getTotalIncome())
                .oid(payRecord.getOid())
                .agentId(agent.getId())
                .paySumAmount(payRecord.getPayMoney().doubleValue())
                .payOrderId(payRecord.getPayId())
                .splitRatio(agentPercent)
                .type(AgentSplitAccountHistory.TYPE_MEMBER)
                .tenantId(payRecord.getTenantId())
                .splitAmount(shouldSplitPayAmount)
                .build();
        agentSplitAccountHistoryService.insert(history);
    }

    @Override
    public List<AgentAmount> accountList(Integer size, Integer offset, Long startTime, Long endTime, Long agentId) {
        return this.agentAmountMapper.accountList(size, offset, startTime, endTime, agentId, TenantContextHolder.getTenantId());
    }

    @Override
    public int updateIdempotent(AgentAmount agentAmount, AgentAmount updateAgentAmount) {
        int result = agentAmountMapper.updateIdempontent(agentAmount, updateAgentAmount);
        if (result > 0) {
            redisService.delete(LockerCabinetConstant.CACHE_AGENT_AMOUNT + agentAmount.getAgentId());
        }
        return result;
    }

	@Override
	public AgentAmount queryByUid(Long uid) {
		return agentAmountMapper.selectOne(new LambdaQueryWrapper<AgentAmount>().eq(AgentAmount::getUid,uid));
	}

	@Override
	public void updateReduceIncome(Long uid, double income) {
		agentAmountMapper.updateReduceIncome(uid,income);
	}

	@Override
	public void updateRollBackIncome(Long uid, double income) {
		agentAmountMapper.updateRollBackIncome(uid,income);
	}

    private void createAgentSplitAccountLockFail(AgentEntity agent, LockerOrderPayRecord payAmount, int agentPercent) {
        log.error("LOCKER ORDER ERROR! handleSplitAccount error! agentId={}", agent.getId());
        SplitAccountFailRecord record = SplitAccountFailRecord.builder()
                .accountId(agent.getId())
                .payAmount(payAmount.getPayAmount())
                .createTime(System.currentTimeMillis())
                .type(SplitAccountFailRecord.TYPE_AGENT)
                .percent(agentPercent)
                .build();

        splitAccountFailRecordService.insert(record);
    }


    private void createAgentSplitAccountByComboLockFail(AgentEntity agent, UserComboPayRecordEntity payAmount, int agentPercent) {
        log.error("Combo ORDER ERROR! handleSplitAccount error! agentId={}", agent.getId());
        SplitAccountFailRecord record = SplitAccountFailRecord.builder()
                .accountId(agent.getId())
                .payAmount(payAmount.getPayMoney().doubleValue())
                .createTime(System.currentTimeMillis())
                .type(SplitAccountFailRecord.TYPE_AGENT)
                .percent(agentPercent)
                .build();

        splitAccountFailRecordService.insert(record);
    }
}

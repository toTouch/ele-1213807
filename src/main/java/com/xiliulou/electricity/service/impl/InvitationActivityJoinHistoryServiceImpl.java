package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jpay.util.StringUtils;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.InvitationActivityJoinHistory;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.InvitationActivityJoinHistoryMapper;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.request.activity.InvitationActivityAnalysisRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.FinalJoinInvitationActivityHistoryVO;
import com.xiliulou.electricity.vo.InvitationActivityJoinHistoryVO;
import com.xiliulou.electricity.vo.activity.InvitationActivityAnalysisAdminVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (InvitationActivityJoinHistory)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-06 09:51:43
 */
@Service("invitationActivityJoinHistoryService")
@Slf4j
public class InvitationActivityJoinHistoryServiceImpl implements InvitationActivityJoinHistoryService {
    private static final Integer PAGE_LIMIT = 100;

    @Resource
    private InvitationActivityJoinHistoryMapper invitationActivityJoinHistoryMapper;
    @Autowired
    private UserInfoService userInfoService;
    @Resource
    private FranchiseeService franchiseeService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityJoinHistory queryByIdFromDB(Long id) {
        return this.invitationActivityJoinHistoryMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityJoinHistory queryByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<InvitationActivityJoinHistory> queryAllByLimit(int offset, int limit) {
        return this.invitationActivityJoinHistoryMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvitationActivityJoinHistory insert(InvitationActivityJoinHistory invitationActivityJoinHistory) {
        this.invitationActivityJoinHistoryMapper.insertOne(invitationActivityJoinHistory);
        return invitationActivityJoinHistory;
    }

    /**
     * 修改数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivityJoinHistory invitationActivityJoinHistory) {
        return this.invitationActivityJoinHistoryMapper.update(invitationActivityJoinHistory);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.invitationActivityJoinHistoryMapper.deleteById(id) > 0;
    }

    @Override
    public InvitationActivityJoinHistory selectByActivityAndInvitationUid(Long activityId, Long invitationUid, Long uid) {
        return this.invitationActivityJoinHistoryMapper.selectOne(new LambdaQueryWrapper<InvitationActivityJoinHistory>().eq(InvitationActivityJoinHistory::getActivityId, activityId)
                .eq(InvitationActivityJoinHistory::getUid, invitationUid).eq(InvitationActivityJoinHistory::getJoinUid, uid));
    }

    @Override
    public InvitationActivityJoinHistory selectByActivityAndUid(Long activityId, Long uid) {
        return this.invitationActivityJoinHistoryMapper.selectByActivityAndUid(activityId, uid);
    }

    @Override
    @Slave
    public List<InvitationActivityJoinHistoryVO> selectByPage(InvitationActivityJoinHistoryQuery query) {
        List<InvitationActivityJoinHistoryVO> list = this.invitationActivityJoinHistoryMapper.selectUserHistoryByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.parallelStream().peek(item -> {
            //查询邀请人信息
            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            item.setUserName(Objects.isNull(userInfo) ? StringUtils.EMPTY : userInfo.getName());
            item.setPhone(Objects.isNull(userInfo) ? StringUtils.EMPTY : userInfo.getPhone());
    
            Long franchiseeId = item.getFranchiseeId();
            if (Objects.nonNull(franchiseeId)) {
                item.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
            }
        }).collect(Collectors.toList());

    }

    @Override
    @Slave
    public Integer selectByPageCount(InvitationActivityJoinHistoryQuery query) {
        return invitationActivityJoinHistoryMapper.selectByPageCount(query);
    }
    
    
    @Override
    public Integer updateStatusByActivityId(Long activityId, Integer status) {
        return invitationActivityJoinHistoryMapper.updateStatusByActivityId(activityId, status);
    }

    @Override
    public InvitationActivityJoinHistory selectByJoinIdAndStatus(Long uid, Integer status) {
        return invitationActivityJoinHistoryMapper.selectByJoinIdAndStatus(uid, status);
    }

    @Override
    @Slave
    public InvitationActivityJoinHistory selectByJoinUid(Long uid) {
        return invitationActivityJoinHistoryMapper.selectByJoinUid(uid);
    }

    @Override
    @Slave
    public List<InvitationActivityJoinHistoryVO> selectUserByPage(InvitationActivityJoinHistoryQuery query) {
        List<InvitationActivityJoinHistoryVO> list = this.invitationActivityJoinHistoryMapper.selectListByUser(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
    
        return list;
    }

    @Override
    public void handelActivityJoinHistoryExpired() {
        InvitationActivityJoinHistory invitationActivityJoinHistoryUpdate = new InvitationActivityJoinHistory();
        invitationActivityJoinHistoryUpdate.setStatus(InvitationActivityJoinHistory.STATUS_FAIL);
        invitationActivityJoinHistoryUpdate.setUpdateTime(System.currentTimeMillis());
        invitationActivityJoinHistoryMapper.updateExpired(invitationActivityJoinHistoryUpdate);
    }
    
    @Slave
    @Override
    public Integer existsByJoinUidAndActivityId(Long joinUid, Long activityId) {
        return invitationActivityJoinHistoryMapper.existsByJoinUidAndActivityId(joinUid, activityId);
    }
    
    @Override
    @Slave
    public List<InvitationActivityJoinHistory> listByJoinUid(Long uid) {
        return invitationActivityJoinHistoryMapper.selectListByJoinUid(uid);
    }
    
    @Slave
    @Override
    public List<InvitationActivityJoinHistoryVO> listByInviterUidOfAdmin(InvitationActivityJoinHistoryQuery query) {
        return invitationActivityJoinHistoryMapper.selectListByInviterUidOfAdmin(query);
    }
    
    @Slave
    @Override
    public List<InvitationActivityJoinHistoryVO> listByInviterUid(InvitationActivityJoinHistoryQuery query) {
        return invitationActivityJoinHistoryMapper.selectListByInviterUid(query);
    }
    
    @Slave
    @Override
    public List<InvitationActivityJoinHistoryVO> listByInviterUidDistinctJoin(InvitationActivityJoinHistoryQuery query) {
        return invitationActivityJoinHistoryMapper.selectListByInviterUidDistinctJoin(query);
    }
    
    @Override
    public InvitationActivityAnalysisAdminVO queryInvitationAdminAnalysis(InvitationActivityAnalysisRequest request) {
        InvitationActivityAnalysisAdminVO invitationActivityAnalysisAdminVO = new InvitationActivityAnalysisAdminVO();
    
        Integer timeType = request.getTimeType();
        Long beginTime = request.getBeginTime();
        Long endTime = request.getEndTime();
    
        if (Objects.equals(timeType, NumberConstant.ONE)) {
            // 查询昨日
            beginTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
            endTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        } else if (Objects.equals(timeType, NumberConstant.TWO)) {
            // 查询本月
            beginTime = DateUtils.getDayOfMonthStartTime(NumberConstant.ONE);
        }
    
        // 已获奖励（首次、续费）
        InvitationActivityJoinHistoryQuery historyQuery = InvitationActivityJoinHistoryQuery.builder().uid(request.getUid()).activityId(request.getActivityId())
                .beginTime(beginTime).endTime(endTime).build();
        List<InvitationActivityJoinHistoryVO> historyVOList = this.listByInviterUidOfAdmin(historyQuery);
    
        Integer totalShareCount = NumberConstant.ZERO;
        int totalInvitationCount = NumberConstant.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal firstTotalIncome = BigDecimal.ZERO;
        BigDecimal renewTotalIncome = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(historyVOList)) {
            // 根据joinUid去重
            List<InvitationActivityJoinHistoryVO> uniqueHistoryVOList = historyVOList.stream().collect(
                    Collectors.collectingAndThen(Collectors.toMap(InvitationActivityJoinHistoryVO::getJoinUid, Function.identity(), (oldValue, newValue) -> newValue),
                            map -> new ArrayList<>(map.values())));
            
            if (CollectionUtils.isNotEmpty(uniqueHistoryVOList)) {
                totalShareCount = uniqueHistoryVOList.size();
            }
    
            List<InvitationActivityJoinHistoryVO> uniqueInvitationHistoryVOList = historyVOList.stream()
                    .filter(item -> Objects.equals(item.getStatus(), NumberConstant.TWO)).collect(Collectors.collectingAndThen(
                            Collectors.toMap(InvitationActivityJoinHistoryVO::getJoinUid, Function.identity(), (oldValue, newValue) -> newValue),
                            map -> new ArrayList<>(map.values())));
    
            if (CollectionUtils.isNotEmpty(uniqueInvitationHistoryVOList)) {
                totalInvitationCount = uniqueInvitationHistoryVOList.size();
            }
            
            // 根据 payCount是否等于1 进行分组，并将每组的 money 相加
            Map<Boolean, BigDecimal> result = historyVOList.stream().collect(
                    Collectors.partitioningBy(history -> Objects.equals(Optional.ofNullable(history.getPayCount()).orElse(NumberConstant.ZERO), NumberConstant.ONE),
                            Collectors.reducing(BigDecimal.ZERO, history -> Optional.ofNullable(history.getMoney()).orElse(BigDecimal.ZERO), BigDecimal::add)));
        
            firstTotalIncome = result.get(Boolean.TRUE);
            renewTotalIncome = result.get(Boolean.FALSE);
        
            totalIncome = firstTotalIncome.add(renewTotalIncome);
        }
    
        invitationActivityAnalysisAdminVO.setTotalShareCount(totalShareCount);
        invitationActivityAnalysisAdminVO.setTotalInvitationCount(totalInvitationCount);
        invitationActivityAnalysisAdminVO.setFirstTotalIncome(firstTotalIncome);
        invitationActivityAnalysisAdminVO.setRenewTotalIncome(renewTotalIncome);
        invitationActivityAnalysisAdminVO.setTotalIncome(totalIncome);
    
        return invitationActivityAnalysisAdminVO;
    }
    
    /**
     * 根据活动id和参与人uid查询对应的参与记录
     */
    @Slave
    @Override
    public InvitationActivityJoinHistory queryByJoinUidAndActivityId(Long joinUid, Long activityId) {
        return invitationActivityJoinHistoryMapper.selectByJoinUidAndActivityId(joinUid, activityId);
    }
    
    @Slave
    @Override
    public FinalJoinInvitationActivityHistoryVO queryFinalHistoryByJoinUid(Long uid, Integer tenantId) {
        return invitationActivityJoinHistoryMapper.selectFinalHistoryByJoinUid(uid, tenantId);
    }
    
    @Slave
    @Override
    public InvitationActivityJoinHistory querySuccessHistoryByJoinUid(Long uid, Integer tenantId) {
        return invitationActivityJoinHistoryMapper.selectSuccessHistoryByJoinUid(uid, tenantId);
    }
    
    @Override
    public Integer removeByJoinUid(Long uid, Long updateTime, Integer tenantId) {
        return invitationActivityJoinHistoryMapper.updateByJoinUid(uid, updateTime, tenantId);
    }
    
    @Slave
    @Override
    public InvitationActivityJoinHistory queryModifiedInviterHistory(Long joinUid, Integer tenantId) {
        return invitationActivityJoinHistoryMapper.selectModifiedInviterHistory(joinUid, tenantId);
    }
}

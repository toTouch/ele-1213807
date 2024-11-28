package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TenantFranchiseeMutualExchange;
import com.xiliulou.electricity.query.MutualExchangePageQuery;
import com.xiliulou.electricity.query.MutualExchangeUpdateQuery;
import com.xiliulou.electricity.request.MutualExchangeAddConfigRequest;
import com.xiliulou.electricity.vo.MutualExchangeDetailVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

/**
 * @Description: TenantFranchiseeMutualExchangeService
 * @Author: renhang
 * @Date 2024/11/27 19:17
 */
public interface TenantFranchiseeMutualExchangeService {
    
    /**
     * 新增或者编辑
     *
     * @param request request
     * @return R
     */
    R addOrEditConfig(MutualExchangeAddConfigRequest request);
    
    /**
     * 获取配置互通详情
     *
     * @param id id
     * @return MutualExchangeDetailVO
     */
    MutualExchangeDetailVO getMutualExchangeDetailById(Long id);
    
    
    /**
     * 保存
     *
     * @param tenantFranchiseeMutualExchange tenantFranchiseeMutualExchange
     */
    void saveMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange);
    
    /**
     * 更新
     *
     * @param tenantFranchiseeMutualExchange tenantFranchiseeMutualExchange
     */
    void updateMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange);
    
    
    /**
     * 获取from
     *
     * @param tenantId tenantId
     * @return List
     */
    List<TenantFranchiseeMutualExchange> getMutualExchangeConfigListFromDB(Integer tenantId);
    
    
    /**
     * 缓存获取
     *
     * @param tenantId tenantId
     * @return List
     */
    List<String> getMutualFranchiseeExchangeCache(Integer tenantId);
    
    /**
     * 分页查询
     *
     * @param query query
     * @return List
     */
    List<MutualExchangeDetailVO> pageList(MutualExchangePageQuery query);
    
    /**
     * 分页count
     *
     * @param query query
     * @return long
     */
    Long pageCount(MutualExchangePageQuery query);
    
    /**
     * 逻辑删除
     *
     * @param id id
     * @return R
     */
    R deleteById(Long id);
    
    /**
     * 更新状态
     *
     * @param query query
     * @return R
     */
    R updateStatus(MutualExchangeUpdateQuery query);
    
    
    /**
     * 当前加盟商是否满足加盟商换电互通
     *
     * @param tenantId     用户的租户
     * @param franchiseeId 电池的加盟商
     * @return Pair
     */
    Pair<Boolean, Set<Long>> isSatisfyFranchiseeMutualExchange(Integer tenantId, Long franchiseeId);
}

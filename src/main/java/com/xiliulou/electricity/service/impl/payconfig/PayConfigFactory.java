/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/18
 */

package com.xiliulou.electricity.service.impl.payconfig;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.pay.base.exception.PayException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: 支付配置工厂
 *
 * @author caobotao.cbt
 * @date 2024/7/18 15:16
 */
@Component
public class PayConfigFactory {
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Resource
    private AlipayAppConfigService alipayAppConfigService;
    
    /**
     * 匹配支付配置策略 {@link PayConfigFactory#init()}
     * <p>
     * key:{@link ChannelEnum#getCode()}
     * <p>
     * value: {@link PayConfigStrategy#execute(java.lang.Integer, java.lang.Long, java.util.Set)} ()}
     */
    private Map<String, PayConfigStrategy> strategyMap = new ConcurrentHashMap<>();
    
    
    /**
     * 精确支付配置策略 {@link PayConfigFactory#init()}
     * <p>
     * key:{@link ChannelEnum#getCode()}
     * <p>
     * value: {@link PayConfigStrategy#execute(java.lang.Integer, java.lang.Long, java.util.Set)} ()}
     */
    private Map<String, PayConfigStrategy> preciseStrategyMap = new ConcurrentHashMap<>();
    
    
    /**
     * 初始化支付配置路由策略
     *
     * @author caobotao.cbt
     * @date 2024/7/17 11:37
     */
    @PostConstruct
    public void init() {
        
        // 注册支付宝支付配置获取函数
        this.registerAliPay();
        
        // 注册微信支付配置获取函数
        this.registerWxPay();
    }
    
    /**
     * 支付宝注册
     *
     * @author caobotao.cbt
     * @date 2024/7/18 15:47
     */
    private void registerAliPay() {
        
        // TODO: 2024/9/6 支付包暂时不支持分账配置查询
        // 支付宝支付参数查询
        strategyMap.put(ChannelEnum.ALIPAY.getCode(),
                (tenantId, franchiseeId, queryProfitSharingConfig) -> alipayAppConfigService.queryByTenantIdAndFranchiseeId(tenantId, franchiseeId));
        // 支付宝支付参数精确查询
        preciseStrategyMap.put(ChannelEnum.ALIPAY.getCode(),
                (tenantId, franchiseeId, queryProfitSharingConfig) -> alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId));
    }
    
    /**
     * 微信注册
     *
     * @author caobotao.cbt
     * @date 2024/7/18 15:47
     */
    private void registerWxPay() {
        // 微信支付参数查询
        strategyMap.put(ChannelEnum.WECHAT.getCode(), (tenantId, franchiseeId, queryProfitSharingConfig) -> wechatPayParamsBizService
                .getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId, queryProfitSharingConfig));
        
        // 微信支付参数精确查询
        preciseStrategyMap.put(ChannelEnum.WECHAT.getCode(), (tenantId, franchiseeId, queryProfitSharingConfig) -> wechatPayParamsBizService
                .getPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId, queryProfitSharingConfig));
        
    }
    
    
    public PayConfigStrategy getStrategy(String key) {
        return strategyMap.get(key);
    }
    
    
    public PayConfigStrategy getPreciseStrategy(String key) {
        return preciseStrategyMap.get(key);
    }
    
    
    @FunctionalInterface
    public interface PayConfigStrategy {
        
        BasePayConfig execute(Integer tenantId, Long franchiseeId, Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig) throws PayException;
    }
    
}

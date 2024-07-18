/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/18
 */

package com.xiliulou.electricity.service.impl.payconfig;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.enums.PaymentMethodEnum;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
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
     * 匹配支付配置策略 {@link PayConfigBizServiceImpl#init()}
     * <p>
     * key:{@link PaymentMethodEnum#getCode()}
     * <p>
     * value: {@link PayConfigStrategy#execute(Integer, Long)} ()}
     */
    private Map<String, PayConfigStrategy> strategyMap = new ConcurrentHashMap<>();
    
    
    /**
     * 精确支付配置策略 {@link PayConfigBizServiceImpl#init()}
     * <p>
     * key:{@link PaymentMethodEnum#getCode()}
     * <p>
     * value: {@link PayConfigStrategy#execute(Integer, Long)} ()}
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
        // 支付宝支付参数查询
        strategyMap.put(PaymentMethodEnum.ALI_PAY.getCode(), (tenantId, franchiseeId) -> alipayAppConfigService.queryByTenantIdAndFranchiseeId(tenantId, franchiseeId));
        // 支付宝支付参数精确查询
        preciseStrategyMap
                .put(PaymentMethodEnum.ALI_PAY.getCode(), (tenantId, franchiseeId) -> alipayAppConfigService.queryPreciseByTenantIdAndFranchiseeId(tenantId, franchiseeId));
    }
    
    /**
     * 微信注册
     *
     * @author caobotao.cbt
     * @date 2024/7/18 15:47
     */
    private void registerWxPay() {
        // 微信支付参数查询
        strategyMap.put(PaymentMethodEnum.WECHAT.getCode(), (tenantId, franchiseeId) -> wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId));
        
        // 微信支付参数精确查询
        preciseStrategyMap.put(PaymentMethodEnum.WECHAT.getCode(),
                (tenantId, franchiseeId) -> wechatPayParamsBizService.getPreciseDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId));
        
    }
    
    
    public <T extends BasePayConfig>  PayConfigStrategy<T> getStrategy(String key) {
        return strategyMap.get(key);
    }
    
    
    public <T extends BasePayConfig> PayConfigStrategy<T> getPreciseStrategy(String key) {
        return preciseStrategyMap.get(key);
    }
    
    
    @FunctionalInterface
    public interface PayConfigStrategy<R extends BasePayConfig> {
        
        R execute(Integer tenantId, Long franchiseeId) throws WechatPayException, AliPayException;
    }
    
}

package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import com.xiliulou.electricity.query.AlipayAppConfigQuery;
import com.xiliulou.electricity.vo.AlipayAppConfigVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.electricity.query.AlipayAppConfigQuery;
import com.xiliulou.electricity.vo.AlipayAppConfigVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 支付宝小程序配置(AlipayAppConfig)表服务接口
 *
 * @author zzlong
 * @since 2024-07-08 16:45:19
 */
public interface AlipayAppConfigService {
    
    AlipayAppConfig queryByAppId(String appId);
    
    AlipayAppConfig queryByTenantId(Integer tenantId);
    
    
    /**
     * 根据运营商和加盟商查询
     * <p>
     * 1.franchiseeId 不存在配置,则返回运营商默认配置<br/> 2.franchiseeId 存在配置,则返回加盟商配置<br/> 3.如果要查询运营商默认配置，franchiseeId传{@link com.xiliulou.electricity.constant.MultiFranchiseeConstant#DEFAULT_FRANCHISEE}
     * </p>
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/7/16 16:36
     * @return
     */
    AlipayAppConfigBizDetails queryByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) throws AliPayException;
    
    /**
     * 根据运营商和加盟商查询精确查询
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/7/16 16:36
     * @return
     */
    AlipayAppConfigBizDetails queryPreciseByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) throws AliPayException;
    
    List<AlipayAppConfigVO> listByTenantId(Integer tenantId);
    
    Triple<Boolean, String, Object> save(AlipayAppConfigQuery query);
    
    Triple<Boolean, String, Object> modify(AlipayAppConfigQuery query);
    
    Triple<Boolean, String, Object> remove(Long id);

}

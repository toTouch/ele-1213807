package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.query.WechatCallBackResouceData;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundOrderCallBackQuery;
import com.xiliulou.pay.weixinv3.util.AesUtil;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3RefundOrderCallBackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author xiaohui.song
 **/
@Slf4j
public class JsonOuterCallBackBasicController {
    
    @Resource
    private ElectricityPayParamsService electricityPayParamsService;
    
    /**
     * 处理回调参数 ,老支付回调(兼容上线期间的历史数据)
     *
     * @param wechatV3RefundOrderCallBackQuery
     * @return
     */
    protected WechatJsapiRefundOrderCallBackResource handCallBackParam(WechatV3RefundOrderCallBackQuery wechatV3RefundOrderCallBackQuery) {
        WechatCallBackResouceData resource = wechatV3RefundOrderCallBackQuery.getResource();
        if (Objects.isNull(resource)) {
            log.error("WECHAT ERROR! no wechat's info ! msg={}", wechatV3RefundOrderCallBackQuery);
            return null;
        }
        
        String decryptJson = null;
        try {
            String apiV3Key = this.getApiV3Key(wechatV3RefundOrderCallBackQuery.getTenantId(), MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
            decryptJson = AesUtil
                    .decryptToString(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8), resource.getNonce().getBytes(StandardCharsets.UTF_8), resource.getCiphertext(),
                            apiV3Key.getBytes(StandardCharsets.UTF_8));
            
        } catch (Exception e) {
            log.error("WECHAT ERROR! wechat decrypt error! msg={}", wechatV3RefundOrderCallBackQuery, e);
            return null;
        }
        
        WechatJsapiRefundOrderCallBackResource callBackResource = JsonUtil.fromJson(decryptJson, WechatJsapiRefundOrderCallBackResource.class);
        
        return callBackResource;
    }
    
    
    /**
     * 处理回调参数
     *
     * @param wechatV3RefundOrderCallBackQuery
     * @return
     */
    protected WechatJsapiRefundOrderCallBackResource handCallBackParam(WechatV3RefundOrderCallBackRequest wechatV3RefundOrderCallBackQuery) {
        WechatCallBackResouceData resource = wechatV3RefundOrderCallBackQuery.getResource();
        if (Objects.isNull(resource)) {
            log.error("WECHAT ERROR! new call back no wechat's info ! msg={}", wechatV3RefundOrderCallBackQuery);
            return null;
        }
        
        String decryptJson = null;
        try {
            String apiV3Key = this.getApiV3Key(wechatV3RefundOrderCallBackQuery.getTenantId(), wechatV3RefundOrderCallBackQuery.getFranchiseeId());
            decryptJson = AesUtil
                    .decryptToString(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8), resource.getNonce().getBytes(StandardCharsets.UTF_8), resource.getCiphertext(),
                            apiV3Key.getBytes(StandardCharsets.UTF_8));
            
        } catch (Exception e) {
            log.error("WECHAT ERROR! new call wechat decrypt error! msg={}", wechatV3RefundOrderCallBackQuery, e);
            return null;
        }
        
        WechatJsapiRefundOrderCallBackResource callBackResource = JsonUtil.fromJson(decryptJson, WechatJsapiRefundOrderCallBackResource.class);
        
        return callBackResource;
    }
    
    
    private String getApiV3Key(Integer tenantId, Long franchiseeId) {
        try {
            ElectricityPayParams payParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.isNull(payParams)) {
                throw new AuthenticationServiceException("未能查找到appId和appSecret！");
            }
            return payParams.getWechatV3ApiKey();
        } catch (Exception e) {
            throw new RuntimeException("获取商户apiV3密钥失败！tenantId=" + tenantId + " franchiseeId=" + franchiseeId, e);
        }
    }
}

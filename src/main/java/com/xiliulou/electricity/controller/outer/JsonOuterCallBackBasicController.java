package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.query.WechatCallBackResouceData;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundOrderCallBackQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3MerchantLoadAndUpdateCertificateService;
import com.xiliulou.pay.weixinv3.util.AesUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author xiaohui.song
 **/
@Slf4j
public class JsonOuterCallBackBasicController {

    @Resource
    private WechatV3MerchantLoadAndUpdateCertificateService certificateService;

    /**
     * 处理回调参数
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
            decryptJson = AesUtil.decryptToString(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8),
                    resource.getNonce().getBytes(StandardCharsets.UTF_8),
                    resource.getCiphertext(),
                    certificateService.getMerchantApiV3Key(wechatV3RefundOrderCallBackQuery.getTenantId()).getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("WECHAT ERROR! wechat decrypt error! msg={}", wechatV3RefundOrderCallBackQuery, e);
            return null;
        }

        WechatJsapiRefundOrderCallBackResource callBackResource = JsonUtil.fromJson(decryptJson, WechatJsapiRefundOrderCallBackResource.class);

        return callBackResource;
    }
}

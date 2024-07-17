/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/12
 */

package com.xiliulou.electricity.converter;

import com.xiliulou.pay.alipay.request.AliPayCreateOrderRequest.ExtendParams;

import com.xiliulou.electricity.bo.pay.PayParamsBizDetails;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import com.xiliulou.electricity.vo.ElectricityPayParamsVO;
import com.xiliulou.pay.alipay.request.AliPayCreateOrderRequest;
import com.xiliulou.pay.base.enums.PayTypeEnum;
import com.xiliulou.pay.base.request.BasePayCreateOrderRequest;
import com.xiliulou.pay.base.request.BasePayRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3CommonRequest;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3OrderRequest;
import org.springframework.beans.BeanUtils;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/6/12 16:51
 */
public class ElectricityPayParamsConverter {
    
    /**
     * 操作参数转换
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/12 16:55
     */
    public static ElectricityPayParams optRequestToDO(ElectricityPayParamsRequest request) {
        ElectricityPayParams payParams = new ElectricityPayParams();
        BeanUtils.copyProperties(request, payParams);
        return payParams;
    }
    
    /**
     * 查询转换
     *
     * @param params
     * @author caobotao.cbt
     * @date 2024/6/12 18:14
     */
    public static List<ElectricityPayParamsVO> qryDoToVos(List<ElectricityPayParams> params) {
        return Optional.ofNullable(params).orElse(Collections.emptyList()).stream().map(p -> {
            ElectricityPayParamsVO electricityPayParamsVO = new ElectricityPayParamsVO();
            BeanUtils.copyProperties(p, electricityPayParamsVO);
            return electricityPayParamsVO;
        }).collect(Collectors.toList());
    }
    
    
    /**
     * do转详情
     *
     * @param payParams
     * @author caobotao.cbt
     * @date 2024/6/14 13:08
     */
    public static WechatPayParamsDetails qryDoToDetails(ElectricityPayParams payParams) {
        WechatPayParamsDetails wechatPayParamsDetails = new WechatPayParamsDetails();
        BeanUtils.copyProperties(payParams, wechatPayParamsDetails);
        return wechatPayParamsDetails;
    }
    
    /**
     * 查询转换
     *
     * @param payParamsDetails
     * @author caobotao.cbt
     * @date 2024/6/14 13:46
     */
    public static WechatV3CommonRequest qryDetailsToCommonRequest(WechatPayParamsDetails payParamsDetails) {
        return WechatV3CommonRequest.builder().merchantApiV3Key(payParamsDetails.getWechatV3ApiKey()).merchantId(payParamsDetails.getWechatMerchantId())
                .merchantCertificateSerialNo(payParamsDetails.getWechatMerchantCertificateSno()).wechatPlatformCertificateMap(payParamsDetails.getWechatPlatformCertificateMap())
                .privateKey(payParamsDetails.getPrivateKey()).build();
    }
    
    
    
    
    
}

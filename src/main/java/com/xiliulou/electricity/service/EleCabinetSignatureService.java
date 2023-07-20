package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.SignFileQuery;
import com.xiliulou.esign.entity.query.UserInfoQuery;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;

/**
 * 电子签名服务
 * @author: Kenneth
 * @Date: 2023/7/5 8:50
 * @Description:
 */
public interface EleCabinetSignatureService {

    public Triple<Boolean, String, Object> personalAuthentication();

    public Triple<Boolean, String, Object> createFileByTemplate();

    public Triple<Boolean, String, Object> getSignFlowLink(SignFileQuery signFileQuery);

    public Triple<Boolean, String, Object> checkUserEsignFinished();

    public Triple<Boolean, String, Object> getSignatureFile(String signFlowId);

    public Triple<Boolean, String, Object> getSignFlowUrl(String signFlowId);

    public void handleCallBackReq(Integer esignConfigId, HttpServletRequest request);


}

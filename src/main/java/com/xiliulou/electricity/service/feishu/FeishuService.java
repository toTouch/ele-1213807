package com.xiliulou.electricity.service.feishu;

/**
 * 飞书Service
 *
 * @author xiaohui.song
 **/
public interface FeishuService {

    void sendException(String requestURI, String traceId, Exception e);

    void sendException(String traceId, Exception e);
}

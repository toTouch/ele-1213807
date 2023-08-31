package com.xiliulou.electricity.service.feishu;

/**
 * 飞书Service
 *
 * @author xiaohui.song
 **/
public interface FeishuService {

    void sendException(Exception e, String traceId);
}

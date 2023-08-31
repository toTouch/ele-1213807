package com.xiliulou.electricity.service.impl.feishu;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.service.feishu.FeishuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class FeishuServiceImpl implements FeishuService {

    @Override
    public void sendException(String requestURI, String traceId, Exception e) {
        if (ObjectUtils.isEmpty(e)) {
            return;
        }
        String requestUri = "";
        if (!ObjectUtils.isEmpty(requestURI)) {
            requestUri = "\n请求路径: " + requestURI;
        }
        String url = CommonConstant.FEISHU_WARNING_ROBOT_WEB_HOOK_URL;
        Map<String, Object> textMap = new HashMap<String, Object>();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String msgExceptionStack = stringWriter.toString();
        textMap.put("text", "traceId: " + traceId  + requestUri + "\n异常信息如下:\n" + msgExceptionStack);

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("msg_type", "text");
        paramMap.put("content", textMap);
        try {
            HttpUtil.post(url, JSONObject.toJSONString(paramMap));
        } catch (Exception ex) {
            log.error("HTTP请求异常：", ex);
            return;
        }
    }

    @Override
    public void sendException(String traceId, Exception e) {
        if (ObjectUtils.isEmpty(e)) {
            return;
        }
        String url = CommonConstant.FEISHU_WARNING_ROBOT_WEB_HOOK_URL;
        Map<String, Object> textMap = new HashMap<String, Object>();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String msgExceptionStack = stringWriter.toString();
        textMap.put("text", "traceId: " + traceId  + "\n异常信息如下:\n" + msgExceptionStack);

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("msg_type", "text");
        paramMap.put("content", textMap);
        try {
            HttpUtil.post(url, JSONObject.toJSONString(paramMap));
        } catch (Exception ex) {
            log.error("HTTP请求异常：", ex);
            return;
        }
    }

}

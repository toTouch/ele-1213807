///**
// * Create date: 2024/6/28
// */
//
//package com.xiliulou.electricity.mq.handler.message.wechat;
//
//import com.xiliulou.core.json.JsonUtil;
//import com.xiliulou.electricity.entity.MqHardwareNotify;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.xiliulou.electricity.enums.notify.SendMessageTypeEnum.HARDWARE_INFO_NOTIFY;
//
///**
// * description: 设备消息通知
// *
// * @author caobotao.cbt
// * @date 2024/6/28 14:45
// */
//@Component
//public class HardwareInfoHandler extends AbstractWechatOfficialAccountSendHandler {
//
//    @Override
//    public Integer getType() {
//        return HARDWARE_INFO_NOTIFY.getType();
//    }
//
//
//    @Override
//    protected Map<String, String> converterParamMap(String data) {
//        MqHardwareNotify notify = JsonUtil.fromJson(data, MqHardwareNotify.class);
//        Map<String, String> params = new HashMap<>();
//
//        params.put("first", notify.getProjectTitle());
//        params.put("keyword1", notify.getDeviceName());
//        params.put("keyword2", notify.getErrMsg());
//        params.put("keyword3", notify.getOccurTime());
//        params.put("remark", "");
//        return params;
//    }
//}

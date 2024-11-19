/**
 *  Create date: 2024/6/27
 */

package com.xiliulou.electricity.mq.handler.message.wechat;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.DeviceNotify;
import com.xiliulou.electricity.enums.notify.DeviceStatusEnum;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * description: 设备上下线
 *
 * @author caobotao.cbt
 * @date 2024/6/27 20:50
 */
@Slf4j
@Component
public class DeviceStatusSendHandler extends AbstractWechatOfficialAccountSendHandler {
    
    
    @Override
    protected Map<String, String> converterParamMap(String data) {
        DeviceNotify deviceNotify = JsonUtil.fromJson(data, DeviceNotify.class);
        // TODO: 2024/10/23 同步notify删除此代码
//        if (DeviceStatusEnum.DEVICE_OFFLINE.getStatus().equals(deviceNotify.getStatus()) && electricityCabinetService
//                .deviceIsOnline(deviceNotify.getProductKey(), deviceNotify.getDeviceName())) {
//            //如果这个时候还在线的话，就不用通知了
//            return null;
//        }
        
        Map<String, String> params = new HashMap<>();
        
        String projectName = deviceNotify.getProjectName();
        String status = deviceNotify.getStatus();
        
        Optional<DeviceStatusEnum> byStatus = DeviceStatusEnum.getByStatus(status);
        DeviceStatusEnum deviceStatusEnum = byStatus.orElse(DeviceStatusEnum.DEVICE_OFFLINE);
        
        params.put("first", String.format("%s%s通知", projectName, deviceStatusEnum.getStatusMsg()));
        params.put("keyword1", projectName);
        params.put("keyword2", deviceStatusEnum.getStatusMsg());
        params.put("keyword3", deviceNotify.getDeviceName());
        params.put("keyword4", deviceNotify.getOccurTime());
        params.put("remark", deviceStatusEnum.getRemarkMsg());
        return params;
    }
    
    @Override
    public Integer getType() {
        return SendMessageTypeEnum.DEVICE_LOGIN_NOTIFY.getType();
    }
}

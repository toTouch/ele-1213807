package com.xiliulou.electricity.constant;

public interface DeviceReportConstant {
    /**
     * 设备网关服务端口
     */
    Integer REPORT_SERVER_PORT = 10030;
    
    /**
     * 设备网关服务context-path
     */
    String REPORT_SERVER_CONTEXT_PATH = "/saas-electricity-device-gateway-web";
    
    /**
     * 设备网关服务发送命令接口
     */
    String REPORT_SERVER_SEND_COMMAND_URL = "/inner/device/command";
}

package com.xiliulou.electricity.service.warn;

import com.xiliulou.electricity.request.failureAlarm.FailureWarnCabinetMsgPageRequest;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;

/**
 * @author maxiaodong
 * @date 2023/12/28 16:18
 * @desc
 */
public interface EleHardwareFaultCabinetMsgService {
    
    Triple<Boolean, String, Object> tenantOverviewPage(FailureWarnCabinetMsgPageRequest request);
    
    Triple<Boolean, String, Object> tenantOverviewPageCount(FailureWarnCabinetMsgPageRequest request);
    
    void tenantOverviewExport(FailureWarnCabinetMsgPageRequest request, HttpServletResponse response);
    
    Triple<Boolean, String, Object> cabinetOverviewPage(FailureWarnCabinetMsgPageRequest request);
    
    Triple<Boolean, String, Object> cabinetOverviewPageCount(FailureWarnCabinetMsgPageRequest request);
    
    void cabinetOverviewExport(FailureWarnCabinetMsgPageRequest request, HttpServletResponse response);
    
    void createFaultWarnData();
}

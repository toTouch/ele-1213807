package com.xiliulou.electricity.service.warn;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFailureWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmTaskQueryRequest;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnFrequencyVo;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/5/23 11:18
 * @desc
 */
public interface EleHardwareWarnMsgService {
    
    List<EleHardwareFailureWarnMsgVo> list(FailureAlarmTaskQueryRequest request);
    
    R listByPage(EleHardwareWarnMsgPageRequest request);
    
    R countTotal(EleHardwareWarnMsgPageRequest request);
    
    R superExportPage(EleHardwareWarnMsgPageRequest request);
    
    Triple<Boolean, String, Object> proportion(EleHardwareWarnMsgPageRequest request);
    
    void proportionExport(EleHardwareWarnMsgPageRequest request, HttpServletResponse response);
    
    void setWarnInfo(FailureWarnFrequencyVo vo, FailureWarnMsgPageQueryModel queryModel);
    
    int existByAlarmId(String alarmId);
    
    int updateNoteFlagByAlarmId(String alarmId);
}

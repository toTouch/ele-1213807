package com.xiliulou.electricity.service.warn;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.warn.EleHardwareWarnMsg;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFailureWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmTaskQueryRequest;
import com.xiliulou.electricity.request.failureAlarm.WarnHandlePageRequest;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgPageVo;
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
    
    List<String> listSignalIdByIdList(List<Long> warnIdList, Integer handleStatus);
    
    List<EleHardwareWarnMsg> listBySignalIdList(List<String> signalIdList, Long maxId, Integer tenantId, Long size);
    
    int batchUpdateHandleStatus(List<Long> warnIdList, Integer status, String batchNo);
    
    List<EleHardwareFailureWarnMsgPageVo> listHandlerRecordByPage(WarnHandlePageRequest warnHandlePageRequest);
    
    Integer countHandleRecordTotal(WarnHandlePageRequest warnHandlePageRequest);
    
    String checkHandleResult(String batchNo);
    
    Boolean existsByIdList(List<Long> warnIdList);
}

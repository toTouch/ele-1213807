package com.xiliulou.electricity.service.warn;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.warn.EleHardwareFaultMsg;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FaultMsgPageQueryModel;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFaultMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmTaskQueryRequest;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnFrequencyVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionVo;
import com.xiliulou.electricity.vo.warn.FaultMsgExcelVo;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author maxiaodong
 * @date 2024/5/23 11:19
 * @desc
 */
public interface EleHardwareFaultMsgService {
    
    List<EleHardwareFailureWarnMsgVo> list(FailureAlarmTaskQueryRequest request);
    
    R transferListPage(List<EleHardwareFaultMsg> eleHardwareFaultMsgList, EleHardwareFaultMsgPageRequest request);
    
    R countTotal(FaultMsgPageQueryModel queryModel);
    
    R superExportPage(EleHardwareFaultMsgPageRequest request, List<FaultMsgExcelVo> list);
    
    Triple<Boolean, String, Object> checkAndInitQuery(EleHardwareFaultMsgPageRequest request, FaultMsgPageQueryModel queryModel, int daySize);
    
    List<EleHardwareFaultMsg> listByPage(EleHardwareFaultMsgPageRequest request, FaultMsgPageQueryModel queryModel);
    
    List<FaultMsgExcelVo> listExportData(FaultMsgPageQueryModel queryModel, Triple<Boolean, String, Object> triple);
    
    List<FailureWarnProportionVo> listProportion(EleHardwareFaultMsgPageRequest request);
    
    List<FailureWarnProportionVo> faultProportion(Map<String, Integer> failureMap);
    
    void proportionExport(List<FailureWarnProportionVo> list, HttpServletResponse response);
    
    void setFailureInfo(FailureWarnFrequencyVo vo, FailureWarnMsgPageQueryModel queryModel);
}

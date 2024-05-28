package com.xiliulou.electricity.mapper.warn;

import com.xiliulou.electricity.entity.warn.EleHardwareFaultMsg;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureFaultMsgTaskQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FaultMsgPageQueryModel;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionVo;
import com.xiliulou.electricity.vo.warn.FaultMsgExcelVo;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/5/23 10:51
 * @desc
 */
public interface EleHardwareFaultMsgMapper {
    
    List<EleHardwareFailureWarnMsgVo> selectList(FailureFaultMsgTaskQueryModel queryModel);
    
    List<EleHardwareFaultMsg> selectListByPage(FaultMsgPageQueryModel queryModel);
    
    Integer countTotal(FaultMsgPageQueryModel queryModel);
    
    List<FaultMsgExcelVo> selectListExport(FaultMsgPageQueryModel queryModel);
    
    List<FailureWarnProportionVo> selectListProportion(FailureWarnMsgPageQueryModel queryModel);
    
    Integer countFaultNum(FailureWarnMsgPageQueryModel queryModel);
}

package com.xiliulou.electricity.mapper.warn;

import com.xiliulou.electricity.entity.warn.EleHardwareFaultMsg;
import com.xiliulou.electricity.entity.warn.EleHardwareWarnMsg;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgTaskQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FaultMsgPageQueryModel;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnMsgExcelVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionVo;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/5/23 10:51
 * @desc
 */
public interface EleHardwareFaultMsgMapper {
    
    List<EleHardwareFailureWarnMsgVo> selectList(FailureWarnMsgTaskQueryModel queryModel);
    
    List<EleHardwareFaultMsg> selectListByPage(FaultMsgPageQueryModel queryModel);
    
    Integer countTotal(FaultMsgPageQueryModel queryModel);
    
    List<FailureWarnMsgExcelVo> selectListExport(FaultMsgPageQueryModel queryModel);
    
    List<FailureWarnProportionVo> selectListProportion(FailureWarnMsgPageQueryModel queryModel);
    
    Integer countFaultNum(FailureWarnMsgPageQueryModel queryModel);
}

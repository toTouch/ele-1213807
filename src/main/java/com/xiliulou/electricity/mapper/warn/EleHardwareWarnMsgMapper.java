package com.xiliulou.electricity.mapper.warn;

import com.xiliulou.electricity.entity.warn.EleHardwareWarnMsg;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgTaskQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.WarnMsgPageQueryModel;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnMsgExcelVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/5/23 11:11
 * @desc
 */
public interface EleHardwareWarnMsgMapper {
    
    List<EleHardwareFailureWarnMsgVo> selectList(FailureWarnMsgTaskQueryModel queryModel);
    
    List<EleHardwareWarnMsg> selectListByPage(WarnMsgPageQueryModel queryModel);
    
    Integer countTotal(WarnMsgPageQueryModel queryModel);
    
    List<FailureWarnMsgExcelVo> selectListExport(WarnMsgPageQueryModel queryModel);
    
    List<FailureWarnProportionVo> selectListProportion(FailureWarnMsgPageQueryModel queryModel);
    
    Integer countWarnNum(FailureWarnMsgPageQueryModel queryModel);
    
    int existByAlarmId(@Param("alarmId") String alarmId);
}

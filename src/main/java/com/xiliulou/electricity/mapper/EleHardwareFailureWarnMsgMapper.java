package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgTaskQueryModel;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnMsgExcelVo;

import java.util.List;

/**
 * @author maxiaodong
 * @since 2023-12-26 09:07:48
 */

public interface EleHardwareFailureWarnMsgMapper extends BaseMapper<EleHardwareFailureWarnMsg> {
    List<EleHardwareFailureWarnMsgVo> selectList(FailureWarnMsgTaskQueryModel queryModel);
    
    List<EleHardwareFailureWarnMsg> selectListByPage(FailureWarnMsgPageQueryModel queryModel);
    
    Integer countTotal(FailureWarnMsgPageQueryModel queryModel);
    
    List<FailureWarnMsgExcelVo> selectListExport(FailureWarnMsgPageQueryModel queryModel);
    
    List<EleHardwareFailureWarnMsgVo> countFailureWarnNum(FailureWarnMsgTaskQueryModel queryModel);
}

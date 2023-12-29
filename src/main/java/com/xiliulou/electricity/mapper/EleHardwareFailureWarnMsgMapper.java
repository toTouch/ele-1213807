package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.queryModel.failureAlarm.EleHardwareFailureWarnMsgQueryModel;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;

import java.util.List;

/**
 * @author maxiaodong
 * @since 2023-12-26 09:07:48
 */

public interface EleHardwareFailureWarnMsgMapper extends BaseMapper<EleHardwareFailureWarnMsg> {
    List<EleHardwareFailureWarnMsgVo> selectList(EleHardwareFailureWarnMsgQueryModel queryModel);
}

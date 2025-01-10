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
    
    int updateNoteFlagByAlarmId(@Param("alarmId") String alarmId, @Param("updateTime") long updateTime);
    
    List<String> selectListSignalIdByIdList(@Param("warnIdList") List<Long> warnIdList,@Param("handleStatus") Integer handleStatus);
    
    List<EleHardwareWarnMsg> selectListBySignalIdList(@Param("signalIdList") List<String> signalIdList,@Param("maxId") Long maxId,@Param("tenantId") Integer tenantId,@Param("size") Long size);
    
    int batchUpdateHandleStatus(@Param("idList") List<Long> warnIdList,@Param("status") Integer status,
            @Param("batchNo") String batchNo,@Param("updateTime") long updateTime);
    
    List<EleHardwareWarnMsg> selectListHandlerRecordByPage(WarnMsgPageQueryModel queryModel);
    
    Integer countHandleRecordTotal(WarnMsgPageQueryModel queryModel);
    
    Integer existsByIdList(@Param("idList") List<Long> warnIdList);
}

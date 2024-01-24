package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleHardwareFailureCabinetMsg;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureCabinetMsgQueryModel;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnCabinetOverviewVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnTenantOverviewVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2023/12/28 16:16
 * @desc
 */
public interface EleHardwareFailureCabinetMsgMapper {
    
    int batchInsert(@Param("list") List<EleHardwareFailureCabinetMsg> failureCabinetMsgList);
    
    void batchDelete(@Param("startTime") Long startTime,@Param("endTime") Long endTime);
    
    List<FailureWarnTenantOverviewVo> selectListForFailure(FailureCabinetMsgQueryModel queryModel);
    
    List<FailureWarnTenantOverviewVo> selectListForWarn(FailureCabinetMsgQueryModel queryModel);
    
    Integer countTenantOverview(FailureCabinetMsgQueryModel queryModel);
    
    List<FailureWarnCabinetOverviewVo> selectListCabinetFailure(FailureCabinetMsgQueryModel queryModel);
    
    List<FailureWarnCabinetOverviewVo> selectListCabinetWarn(FailureCabinetMsgQueryModel queryModel);
    
    Integer countCabinetOverview(FailureCabinetMsgQueryModel queryModel);
}

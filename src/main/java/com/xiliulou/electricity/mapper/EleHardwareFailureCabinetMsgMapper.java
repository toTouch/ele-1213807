package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleHardwareFailureCabinetMsg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2023/12/28 16:16
 * @desc
 */
public interface EleHardwareFailureCabinetMsgMapper extends BaseMapper<EleHardwareFailureCabinetMsg> {
    
    int batchInsert(@Param("list") List<EleHardwareFailureCabinetMsg> failureCabinetMsgList);
    
    void batchDelete(@Param("startTime") Long startTime,@Param("endTime") Long endTime);
}

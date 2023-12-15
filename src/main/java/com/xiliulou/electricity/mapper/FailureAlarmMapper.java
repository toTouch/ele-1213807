package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureAlarmQueryModel;
import com.xiliulou.electricity.vo.failureAlarm.FailureAlarmVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (City)故障告警设置
 *
 * @author maxiaodong
 * @since 2023-12-15 18:05:41
 */
public interface FailureAlarmMapper extends BaseMapper<FailureAlarm>{
    
    int insertOne(FailureAlarm failureAlarm);
    
    int checkErrorCode(@Param("errorCode") Integer errorCode);
    
    Integer countTotal(FailureAlarmQueryModel failureAlarmQueryModel);
    
    List<FailureAlarm> selectListByPage(FailureAlarmQueryModel failureAlarmQueryModel);
    
    int update(FailureAlarm failureAlarm);
    
    int batchDeleteByFailureAlarmId(@Param("failureAlarmId") Long id);
    
    FailureAlarm select(@Param("id") Long id);
    
    int remove(@Param("id") Long id, @Param("updateTime") Long updateTime, @Param("delFlag") Integer delFlag);
    
    int batchUpdateTenantVisible(@Param("idList") List<Long> idList,@Param("tenantVisible") Integer tenantVisible, @Param("updateTime") Long updateTime);
    
    List<FailureAlarm> selectList(@Param("idList") List<Long> idList);
}

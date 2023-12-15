package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmBatchSetRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmSaveRequest;
import com.xiliulou.electricity.vo.failureAlarm.FailureAlarmVO;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author: maxiaodong
 * @Date: 2023/12/15 11:27
 * @Description: 故障预警设置
 */
public interface FailureAlarmService {
    
    Triple<Boolean, String, Object> save(FailureAlarmSaveRequest failureAlarmSaveRequest, Long uid);
    
    Integer countTotal(FailureAlarmPageRequest allocateRecordPageRequest);
    
    List<FailureAlarmVO> listByPage(FailureAlarmPageRequest allocateRecordPageRequest);
    
    Triple update(FailureAlarmSaveRequest failureAlarmSaveRequest, Long uid);
    
    Triple delete(Long id, Long uid);
    
    R batchSet(FailureAlarmBatchSetRequest request, Long uid);
    
    void exportExcel(FailureAlarmPageRequest allocateRecordPageRequest, HttpServletResponse response);
    
    void refreshCache(FailureAlarm failureAlarm);
    
    void deleteCache(FailureAlarm failureAlarm);
}

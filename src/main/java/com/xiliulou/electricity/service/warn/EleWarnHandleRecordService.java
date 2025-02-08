package com.xiliulou.electricity.service.warn;


import com.xiliulou.electricity.entity.warn.EleWarnHandleRecord;

import java.util.List;
import java.util.Set;

/**
 * 告警处理操作记录(TEleWarnHandleRecord)表服务接口
 *
 * @author maxiaodong
 * @since 2024-11-07 13:53:35
 */
public interface EleWarnHandleRecordService {
    
    int batchInsert(List<EleWarnHandleRecord> warnHandleRecordList);
    
    List<EleWarnHandleRecord> listByBatchNoList(Set<String> batchNoSet);
}

package com.xiliulou.electricity.mapper.warn;

import com.xiliulou.electricity.entity.warn.EleWarnHandleRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 告警处理操作记录(TEleWarnHandleRecord)表数据库访问层
 *
 * @author maxiaodong
 * @since 2024-11-07 13:53:33
 */
public interface EleWarnHandleRecordMapper {
    
    int batchInsert(@Param("list") List<EleWarnHandleRecord> warnHandleRecordList);
    
    List<EleWarnHandleRecord> selectListByBatchNoList(@Param("batchNoList") Set<String> batchNoSet);
}


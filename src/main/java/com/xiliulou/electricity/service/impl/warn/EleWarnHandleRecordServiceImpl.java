package com.xiliulou.electricity.service.impl.warn;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.warn.EleWarnHandleRecord;
import com.xiliulou.electricity.mapper.warn.EleWarnHandleRecordMapper;
import com.xiliulou.electricity.service.warn.EleWarnHandleRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 告警处理操作记录(TEleWarnHandleRecord)表服务实现类
 *
 * @author maxiaodong
 * @since 2024-11-07 13:53:36
 */
@Service
public class EleWarnHandleRecordServiceImpl implements EleWarnHandleRecordService {
    @Resource
    private EleWarnHandleRecordMapper eleWarnHandleRecordMapper;
    
    
    @Override
    public int batchInsert(List<EleWarnHandleRecord> warnHandleRecordList) {
        return eleWarnHandleRecordMapper.batchInsert(warnHandleRecordList);
    }
    
    @Override
    @Slave
    public List<EleWarnHandleRecord> listByBatchNoList(Set<String> batchNoSet) {
        return eleWarnHandleRecordMapper.selectListByBatchNoList(batchNoSet);
    }
}

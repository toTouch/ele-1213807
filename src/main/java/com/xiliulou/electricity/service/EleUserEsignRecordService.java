package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleUserEsignRecord;
import com.xiliulou.electricity.query.EleUserEsignRecordQuery;
import com.xiliulou.electricity.vo.EleUserEsignRecordVO;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/10 11:27
 * @Description:
 */
public interface EleUserEsignRecordService {

    List<EleUserEsignRecordVO> queryUserEsignRecords(EleUserEsignRecordQuery eleUserEsignRecordQuery);

    Integer queryCount(EleUserEsignRecordQuery eleUserEsignRecordQuery);
    
    EleUserEsignRecord queryUserEsignRecordFromDB(Long uid, Long tenantId);
    
    EleUserEsignRecord queryEsignFinishedRecordByUser(Long uid, Long aLong);
    
    /**
     * 取消签署流程 逻辑删除
     *
     * @param eleUserEsignRecord
     * @return
     */
    Integer cancelEsignFlow(Long eleUserEsignRecord);
}

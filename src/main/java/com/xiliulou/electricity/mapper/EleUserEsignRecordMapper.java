package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleUserEsignRecord;
import com.xiliulou.electricity.query.EleUserEsignRecordQuery;
import com.xiliulou.electricity.vo.EleUserEsignRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/9 18:35
 * @Description:
 */
public interface EleUserEsignRecordMapper extends BaseMapper<EleUserEsignRecord> {
    int insertUserEsignRecord(EleUserEsignRecord eleUserEsignRecord);
    int updateUserEsignRecord(EleUserEsignRecord eleUserEsignRecord);
    EleUserEsignRecord selectLatestEsignRecordByUser(@Param("uid") Long uid, @Param("tenantId") Long tenantId);

    EleUserEsignRecord selectEsignRecordBySignFlowId(@Param("signFlowId") String signFlowId);

    List<EleUserEsignRecordVO> selectByPage(EleUserEsignRecordQuery query);

    Integer selectByPageCount(EleUserEsignRecordQuery query);

}

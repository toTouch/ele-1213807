package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EnableMemberCardRecord;
import com.xiliulou.electricity.entity.EsignCapacityRechargeRecord;
import com.xiliulou.electricity.query.EsignCapacityRechargeRecordQuery;
import com.xiliulou.electricity.query.FaceRecognizeRechargeRecordQuery;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/11 19:24
 * @Description:
 */
public interface EsignCapacityRechargeRecordMapper extends BaseMapper<EnableMemberCardRecord> {

    EsignCapacityRechargeRecord selectById(Long id);

    int insertOne(EsignCapacityRechargeRecord esignCapacityRechargeRecord);

    List<EsignCapacityRechargeRecord> selectByPage(EsignCapacityRechargeRecordQuery query);

    Integer selectByPageCount(EsignCapacityRechargeRecordQuery query);

}

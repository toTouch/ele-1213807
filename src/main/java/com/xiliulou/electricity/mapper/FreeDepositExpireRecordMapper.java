package com.xiliulou.electricity.mapper;


import com.xiliulou.electricity.entity.FreeDepositExpireRecord;
import com.xiliulou.electricity.query.FreeDepositExpireRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author : renhang
 * @description FreeDepositExpireRecordMapper
 * @date : 2025-02-25 14:14
 **/
public interface FreeDepositExpireRecordMapper {

    List<FreeDepositExpireRecord> selectByPage(FreeDepositExpireRecordQuery query);

    Integer selectByCount(FreeDepositExpireRecordQuery query);

    Integer selectByIds(@Param("ids") List<Long> ids);

    void updateStatus(@Param("ids") List<Long> ids);

    void updateRemark(@Param("id") Long id, @Param("remark") String remark);
}

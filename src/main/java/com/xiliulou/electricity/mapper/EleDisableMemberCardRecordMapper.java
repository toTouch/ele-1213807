package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜文件表(TEleDisableMemberCardRecord)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */
public interface EleDisableMemberCardRecordMapper extends BaseMapper<EleDisableMemberCardRecord> {

    List<EleDisableMemberCardRecord> queryList(@Param("query") ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery);

    Integer queryCount(@Param("query") ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery);

    EleDisableMemberCardRecord queryCreateTimeMaxEleDisableMemberCardRecord(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);

    List<EleDisableMemberCardRecord> queryDisableCardExpireRecord(@Param("offset") Integer offset, @Param("size") Integer size, @Param("nowTime") Long nowTime);
}

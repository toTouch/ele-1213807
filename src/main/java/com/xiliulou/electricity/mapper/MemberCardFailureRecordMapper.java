package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.CarMemberCardOrder;
import com.xiliulou.electricity.entity.MemberCardFailureRecord;
import com.xiliulou.electricity.query.RentCarMemberCardOrderQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 失效套餐表(memberCardFailureRecord)表数据库访问层
 *
 * @author hrp
 * @since 2022-12-21 09:47:25
 */
public interface MemberCardFailureRecordMapper extends BaseMapper<MemberCardFailureRecord> {


    List<MemberCardFailureRecord> queryFailureMemberCard(@Param("uid") Long uid, @Param("tenantId") Integer tenantID);


}

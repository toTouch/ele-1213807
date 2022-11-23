package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.query.ElectricityMemberCardOrderQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.query.MemberCardOrderQuery;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface EleDisableMemberCardRecordService {


    int save(EleDisableMemberCardRecord eleDisableMemberCardRecord);

    R list(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery);

    EleDisableMemberCardRecord queryCreateTimeMaxEleDisableMemberCardRecord(Long uid, Integer tenantId);

    R reviewDisableMemberCard(String disableMemberCardNo, String errMsg, Integer status);

    R queryCount(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery);

}

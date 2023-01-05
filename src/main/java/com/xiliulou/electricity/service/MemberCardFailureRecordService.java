package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.MemberCardFailureRecord;

import java.util.List;

public interface MemberCardFailureRecordService {

    void failureMemberCardTask();

    R queryFailureMemberCard(Long uid, Integer offset, Integer size);

    Integer insert(MemberCardFailureRecord memberCardFailureRecord);
}

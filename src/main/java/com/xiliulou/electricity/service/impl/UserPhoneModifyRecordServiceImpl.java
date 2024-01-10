package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.UserPhoneModifyRecord;
import com.xiliulou.electricity.mapper.UserPhoneModifyRecordMapper;
import com.xiliulou.electricity.service.UserPhoneModifyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户修改手机号后需要向该表插入数据，记录修改手机号的记录
 */
@Service
@Slf4j
public class UserPhoneModifyRecordServiceImpl implements UserPhoneModifyRecordService {
    
    @Autowired
    UserPhoneModifyRecordMapper userPhoneModifyRecordMapper;
    
    @Override
    public Integer insertOne(UserPhoneModifyRecord userPhoneModifyRecord) {
        return userPhoneModifyRecordMapper.insertOne(userPhoneModifyRecord);
    }
}

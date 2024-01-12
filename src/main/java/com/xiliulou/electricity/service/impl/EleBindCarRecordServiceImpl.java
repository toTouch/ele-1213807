package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.EleBindCarRecordMapper;
import com.xiliulou.electricity.query.EleBindCarRecordQuery;
import com.xiliulou.electricity.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 换电柜电池表(EleBindCArRecord)表服务实现类
 *
 * @author makejava
 * @since 2022-06-16 14:44:12
 */
@Service
@Slf4j
public class EleBindCarRecordServiceImpl extends ServiceImpl<EleBindCarRecordMapper, EleBindCarRecord> implements EleBindCarRecordService {

    @Resource
    private EleBindCarRecordMapper eleBindCarRecordMapper;

    @Override
    public void insert(EleBindCarRecord eleBindCarRecord) {
        eleBindCarRecordMapper.insert(eleBindCarRecord);
    }

    @Slave
    @Override
    public R queryList(EleBindCarRecordQuery eleBindCarRecordQuery) {
        return R.ok(eleBindCarRecordMapper.queryList(eleBindCarRecordQuery));
    }

    @Slave
    @Override
    public R queryCount(EleBindCarRecordQuery eleBindCarRecordQuery) {
        return R.ok(eleBindCarRecordMapper.queryCount(eleBindCarRecordQuery));
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param newPhone 新号码
     * @param oldPhone 旧号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByOldPhone(Integer tenantId,String newPhone,String oldPhone) {
        return eleBindCarRecordMapper.updatePhoneByOldPhone(tenantId,newPhone,oldPhone);
    }
}

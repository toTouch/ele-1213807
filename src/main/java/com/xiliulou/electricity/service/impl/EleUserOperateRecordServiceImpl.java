package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.EleUserOperateRecordMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 换电柜电池表(TEleUserOperateRecord)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Service
@Slf4j
public class EleUserOperateRecordServiceImpl extends ServiceImpl<EleUserOperateRecordMapper, EleUserOperateRecord> implements EleUserOperateRecordService {

    @Resource
    EleUserOperateRecordMapper eleUserOperateRecordMapper;

    @Override
    public void insert(EleUserOperateRecord eleUserOperateRecord) {
        eleUserOperateRecordMapper.insert(eleUserOperateRecord);
    }

    @Override
    public R queryList(Long uid,Long size,Long offset,Long beginTime,Long enTime,Integer operateModel) {
        Integer tenantId=TenantContextHolder.getTenantId();
        return R.ok(eleUserOperateRecordMapper.queryList(uid,size,offset,beginTime,enTime,operateModel,tenantId));
    }

    @Override
    public R queryCount(Long uid,Long beginTime,Long enTime,Integer operateModel) {
        Integer tenantId=TenantContextHolder.getTenantId();
        return R.ok(eleUserOperateRecordMapper.queryCount(uid,beginTime,enTime,operateModel,tenantId));
    }
}

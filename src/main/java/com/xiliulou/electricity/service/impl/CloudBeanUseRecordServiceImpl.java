package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.CloudBeanUseRecord;
import com.xiliulou.electricity.mapper.CloudBeanUseRecordMapper;
import com.xiliulou.electricity.service.CloudBeanUseRecordService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 云豆使用记录表(CloudBeanUseRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-18 10:35:13
 */
@Service("cloudBeanUseRecordService")
@Slf4j
public class CloudBeanUseRecordServiceImpl implements CloudBeanUseRecordService {
    @Resource
    private CloudBeanUseRecordMapper cloudBeanUseRecordMapper;

    @Override
    public CloudBeanUseRecord queryByIdFromDB(Long id) {
        return this.cloudBeanUseRecordMapper.queryById(id);
    }

    @Override
    public Integer insert(CloudBeanUseRecord cloudBeanUseRecord) {
        return this.cloudBeanUseRecordMapper.insert(cloudBeanUseRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CloudBeanUseRecord cloudBeanUseRecord) {
        return this.cloudBeanUseRecordMapper.update(cloudBeanUseRecord);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.cloudBeanUseRecordMapper.deleteById(id) > 0;
    }

    @Override
    public Double selectCloudBeanByUidAndType(Long uid, Integer type) {
        return this.cloudBeanUseRecordMapper.selectCloudBeanByUidAndType(uid,type);
    }
}

package com.xiliulou.electricity.service.impl.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanRecord;
import com.xiliulou.electricity.mapper.EnterpriseCloudBeanRecordMapper;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanRecordService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 企业云豆操作记录表(EnterpriseCloudBeanRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-14 10:16:20
 */
@Service("enterpriseCloudBeanRecordService")
@Slf4j
public class EnterpriseCloudBeanRecordServiceImpl implements EnterpriseCloudBeanRecordService {
    @Resource
    private EnterpriseCloudBeanRecordMapper enterpriseCloudBeanRecordMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseCloudBeanRecord queryByIdFromDB(Long id) {
        return this.enterpriseCloudBeanRecordMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseCloudBeanRecord queryByIdFromCache(Long id) {
        return null;
    }

    /**
     * 修改数据
     *
     * @param enterpriseCloudBeanRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EnterpriseCloudBeanRecord enterpriseCloudBeanRecord) {
        return this.enterpriseCloudBeanRecordMapper.update(enterpriseCloudBeanRecord);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.enterpriseCloudBeanRecordMapper.deleteById(id) > 0;
    }
}

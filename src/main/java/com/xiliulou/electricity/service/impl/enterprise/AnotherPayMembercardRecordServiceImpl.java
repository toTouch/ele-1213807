package com.xiliulou.electricity.service.impl.enterprise;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.enterprise.AnotherPayMembercardRecord;
import com.xiliulou.electricity.mapper.AnotherPayMembercardRecordMapper;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 代付记录表(AnotherPayMembercardRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-10-10 15:07:39
 */
@Service("anotherPayMembercardRecordService")
@Slf4j
public class AnotherPayMembercardRecordServiceImpl implements AnotherPayMembercardRecordService {
    
    @Resource
    private AnotherPayMembercardRecordMapper anotherPayMembercardRecordMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public AnotherPayMembercardRecord queryByIdFromDB(Long id) {
        return this.anotherPayMembercardRecordMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public AnotherPayMembercardRecord queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 修改数据
     *
     * @param anotherPayMembercardRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(AnotherPayMembercardRecord anotherPayMembercardRecord) {
        return this.anotherPayMembercardRecordMapper.update(anotherPayMembercardRecord);
        
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
        return this.anotherPayMembercardRecordMapper.deleteById(id) > 0;
    }
    
    @Override
    public int saveAnotherPayMembercardRecord(Long uid, String orderId, Integer tenantId) {
        AnotherPayMembercardRecord anotherPayMembercardRecord = new AnotherPayMembercardRecord();
        anotherPayMembercardRecord.setOrderId(orderId);
        anotherPayMembercardRecord.setUid(uid);
        anotherPayMembercardRecord.setCreateTime(System.currentTimeMillis());
        anotherPayMembercardRecord.setUpdateTime(System.currentTimeMillis());
        anotherPayMembercardRecord.setTenantId(tenantId);
        
        return this.anotherPayMembercardRecordMapper.insert(anotherPayMembercardRecord);
    }
    
    @Override
    public List<AnotherPayMembercardRecord> selectByUid(Long uid) {
        return this.anotherPayMembercardRecordMapper.selectList(new LambdaQueryWrapper<AnotherPayMembercardRecord>().eq(AnotherPayMembercardRecord::getUid,uid));
    }
    
    @Override
    public int deleteByUid(Long uid) {
        return this.anotherPayMembercardRecordMapper.deleteByUid(uid);
    }
}

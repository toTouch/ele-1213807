package com.xiliulou.electricity.service.impl.enterprise;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.enterprise.UserBehaviorRecord;
import com.xiliulou.electricity.mapper.enterprise.UserBehaviorRecordMapper;
import com.xiliulou.electricity.service.enterprise.UserBehaviorRecordService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * (UserBehaviorRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-27 17:08:37
 */
@Service("userBehaviorRecordService")
@Slf4j
public class UserBehaviorRecordServiceImpl implements UserBehaviorRecordService {
    
    @Resource
    private UserBehaviorRecordMapper userBehaviorRecordMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserBehaviorRecord queryByIdFromDB(Long id) {
        return this.userBehaviorRecordMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserBehaviorRecord queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 修改数据
     *
     * @param userBehaviorRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserBehaviorRecord userBehaviorRecord) {
        return this.userBehaviorRecordMapper.update(userBehaviorRecord);
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
        return this.userBehaviorRecordMapper.deleteById(id) > 0;
    }
    
    @Override
    public int saveUserBehaviorRecord(Long uid, String orderId, Integer type ,Integer tenantId) {
        UserBehaviorRecord userBehaviorRecord = new UserBehaviorRecord();
        userBehaviorRecord.setOrderId(orderId);
        userBehaviorRecord.setUid(uid);
        userBehaviorRecord.setType(type);
        userBehaviorRecord.setTenantId(tenantId);
        userBehaviorRecord.setCreateTime(System.currentTimeMillis());
        userBehaviorRecord.setUpdateTime(System.currentTimeMillis());
        return this.userBehaviorRecordMapper.insert(userBehaviorRecord);
    }
    
    @Override
    public List<UserBehaviorRecord> selectByUid(Long uid) {
        return this.userBehaviorRecordMapper.selectList(new LambdaQueryWrapper<UserBehaviorRecord>().eq(UserBehaviorRecord::getUid,uid));
    }
    
    @Override
    public int deleteByUid(Long uid) {
        return this.userBehaviorRecordMapper.deleteByUid(uid);
    }
}

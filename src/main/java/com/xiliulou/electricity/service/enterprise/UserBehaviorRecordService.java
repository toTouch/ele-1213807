package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.UserBehaviorRecord;

import java.util.List;

/**
 * (UserBehaviorRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-09-27 17:08:37
 */
public interface UserBehaviorRecordService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserBehaviorRecord queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UserBehaviorRecord queryByIdFromCache(Long id);
    
    /**
     * 修改数据
     *
     * @param userBehaviorRecord 实例对象
     * @return 实例对象
     */
    Integer update(UserBehaviorRecord userBehaviorRecord);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    int saveUserBehaviorRecord(Long uid, String orderId, Integer type, Integer tenantId);
    
    List<UserBehaviorRecord> selectByUid(Long uid);
    
    int deleteByUid(Long uid);
    
}

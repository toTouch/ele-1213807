package com.xiliulou.electricity.mapper.enterprise;

import com.xiliulou.electricity.entity.enterprise.UserBehaviorRecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserBehaviorRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-09-27 17:08:36
 */
public interface UserBehaviorRecordMapper extends BaseMapper<UserBehaviorRecord> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserBehaviorRecord queryById(Long id);
    
    /**
     * 修改数据
     *
     * @param userBehaviorRecord 实例对象
     * @return 影响行数
     */
    int update(UserBehaviorRecord userBehaviorRecord);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    int deleteByUid(Long uid);
}

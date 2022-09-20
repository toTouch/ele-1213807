package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.UserDataScope;
import com.xiliulou.electricity.mapper.UserDataScopeMapper;
import com.xiliulou.electricity.service.UserDataScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserDataScope)表服务实现类
 *
 * @author zzlong
 * @since 2022-09-19 14:22:34
 */
@Service("userDataScopeService")
@Slf4j
public class UserDataScopeServiceImpl implements UserDataScopeService {
    @Autowired
    private UserDataScopeMapper userDataScopeMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserDataScope selectByIdFromDB(Long id) {
        return this.userDataScopeMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserDataScope selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<UserDataScope> selectByPage(int offset, int limit) {
        return this.userDataScopeMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param userDataScope 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDataScope insert(UserDataScope userDataScope) {
        this.userDataScopeMapper.insertOne(userDataScope);
        return userDataScope;
    }

    /**
     * 修改数据
     *
     * @param userDataScope 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserDataScope userDataScope) {
        return this.userDataScopeMapper.update(userDataScope);

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
        return this.userDataScopeMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<UserDataScope> userDataScopes) {
        return this.userDataScopeMapper.batchInsert(userDataScopes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteByUid(Long uid) {
        return this.userDataScopeMapper.deleteByUid(uid);
    }

    @Override
    public List<UserDataScope> selectByUid(Long uid) {
        return this.userDataScopeMapper.selectByUid(uid);
    }
}

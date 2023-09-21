package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.UserAuthMessage;
import com.xiliulou.electricity.mapper.UserAuthMessageMapper;
import com.xiliulou.electricity.service.UserAuthMessageService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserAuthMessage)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-05 14:36:03
 */
@Service("userAuthMessageService")
@Slf4j
public class UserAuthMessageServiceImpl implements UserAuthMessageService {
    @Resource
    private UserAuthMessageMapper userAuthMessageMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserAuthMessage queryByIdFromDB(Long id) {
        return this.userAuthMessageMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserAuthMessage queryByIdFromCache(Long id) {
        return null;
    }

    /**
     * 修改数据
     *
     * @param userAuthMessage 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserAuthMessage userAuthMessage) {
        return this.userAuthMessageMapper.update(userAuthMessage);

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
        return this.userAuthMessageMapper.deleteById(id) > 0;
    }

    @Override
    public Integer insert(UserAuthMessage userAuthMessage) {
        return this.userAuthMessageMapper.insert(userAuthMessage);
    }

    @Override
    public UserAuthMessage selectLatestByUid(Long uid) {
        return this.userAuthMessageMapper.selectLatestByUid(uid);
    }
}

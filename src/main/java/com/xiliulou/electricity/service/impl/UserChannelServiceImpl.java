package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.UserChannel;
import com.xiliulou.electricity.mapper.UserChannelMapper;
import com.xiliulou.electricity.service.UserChannelService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserChannel)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-22 15:34:57
 */
@Service("userChannelService")
@Slf4j
public class UserChannelServiceImpl implements UserChannelService {
    
    @Resource
    private UserChannelMapper userChannelMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserChannel queryByIdFromDB(Long id) {
        return this.userChannelMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserChannel queryByIdFromCache(Long id) {
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
    public List<UserChannel> queryAllByLimit(int offset, int limit) {
        return this.userChannelMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param userChannel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserChannel insert(UserChannel userChannel) {
        this.userChannelMapper.insertOne(userChannel);
        return userChannel;
    }
    
    /**
     * 修改数据
     *
     * @param userChannel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserChannel userChannel) {
        return this.userChannelMapper.update(userChannel);
        
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
        return this.userChannelMapper.deleteById(id) > 0;
    }
}

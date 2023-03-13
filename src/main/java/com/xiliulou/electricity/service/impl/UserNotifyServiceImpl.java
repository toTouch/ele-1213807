package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserNotify;
import com.xiliulou.electricity.mapper.UserNotifyMapper;
import com.xiliulou.electricity.query.UserNotifyQuery;
import com.xiliulou.electricity.service.UserNotifyService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.UserNotifyVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserNotify)表服务实现类
 *
 * @author zgw
 * @since 2023-02-21 09:10:41
 */
@Service("userNotifyService")
@Slf4j
public class UserNotifyServiceImpl implements UserNotifyService {
    
    @Resource
    private UserNotifyMapper userNotifyMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserNotify queryByIdFromDB(Long id) {
        return this.userNotifyMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserNotify queryByIdFromCache(Long id) {
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
    public List<UserNotify> queryAllByLimit(int offset, int limit) {
        return this.userNotifyMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param userNotify 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserNotify insert(UserNotify userNotify) {
        this.userNotifyMapper.insertOne(userNotify);
        return userNotify;
    }
    
    /**
     * 修改数据
     *
     * @param userNotify 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserNotify userNotify) {
        return this.userNotifyMapper.update(userNotify);
        
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
        return this.userNotifyMapper.deleteById(id) > 0;
    }
    
    @Override
    public UserNotify queryByTenantId() {
        return this.userNotifyMapper.queryByTenantId(TenantContextHolder.getTenantId());
    }
    
    @Override
    public R deleteOne(Long id) {
        return R.ok(userNotifyMapper.deleteById(id));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R editOne(UserNotifyQuery userNotifyQuery) {
        if (Objects.isNull(userNotifyQuery.getStatus())) {
            userNotifyQuery.setStatus(UserNotify.STATUS_OFF);
        }
    
        if (Objects.equals(userNotifyQuery.getStatus(), UserNotify.STATUS_ON)) {
            if (StringUtils.isBlank(userNotifyQuery.getTitle()) || StringUtils.isBlank(userNotifyQuery.getContent())) {
                return R.fail("100368", "用户通知标题和内容不能为空");
            }
        
            if (Objects.isNull(userNotifyQuery.getBeginTime()) || Objects.isNull(userNotifyQuery.getEndTime())) {
                return R.fail("100369", "用户通知时间间隔不能为空");
            }
        }
        
        UserNotify userNotify = queryByTenantId();
        UserNotify updateAndInsert = new UserNotify();
        updateAndInsert.setContent(userNotifyQuery.getContent());
        updateAndInsert.setTitle(userNotifyQuery.getTitle());
        updateAndInsert.setBeginTime(userNotifyQuery.getBeginTime());
        updateAndInsert.setEndTime(userNotifyQuery.getEndTime());
        updateAndInsert.setStatus(userNotifyQuery.getStatus());
        updateAndInsert.setTenantId(TenantContextHolder.getTenantId());
        updateAndInsert.setUpdateTime(System.currentTimeMillis());
        
        if (Objects.isNull(userNotify)) {
            updateAndInsert.setCreateTime(System.currentTimeMillis());
            insert(updateAndInsert);
        } else {
            updateAndInsert.setId(userNotify.getId());
            update(updateAndInsert);
        }
        return R.ok();
    }
    
    @Override
    public R queryOne() {
        UserNotify userNotify = this.queryByTenantId();
        if (Objects.isNull(userNotify)) {
            return R.ok();
        }
        
        UserNotifyVo vo = new UserNotifyVo();
        BeanUtils.copyProperties(userNotify, vo);
        return R.ok(vo);
    }
}

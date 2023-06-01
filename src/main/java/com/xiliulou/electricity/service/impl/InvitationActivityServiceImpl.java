package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.InvitationActivity;
import com.xiliulou.electricity.mapper.InvitationActivityMapper;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.service.InvitationActivityService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (InvitationActivity)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-01 15:55:48
 */
@Service("invitationActivityService")
@Slf4j
public class InvitationActivityServiceImpl implements InvitationActivityService {
    @Resource
    private InvitationActivityMapper invitationActivityMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivity queryByIdFromDB(Long id) {
        return this.invitationActivityMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivity queryByIdFromCache(Long id) {
        return null;
    }

    @Override
    public List<InvitationActivity> selectByPage(InvitationActivityQuery query) {
        return null;
    }

    @Override
    public Integer selectByPageCount(InvitationActivityQuery query) {
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
    public List<InvitationActivity> queryAllByLimit(int offset, int limit) {
        return this.invitationActivityMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvitationActivity insert(InvitationActivity invitationActivity) {
        this.invitationActivityMapper.insertOne(invitationActivity);
        return invitationActivity;
    }

    /**
     * 修改数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivity invitationActivity) {
        return this.invitationActivityMapper.update(invitationActivity);

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
        return this.invitationActivityMapper.deleteById(id) > 0;
    }
}

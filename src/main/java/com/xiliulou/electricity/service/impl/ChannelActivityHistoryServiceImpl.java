package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ChannelActivityHistory;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ChannelActivityHistoryMapper;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.ChannelActivityHistoryVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (ChannelActivityHistory)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-23 09:24:25
 */
@Service
@Slf4j
public class ChannelActivityHistoryServiceImpl implements ChannelActivityHistoryService {
    
    @Resource
    private ChannelActivityHistoryMapper channelActivityHistoryMapper;
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ChannelActivityHistory queryByIdFromDB(Long id) {
        return this.channelActivityHistoryMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ChannelActivityHistory queryByIdFromCache(Long id) {
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
    public List<ChannelActivityHistory> queryAllByLimit(int offset, int limit) {
        return this.channelActivityHistoryMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param channelActivityHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChannelActivityHistory insert(ChannelActivityHistory channelActivityHistory) {
        this.channelActivityHistoryMapper.insertOne(channelActivityHistory);
        return channelActivityHistory;
    }
    
    /**
     * 修改数据
     *
     * @param channelActivityHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ChannelActivityHistory channelActivityHistory) {
        return this.channelActivityHistoryMapper.update(channelActivityHistory);
        
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
        return this.channelActivityHistoryMapper.deleteById(id) > 0;
    }
    
    /**
     * 查询邀请人邀请数量
     */
    @Override
    public Long queryInviteCount(Long uid) {
        return this.channelActivityHistoryMapper.queryInviteCount(uid);
    }
    
    @Override
    public ChannelActivityHistory queryByUid(Long uid) {
        return this.channelActivityHistoryMapper.queryByUid(uid);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryList(Long size, Long offset, String name, String phone) {
        List<ChannelActivityHistoryVo> query = channelActivityHistoryMapper
                .queryList(size, offset, name, phone, TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(query)) {
            return Triple.of(true, "", new ArrayList<>());
        }
        
        query.forEach(item -> {
            UserInfo inviteUserInfo = userInfoService.queryByUidFromDb(item.getInviteUid());
            if (Objects.nonNull(inviteUserInfo)) {
                item.setInviteName(inviteUserInfo.getName());
                item.setInvitePhone(inviteUserInfo.getPhone());
            }
            
            UserInfo channelUserInfo = userInfoService.queryByUidFromDb(item.getChannelUid());
            if (Objects.nonNull(inviteUserInfo)) {
                item.setChannelName(channelUserInfo.getName());
                item.setChannelPhone(channelUserInfo.getPhone());
            }
        });
        return Triple.of(true, "", query);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryCount(String name, String phone) {
        Long count = channelActivityHistoryMapper.queryCount(name, phone, TenantContextHolder.getTenantId());
        return Triple.of(true, "", count);
    }
}

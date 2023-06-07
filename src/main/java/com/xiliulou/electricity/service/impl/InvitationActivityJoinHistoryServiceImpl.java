package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.InvitationActivityJoinHistory;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.InvitationActivityJoinHistoryMapper;
import com.xiliulou.electricity.query.InvitationActivityJoinHistoryQuery;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.vo.InvitationActivityJoinHistoryVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (InvitationActivityJoinHistory)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-06 09:51:43
 */
@Service("invitationActivityJoinHistoryService")
@Slf4j
public class InvitationActivityJoinHistoryServiceImpl implements InvitationActivityJoinHistoryService {
    @Resource
    private InvitationActivityJoinHistoryMapper invitationActivityJoinHistoryMapper;
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityJoinHistory queryByIdFromDB(Long id) {
        return this.invitationActivityJoinHistoryMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityJoinHistory queryByIdFromCache(Long id) {
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
    public List<InvitationActivityJoinHistory> queryAllByLimit(int offset, int limit) {
        return this.invitationActivityJoinHistoryMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvitationActivityJoinHistory insert(InvitationActivityJoinHistory invitationActivityJoinHistory) {
        this.invitationActivityJoinHistoryMapper.insertOne(invitationActivityJoinHistory);
        return invitationActivityJoinHistory;
    }

    /**
     * 修改数据
     *
     * @param invitationActivityJoinHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivityJoinHistory invitationActivityJoinHistory) {
        return this.invitationActivityJoinHistoryMapper.update(invitationActivityJoinHistory);

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
        return this.invitationActivityJoinHistoryMapper.deleteById(id) > 0;
    }

    @Override
    public InvitationActivityJoinHistory selectByActivityAndInvitationUid(Long activityId, Long invitationUid, Long uid) {
        return this.invitationActivityJoinHistoryMapper.selectOne(new LambdaQueryWrapper<InvitationActivityJoinHistory>().eq(InvitationActivityJoinHistory::getActivityId, activityId)
                .eq(InvitationActivityJoinHistory::getUid, invitationUid).eq(InvitationActivityJoinHistory::getJoinUid, uid));
    }

    @Override
    public List<InvitationActivityJoinHistoryVO> selectByPage(InvitationActivityJoinHistoryQuery query) {
        List<InvitationActivityJoinHistoryVO> list = this.invitationActivityJoinHistoryMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.parallelStream().peek(item -> {
            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            item.setUserName(Objects.isNull(userInfo) ? "" : userInfo.getName());
        }).collect(Collectors.toList());

    }

    @Override
    public Integer selectByPageCount(InvitationActivityJoinHistoryQuery query) {
        return invitationActivityJoinHistoryMapper.selectByPageCount(query);
    }

    @Override
    public Integer updateStatusByActivityId(Long activityId, Integer status) {
        return invitationActivityJoinHistoryMapper.updateStatusByActivityId(activityId, status);
    }

    @Override
    public InvitationActivityJoinHistory selectByJoinIdAndStatus(Long uid, Integer status) {
        return invitationActivityJoinHistoryMapper.selectByJoinIdAndStatus(uid, status);
    }

    @Override
    public List<InvitationActivityJoinHistoryVO> selectUserByPage(InvitationActivityJoinHistoryQuery query) {
        List<InvitationActivityJoinHistoryVO> list = this.invitationActivityJoinHistoryMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list;
    }
}

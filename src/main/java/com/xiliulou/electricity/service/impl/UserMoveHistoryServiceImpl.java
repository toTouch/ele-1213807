package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.UserMoveHistory;
import com.xiliulou.electricity.mapper.UserMoveHistoryMapper;
import com.xiliulou.electricity.service.UserMoveHistoryService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
/**
 * 用户迁移历史记录(UserMoveHistory)表服务实现类
 *
 * @author makejava
 * @since 2021-08-16 09:26:11
 */
@Service("userMoveHistoryService")
@Slf4j
public class UserMoveHistoryServiceImpl implements UserMoveHistoryService {
    @Resource
    private UserMoveHistoryMapper userMoveHistoryMapper;

    /**
     * 新增数据
     *
     * @param userMoveHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserMoveHistory insert(UserMoveHistory userMoveHistory) {
        this.userMoveHistoryMapper.insert(userMoveHistory);
        return userMoveHistory;
    }

}

package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserMoveHistory;
import java.util.List;

/**
 * 用户迁移历史记录(UserMoveHistory)表服务接口
 *
 * @author makejava
 * @since 2021-08-16 09:26:11
 */
public interface UserMoveHistoryService {

    /**
     * 新增数据
     *
     * @param userMoveHistory 实例对象
     * @return 实例对象
     */
    UserMoveHistory insert(UserMoveHistory userMoveHistory);


}

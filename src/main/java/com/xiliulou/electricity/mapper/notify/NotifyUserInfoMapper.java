package com.xiliulou.electricity.mapper.notify;

import com.xiliulou.electricity.entity.notify.NotifyUserInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用户微信公众号通知表(TNotifyUserInfo)表数据库访问层
 *
 * @author makejava
 * @since 2024-06-26 14:38:32
 */
public interface NotifyUserInfoMapper {
    
    /**
     * 根据id查询
     *
     * @param id
     * @author caobotao.cbt
     * @date 2024/6/26 14:49
     */
    NotifyUserInfo selectById(@Param("id") Long id);
    /**
     * 根据电话查询
     *
     * @param phone
     * @author caobotao.cbt
     * @date 2024/6/26 14:49
     */
    NotifyUserInfo selectByPhone(@Param("phone") String phone);
    
    /**
     * 根据openId查询
     *
     * @param openId
     * @author caobotao.cbt
     * @date 2024/6/26 14:49
     */
    NotifyUserInfo selectByOpenId(@Param("openId") String openId);
    
    
    /**
     * 查询集合
     *
     * @param offset
     * @param size
     * @author caobotao.cbt
     * @date 2024/6/26 14:51
     */
    List<NotifyUserInfo> selectList(@Param("offset") Integer offset, @Param("size") Integer size);
    
    
    /**
     * 新增数据
     *
     * @param notifyUserInfo 实例对象
     * @return 影响行数
     */
    int insert(NotifyUserInfo notifyUserInfo);
    
    
    /**
     * 修改数据
     *
     * @param notifyUserInfo 实例对象
     * @return 影响行数
     */
    int update(NotifyUserInfo notifyUserInfo);
    
    
}


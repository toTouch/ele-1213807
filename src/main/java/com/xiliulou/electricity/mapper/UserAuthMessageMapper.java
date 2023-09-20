package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserAuthMessage;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserAuthMessage)表数据库访问层
 *
 * @author zzlong
 * @since 2023-09-05 14:36:03
 */
public interface UserAuthMessageMapper extends BaseMapper<UserAuthMessage> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    UserAuthMessage queryById(Long id);

    /**
     * 修改数据
     *
     * @param userAuthMessage 实例对象
     * @return 影响行数
     */
    int update(UserAuthMessage userAuthMessage);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    UserAuthMessage selectLatestByUid(Long uid);
}

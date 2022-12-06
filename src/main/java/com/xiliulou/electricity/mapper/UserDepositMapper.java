package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.UserDeposit;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (UserDeposit)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-06 13:40:21
 */
public interface UserDepositMapper extends BaseMapper<UserDeposit> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserDeposit selectByUid(@Param("uid") Long uid);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param userDeposit 实例对象
     * @return 对象列表
     */
    List<UserDeposit> selectByQuery(UserDeposit userDeposit);
    
    /**
     * 新增数据
     *
     * @param userDeposit 实例对象
     * @return 影响行数
     */
    int insertOne(UserDeposit userDeposit);
    
    /**
     * 修改数据
     *
     * @param userDeposit 实例对象
     * @return 影响行数
     */
    int updateByUid(UserDeposit userDeposit);
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 影响行数
     */
    int deleteByUid(Long uid);
    
}

package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 用户绑定列表(FranchiseeUserInfo)表数据库访问层
 *
 * @author makejava
 * @since 2021-06-17 10:10:13
 */
public interface FranchiseeUserInfoMapper  extends BaseMapper<FranchiseeUserInfo>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FranchiseeUserInfo queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<FranchiseeUserInfo> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param franchiseeUserInfo 实例对象
     * @return 对象列表
     */
    List<FranchiseeUserInfo> queryAll(FranchiseeUserInfo franchiseeUserInfo);

    /**
     * 新增数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 影响行数
     */
    int insertOne(FranchiseeUserInfo franchiseeUserInfo);

    /**
     * 修改数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 影响行数
     */
    int update(FranchiseeUserInfo franchiseeUserInfo);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
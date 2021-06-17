package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import java.util.List;

/**
 * 用户绑定列表(FranchiseeUserInfo)表服务接口
 *
 * @author makejava
 * @since 2021-06-17 10:10:13
 */
public interface FranchiseeUserInfoService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FranchiseeUserInfo queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    FranchiseeUserInfo queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<FranchiseeUserInfo> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 实例对象
     */
    FranchiseeUserInfo insert(FranchiseeUserInfo franchiseeUserInfo);

    /**
     * 修改数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 实例对象
     */
    Integer update(FranchiseeUserInfo franchiseeUserInfo);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

}
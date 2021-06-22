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

    List<FranchiseeUserInfo> queryByUserInfoId(Long id);

    Integer queryCountByBatterySn(String electricityBatterySn);

    Integer unBind(FranchiseeUserInfo franchiseeUserInfo);

    Integer minCount(Long id);

    void updateByUserInfoId(FranchiseeUserInfo franchiseeUserInfo);

    void plusCount(Long id);

	void updateRefund(FranchiseeUserInfo franchiseeUserInfo);
}

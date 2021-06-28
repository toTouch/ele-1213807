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
     * 修改数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 实例对象
     */
    Integer update(FranchiseeUserInfo franchiseeUserInfo);

    FranchiseeUserInfo queryByUserInfoId(Long id);

    Integer queryCountByBatterySn(String electricityBatterySn);

    Integer unBind(FranchiseeUserInfo franchiseeUserInfo);

    Integer minCount(Long id);

    void updateByUserInfoId(FranchiseeUserInfo franchiseeUserInfo);

    void plusCount(Long id);

	void updateRefund(FranchiseeUserInfo franchiseeUserInfo);

	void insert(FranchiseeUserInfo insertFranchiseeUserInfo);
}

package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.FranchiseeBind;

import java.util.List;

/**
 * (FranchiseeBind)表服务接口
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
public interface FranchiseeBindService {

    void deleteByFranchiseeId(Integer franchiseeId);

    void insert(FranchiseeBind franchiseeBind);

    List<FranchiseeBind> queryByFranchiseeId(Integer id);
}
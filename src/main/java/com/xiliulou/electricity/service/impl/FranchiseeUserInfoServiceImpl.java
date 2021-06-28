package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.mapper.FranchiseeUserInfoMapper;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
/**
 * 用户绑定列表(FranchiseeUserInfo)表服务实现类
 *
 * @author makejava
 * @since 2021-06-17 10:10:13
 */
@Service("franchiseeUserInfoService")
@Slf4j
public class FranchiseeUserInfoServiceImpl implements FranchiseeUserInfoService {
    @Resource
    private FranchiseeUserInfoMapper franchiseeUserInfoMapper;

    /**
     * 修改数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FranchiseeUserInfo franchiseeUserInfo) {
       return this.franchiseeUserInfoMapper.updateById(franchiseeUserInfo);

    }

    @Override
    public FranchiseeUserInfo queryByUserInfoId(Long id) {
        return franchiseeUserInfoMapper.selectOne(new LambdaQueryWrapper<FranchiseeUserInfo>()
        .eq(FranchiseeUserInfo::getUserInfoId,id).eq(FranchiseeUserInfo::getDelFlag,FranchiseeUserInfo.DEL_NORMAL));
    }

    @Override
    public Integer queryCountByBatterySn(String electricityBatterySn) {
        return franchiseeUserInfoMapper.selectCount(new LambdaQueryWrapper<FranchiseeUserInfo>()
                .eq(FranchiseeUserInfo::getNowElectricityBatterySn,electricityBatterySn).eq(FranchiseeUserInfo::getDelFlag,FranchiseeUserInfo.DEL_NORMAL));
    }

    @Override
    public Integer unBind(FranchiseeUserInfo franchiseeUserInfo) {
        return franchiseeUserInfoMapper.unBind(franchiseeUserInfo);
    }

    @Override
    public Integer minCount(Long id) {
        return franchiseeUserInfoMapper.minCount(id);
    }

    @Override
    public void updateByUserInfoId(FranchiseeUserInfo franchiseeUserInfo) {

    }

    @Override
    public void plusCount(Long id) {

    }

    @Override
    public void updateRefund(FranchiseeUserInfo franchiseeUserInfo) {

    }

    @Override
    public void insert(FranchiseeUserInfo insertFranchiseeUserInfo) {
        franchiseeUserInfoMapper.insert(insertFranchiseeUserInfo);
    }

}

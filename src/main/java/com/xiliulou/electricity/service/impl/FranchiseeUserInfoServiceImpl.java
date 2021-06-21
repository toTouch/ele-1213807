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
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FranchiseeUserInfo queryByIdFromDB(Long id) {
        return this.franchiseeUserInfoMapper.selectById(id);
    }


    /**
     * 新增数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FranchiseeUserInfo insert(FranchiseeUserInfo franchiseeUserInfo) {
        this.franchiseeUserInfoMapper.insert(franchiseeUserInfo);
        return franchiseeUserInfo;
    }

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
    public List<FranchiseeUserInfo> queryByUserInfoId(Long id) {
        return franchiseeUserInfoMapper.selectList(new LambdaQueryWrapper<FranchiseeUserInfo>()
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

}

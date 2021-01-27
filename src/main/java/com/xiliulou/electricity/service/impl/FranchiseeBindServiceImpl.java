package com.xiliulou.electricity.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.FranchiseeBind;
import com.xiliulou.electricity.mapper.FranchiseeBindMapper;
import com.xiliulou.electricity.service.FranchiseeBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * ( FranchiseeBind)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service("franchiseeBindService")
@Slf4j
public class FranchiseeBindServiceImpl implements FranchiseeBindService {

    @Resource
    FranchiseeBindMapper franchiseeBindMapper;

    @Override
    public void deleteByFranchiseeId(Integer franchiseeId) {
        franchiseeBindMapper.deleteByFranchiseeId(franchiseeId);
    }

    @Override
    public void insert(FranchiseeBind franchiseeBind) {
        franchiseeBindMapper.insert(franchiseeBind);
    }

    @Override
    public List<FranchiseeBind> queryByFranchiseeId(Integer id) {
        return franchiseeBindMapper.selectList(new LambdaQueryWrapper<FranchiseeBind>().eq(FranchiseeBind::getFranchiseeId,id));
    }
}
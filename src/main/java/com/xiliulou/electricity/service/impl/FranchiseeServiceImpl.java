package com.xiliulou.electricity.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBatteryBind;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeBind;
import com.xiliulou.electricity.mapper.FranchiseeMapper;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.BindFranchiseeQuery;
import com.xiliulou.electricity.query.FranchiseeAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.service.ElectricityBatteryBindService;
import com.xiliulou.electricity.service.FranchiseeBindService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ( Franchisee)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service("franchiseeService")
@Slf4j
public class FranchiseeServiceImpl implements FranchiseeService {
    @Resource
    FranchiseeMapper franchiseeMapper;

    @Autowired
    ElectricityBatteryBindService electricityBatteryBindService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    FranchiseeBindService franchiseeBindService;
    @Override
    public R save(FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        Franchisee franchisee = new Franchisee();
        BeanUtil.copyProperties(franchiseeAddAndUpdate, franchisee);
        franchisee.setCreateTime(System.currentTimeMillis());
        franchisee.setUpdateTime(System.currentTimeMillis());
        franchisee.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        int insert = franchiseeMapper.insert(franchisee);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            return null;
        });
        return R.ok();
    }

    @Override
    public R edit(FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        Franchisee franchisee = new Franchisee();
        BeanUtil.copyProperties(franchiseeAddAndUpdate, franchisee);
        franchisee.setUpdateTime(System.currentTimeMillis());
        int update = franchiseeMapper.updateById(franchisee);
        DbUtils.dbOperateSuccessThen(update, () -> {
            return null;
        });
        return R.ok();
    }

    @Override
    public R delete(Integer id) {
        Franchisee franchisee = queryByIdFromDB(id);
        if (Objects.isNull(franchisee)) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        franchisee.setUpdateTime(System.currentTimeMillis());
        franchisee.setDelFlag(ElectricityCabinet.DEL_DEL);
        int update = franchiseeMapper.updateById(franchisee);
        DbUtils.dbOperateSuccessThen(update, () -> {
            return null;
        });
        return R.ok();
    }

    @Override
    public Franchisee queryByIdFromDB(Integer id) {
        return franchiseeMapper.selectById(id);
    }

    @Override
    public R queryList(FranchiseeQuery franchiseeQuery) {
        Page page = PageUtil.getPage(franchiseeQuery.getOffset(), franchiseeQuery.getSize());
        franchiseeMapper.queryList(page, franchiseeQuery);
        if (ObjectUtil.isEmpty(page.getRecords())) {
            return R.ok(new ArrayList<>());
        }
        List<Franchisee> franchiseeVOList = page.getRecords();
        page.setRecords(franchiseeVOList.stream().sorted(Comparator.comparing(Franchisee::getCreateTime).reversed()).collect(Collectors.toList()));
        return R.ok(page);
    }

    @Override
    public R bindElectricityBattery(BindElectricityBatteryQuery bindElectricityBatteryQuery) {
        //先删除
        electricityBatteryBindService.deleteByFranchiseeId(bindElectricityBatteryQuery.getFranchiseeId());
        if(ObjectUtil.isEmpty(bindElectricityBatteryQuery.getElectricityBatteryIdList())){
            return R.ok();
        }
        //再新增
        for (Long electricityBatteryId : bindElectricityBatteryQuery.getElectricityBatteryIdList()) {
            ElectricityBatteryBind electricityBatteryBind=new ElectricityBatteryBind();
            electricityBatteryBind.setFranchiseeId(bindElectricityBatteryQuery.getFranchiseeId());
            electricityBatteryBind.setElectricityBatteryId(electricityBatteryId);
            electricityBatteryBindService.insert(electricityBatteryBind);
        }
        return R.ok();
    }

    @Override
    public R bindStore(BindFranchiseeQuery bindFranchiseeQuery) {
        //先删除
        franchiseeBindService.deleteByFranchiseeId(bindFranchiseeQuery.getFranchiseeId());
        if(ObjectUtil.isEmpty(bindFranchiseeQuery.getStoreIdList())){
            return R.ok();
        }
        //再新增
        for (Integer storeId : bindFranchiseeQuery.getStoreIdList()) {
            FranchiseeBind franchiseeBind=new FranchiseeBind();
            franchiseeBind.setFranchiseeId(bindFranchiseeQuery.getFranchiseeId());
            franchiseeBind.setStoreId(storeId);
            franchiseeBindService.insert(franchiseeBind);
        }
        return R.ok();
    }

    @Override
    public R getElectricityBatteryList(Integer id) {
        return R.ok(electricityBatteryBindService.queryByFranchiseeId(id));
    }

    @Override
    public R getStoreList(Integer id) {
        return R.ok(franchiseeBindService.queryByFranchiseeId(id));
    }

    @Override
    public List<Franchisee> queryByUid(Long uid) {
        return franchiseeMapper.selectList(new LambdaQueryWrapper<Franchisee>().eq(Franchisee::getUid,uid));
    }
}
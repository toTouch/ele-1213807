package com.xiliulou.electricity.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.FranchiseeBindElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeBind;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.FranchiseeMapper;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.BindFranchiseeQuery;
import com.xiliulou.electricity.query.FranchiseeAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.FranchiseeBindElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeBindService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.vo.FranchiseeVO;
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
    FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;

    @Autowired
    FranchiseeService franchiseeService;


    @Autowired
    CityService cityService;
    @Autowired
    UserService userService;
    @Override
    public R save(FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        //新增加盟商新增用户 TODO

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Franchisee franchisee = new Franchisee();
        BeanUtil.copyProperties(franchiseeAddAndUpdate, franchisee);
        franchisee.setCreateTime(System.currentTimeMillis());
        franchisee.setUpdateTime(System.currentTimeMillis());
        franchisee.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        franchisee.setTenantId(tenantId);
        int insert =franchiseeMapper.insert(franchisee);
        if(insert>0){
            return R.ok();
        }
return

    }

    @Override
    public R edit(FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        //判断用户存不存在
        User user=userService.queryByIdFromDB(franchiseeAddAndUpdate.getUid());
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Franchisee franchisee = new Franchisee();
        BeanUtil.copyProperties(franchiseeAddAndUpdate, franchisee);
        franchisee.setUpdateTime(System.currentTimeMillis());
        franchiseeMapper.updateById(franchisee);

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
        franchiseeMapper.updateById(franchisee);

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
        List<FranchiseeVO> franchiseeVOList = page.getRecords();
        if (ObjectUtil.isNotEmpty(franchiseeVOList)) {
            franchiseeVOList.parallelStream().forEach(e -> {
              //获取城市名称
                City city=cityService.queryByIdFromDB(e.getCid());
                if(Objects.nonNull(city)){
                    e.setCityName(city.getName());
                }
                //获取用户名称
                if(Objects.nonNull(e.getUid())) {
                    User user=userService.queryByUidFromCache(e.getUid());
                    if(Objects.nonNull(user)){
                        e.setUserName(user.getName());
                    }
                }
            });
        }
        page.setRecords(franchiseeVOList.stream().sorted(Comparator.comparing(FranchiseeVO::getCreateTime).reversed()).collect(Collectors.toList()));
        return R.ok(page);
    }

    @Override
    public R bindElectricityBattery(BindElectricityBatteryQuery bindElectricityBatteryQuery) {
        //先删除
        franchiseeBindElectricityBatteryService.deleteByFranchiseeId(bindElectricityBatteryQuery.getFranchiseeId());
        if(ObjectUtil.isEmpty(bindElectricityBatteryQuery.getElectricityBatteryIdList())){
            return R.ok();
        }
        //再新增
        for (Long electricityBatteryId : bindElectricityBatteryQuery.getElectricityBatteryIdList()) {
            FranchiseeBindElectricityBattery franchiseeBindElectricityBattery =new FranchiseeBindElectricityBattery();
            franchiseeBindElectricityBattery.setFranchiseeId(bindElectricityBatteryQuery.getFranchiseeId());
            franchiseeBindElectricityBattery.setElectricityBatteryId(electricityBatteryId);
            franchiseeBindElectricityBatteryService.insert(franchiseeBindElectricityBattery);
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
        return R.ok(franchiseeBindElectricityBatteryService.queryByFranchiseeId(id));
    }

    @Override
    public R getStoreList(Integer id) {
        return R.ok(franchiseeBindService.queryByFranchiseeId(id));
    }

    @Override
    public Franchisee queryByUid(Long uid) {
        return franchiseeMapper.selectOne(new LambdaQueryWrapper<Franchisee>().eq(Franchisee::getUid,uid).eq(Franchisee::getDelFlag,Franchisee.DEL_NORMAL));
    }

    @Override
    public Franchisee queryByCid(Integer cid) {
        return franchiseeMapper.selectOne(new LambdaQueryWrapper<Franchisee>().eq(Franchisee::getCid,cid).eq(Franchisee::getDelFlag,Franchisee.DEL_NORMAL));
    }
}

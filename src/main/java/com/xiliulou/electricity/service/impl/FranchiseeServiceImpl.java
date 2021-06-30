package com.xiliulou.electricity.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.FranchiseeBindElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.FranchiseeMapper;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.FranchiseeAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.FranchiseeBindElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.FranchiseeVO;
import com.xiliulou.electricity.web.query.AdminUserQuery;
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
    RedisService redisService;


    @Autowired
    CityService cityService;
    @Autowired
    UserService userService;
    @Override
    public R save(FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        //新增加盟商新增用户
        AdminUserQuery adminUserQuery = new AdminUserQuery();
        BeanUtil.copyProperties(franchiseeAddAndUpdate,adminUserQuery);
        adminUserQuery.setUserType(User.TYPE_USER_STORE);
        adminUserQuery.setLang(User.DEFAULT_LANG);
        adminUserQuery.setGender(User.GENDER_FEMALE);

        R result= userService.addInnerUser(adminUserQuery);
        if(result.getCode()==1){
            return result;
        }

        Long uid=(Long) result.getData();
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        Franchisee franchisee = new Franchisee();
        BeanUtil.copyProperties(franchiseeAddAndUpdate, franchisee);
        franchisee.setCreateTime(System.currentTimeMillis());
        franchisee.setUpdateTime(System.currentTimeMillis());
        franchisee.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        franchisee.setTenantId(tenantId);
        franchisee.setUid(uid);
        franchisee.setCid(franchiseeAddAndUpdate.getCityId());
        int insert =franchiseeMapper.insert(franchisee);

        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_FRANCHISEE + franchisee.getId(), franchisee);
            return null;
        });

        if (insert > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");

    }

    @Override
    public R edit(FranchiseeAddAndUpdate franchiseeAddAndUpdate) {

        Franchisee oldFranchisee = queryByIdFromCache(franchiseeAddAndUpdate.getId());
        if (Objects.isNull(oldFranchisee)) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        BeanUtil.copyProperties(franchiseeAddAndUpdate, oldFranchisee);
        oldFranchisee.setUpdateTime(System.currentTimeMillis());
        int update=franchiseeMapper.updateById(oldFranchisee);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //修改缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_FRANCHISEE + oldFranchisee.getId(), oldFranchisee);
            return null;
        });


        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    public R delete(Integer id) {

        Franchisee franchisee = queryByIdFromCache(id);
        if (Objects.isNull(franchisee)) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        //先删除用户
        Boolean result=userService.deleteById(franchisee.getUid());

        if(!result){
            return R.fail("ELECTRICITY.0086", "操作失败");
        }

        //再删除加盟商
        franchisee.setUpdateTime(System.currentTimeMillis());
        franchisee.setDelFlag(ElectricityCabinet.DEL_DEL);
        int update=franchiseeMapper.updateById(franchisee);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //修改缓存
            redisService.delete(ElectricityCabinetConstant.CACHE_FRANCHISEE + id);
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    public Franchisee queryByIdFromCache(Integer id) {
        Franchisee cacheFranchisee = redisService.getWithHash(ElectricityCabinetConstant.CACHE_FRANCHISEE + id, Franchisee.class);
        if (Objects.nonNull(cacheFranchisee)) {
            return cacheFranchisee;
        }
        Franchisee franchisee = franchiseeMapper.selectById(id);
        if (Objects.isNull(franchisee)) {
            return null;
        }
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_FRANCHISEE + id, franchisee);
        return franchisee;

    }

    @Override
    public R queryList(FranchiseeQuery franchiseeQuery) {
        List<FranchiseeVO> franchiseeVOList =franchiseeMapper.queryList(franchiseeQuery);
        if (ObjectUtil.isEmpty(franchiseeVOList)) {
            return R.ok(new ArrayList<>());
        }
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
        franchiseeVOList.stream().sorted(Comparator.comparing(FranchiseeVO::getCreateTime).reversed()).collect(Collectors.toList());
        return R.ok(franchiseeVOList);
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
    public R getElectricityBatteryList(Integer id) {
        return R.ok(franchiseeBindElectricityBatteryService.queryByFranchiseeId(id));
    }


    @Override
    public Franchisee queryByUid(Long uid) {
        return franchiseeMapper.selectOne(new LambdaQueryWrapper<Franchisee>().eq(Franchisee::getUid,uid).eq(Franchisee::getDelFlag,Franchisee.DEL_NORMAL));
    }

    @Override
    public R queryCount(FranchiseeQuery franchiseeQuery) {
        return R.ok(franchiseeMapper.queryCount(franchiseeQuery));
    }

}

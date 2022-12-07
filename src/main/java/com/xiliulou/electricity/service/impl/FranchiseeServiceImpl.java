package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.FranchiseeMapper;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.FranchiseeAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.query.FranchiseeSetSplitQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.FranchiseeVO;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
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
    FranchiseeService franchiseeService;

    @Autowired
    RedisService redisService;

    @Autowired
    CityService cityService;

    @Autowired
    UserService userService;

    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;

    @Autowired
    StoreService storeService;

    @Autowired
    FranchiseeAmountService franchiseeAmountService;

    @Autowired
    RoleService roleService;

    @Autowired
    ElectricityConfigService electricityConfigService;

    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    UserDataScopeService userDataScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R save(FranchiseeAddAndUpdate franchiseeAddAndUpdate) {

        //押金参数判断
        if (Objects.equals(franchiseeAddAndUpdate.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            if (Objects.isNull(franchiseeAddAndUpdate.getBatteryDeposit())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
        }

        if (Objects.equals(franchiseeAddAndUpdate.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (ObjectUtil.isEmpty(franchiseeAddAndUpdate.getModelBatteryDepositList())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }

            //封装型号押金
            String modelBatteryDeposit = JsonUtil.toJson(franchiseeAddAndUpdate.getModelBatteryDepositList());
            franchiseeAddAndUpdate.setModelBatteryDeposit(modelBatteryDeposit);

        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //新增加盟商新增用户
        AdminUserQuery adminUserQuery = new AdminUserQuery();
        BeanUtil.copyProperties(franchiseeAddAndUpdate, adminUserQuery);

        //admin用户新增加盟商
        adminUserQuery.setUserType(User.TYPE_USER_NORMAL_ADMIN);
        adminUserQuery.setDataType(User.DATA_TYPE_FRANCHISEE);
        if (!Objects.equals(tenantId, 1)) {
            //普通租户新增加盟商
            //1、查普通租户加盟商角色
            Long roleId = roleService.queryByName(Role.ROLE_FRANCHISEE_USER_NAME, tenantId);
            if (Objects.nonNull(roleId)) {
                adminUserQuery.setRoleId(roleId);
            }

        }

        adminUserQuery.setLang(User.DEFAULT_LANG);
        adminUserQuery.setGender(User.GENDER_FEMALE);

        R result = userService.addInnerUser(adminUserQuery);
        if (result.getCode() == 1) {
            return result;
        }

        Long uid = (Long) result.getData();

        Franchisee franchisee = new Franchisee();
        BeanUtil.copyProperties(franchiseeAddAndUpdate, franchisee);
        franchisee.setCreateTime(System.currentTimeMillis());
        franchisee.setUpdateTime(System.currentTimeMillis());
        franchisee.setDelFlag(ElectricityCabinet.DEL_NORMAL);
        franchisee.setTenantId(tenantId);
        franchisee.setUid(uid);
        franchisee.setCid(franchiseeAddAndUpdate.getCityId());
        int insert = franchiseeMapper.insert(franchisee);

        //新增加盟商账户
        FranchiseeAmount franchiseeAmount = FranchiseeAmount.builder()
                .franchiseeId(franchisee.getId())
                .delFlag(FranchiseeAmount.DEL_NORMAL)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .uid(uid)
                .balance(BigDecimal.valueOf(0.0))
                .totalIncome(BigDecimal.valueOf(0.0))
                .withdraw(BigDecimal.valueOf(0.0))
                .tenantId(tenantId)
                .build();
        franchiseeAmountService.insert(franchiseeAmount);
        
        //保存用户数据可见范围
        UserDataScope userDataScope = new UserDataScope();
        userDataScope.setUid(franchisee.getUid());
        userDataScope.setDataId(franchisee.getId());
        userDataScopeService.insert(userDataScope);
    
        if (insert > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R edit(FranchiseeAddAndUpdate franchiseeAddAndUpdate) {

        Franchisee oldFranchisee = queryByIdFromDB(franchiseeAddAndUpdate.getId());
        if (Objects.isNull(oldFranchisee)) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        if(!Objects.equals(oldFranchisee.getTenantId(),TenantContextHolder.getTenantId())){
            return R.ok();
        }

        //押金参数判断
        if (Objects.equals(franchiseeAddAndUpdate.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            if (Objects.isNull(franchiseeAddAndUpdate.getBatteryDeposit())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
        }

        if (Objects.equals(franchiseeAddAndUpdate.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (ObjectUtil.isEmpty(franchiseeAddAndUpdate.getModelBatteryDepositList())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }

            //封装型号押金
            String modelBatteryDeposit = JsonUtil.toJson(franchiseeAddAndUpdate.getModelBatteryDepositList());
            franchiseeAddAndUpdate.setModelBatteryDeposit(modelBatteryDeposit);

        }

        Franchisee newFranchisee = new Franchisee();
        BeanUtil.copyProperties(franchiseeAddAndUpdate, newFranchisee);
        newFranchisee.setUpdateTime(System.currentTimeMillis());
        newFranchisee.setCid(franchiseeAddAndUpdate.getCityId());
        int update = franchiseeMapper.updateById(newFranchisee);

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R delete(Long id) {

        Franchisee franchisee = queryByIdFromDB(id);
        if (Objects.isNull(franchisee)) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        if(!Objects.equals(franchisee.getTenantId(),TenantContextHolder.getTenantId())){
            return R.ok();
        }

        //查询加盟商是否绑定的有套餐
        List<ElectricityMemberCard> electricityMemberCardList =electricityMemberCardService.selectByFranchiseeId(id,franchisee.getTenantId());
        if(!CollectionUtils.isEmpty(electricityMemberCardList)){
            log.error("ELE ERROR! delete franchisee fail,franchisee has binding memberCard,franchiseeId={}",id);
            return R.fail(id,"100101", "删除失败，该加盟商已绑定套餐！");
        }

        //查询加盟商是否绑定门店
        List<Store> storeList=storeService.selectByFranchiseeId(id);
        if(!CollectionUtils.isEmpty(storeList)){
            log.error("ELE ERROR! delete franchisee fail,franchisee has binding store,franchiseeId={}",id);
            return R.fail(id,"100102", "删除失败，该加盟商已绑定门店！");
        }

        //查询加盟商是否绑定普通用户
        List<FranchiseeUserInfo> franchiseeUserInfoList=franchiseeUserInfoService.selectByFranchiseeId(id);
        if(!CollectionUtils.isEmpty(franchiseeUserInfoList)){
            log.error("ELE ERROR! delete franchisee fail,franchisee has binding user,franchiseeId={}",id);
            return R.fail(id,"100103", "删除失败，该加盟商已绑定用户！");
        }

        //查询加盟商是否绑定门店，绑定门店则不能删除
//        Integer count1 = storeService.queryCountByFranchiseeId(franchisee.getId());

        //查询加盟商是否绑定普通用户
//        Integer count2 = franchiseeUserInfoService.queryCountByFranchiseeId(franchisee.getId());

//        if (count1 > 0 || count2 > 0) {
//            return R.fail("加盟商已绑定门店或用户");
//        }

        //再删除加盟商
        franchisee.setUpdateTime(System.currentTimeMillis());
        franchisee.setDelFlag(ElectricityCabinet.DEL_DEL);
        int update = franchiseeMapper.updateById(franchisee);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除用户
            userService.deleteInnerUser(franchisee.getUid());

            //删除加盟商账号
            franchiseeAmountService.deleteByFranchiseeId(id);

            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    public Franchisee queryByIdFromDB(Long id) {
        return franchiseeMapper.selectById(id);

    }
    
    @Override
    public Franchisee queryByIdFromCache(Long id) {
        // TODO userFirstLogin分支
        return null;
    }
    
    @Override
    public R queryList(FranchiseeQuery franchiseeQuery) {
        List<FranchiseeVO> franchiseeVOList = franchiseeMapper.queryList(franchiseeQuery);
        if (ObjectUtil.isEmpty(franchiseeVOList)) {
            return R.ok(new ArrayList<>());
        }
        if (ObjectUtil.isNotEmpty(franchiseeVOList)) {
            franchiseeVOList.parallelStream().forEach(e -> {

                //获取城市名称
                City city = cityService.queryByIdFromDB(e.getCid());
                if (Objects.nonNull(city)) {
                    e.setCityName(city.getName());
                }

                //获取用户名称
                if (Objects.nonNull(e.getUid())) {
                    User user = userService.queryByUidFromCache(e.getUid());
                    if (Objects.nonNull(user)) {
                        e.setUserName(user.getName());
                    }
                }

                //加盟商押金
                if (Objects.equals(e.getModelType(), Franchisee.NEW_MODEL_TYPE)) {

                    //封装型号押金
                    List modelBatteryDepositList = JsonUtil.fromJson(e.getModelBatteryDeposit(), List.class);
                    e.setModelBatteryDepositList(modelBatteryDepositList);

                }

            });
        }
        franchiseeVOList.stream().sorted(Comparator.comparing(FranchiseeVO::getCreateTime).reversed()).collect(Collectors.toList());
        return R.ok(franchiseeVOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R bindElectricityBattery(BindElectricityBatteryQuery bindElectricityBatteryQuery) {
        //先删除
/*        franchiseeBindElectricityBatteryService.deleteByFranchiseeId(bindElectricityBatteryQuery.getFranchiseeId());
        if (ObjectUtil.isEmpty(bindElectricityBatteryQuery.getElectricityBatteryIdList())) {
            return R.ok();
        }
        //再新增
        for (Long electricityBatteryId : bindElectricityBatteryQuery.getElectricityBatteryIdList()) {
            //判断电池是否绑定加盟商
            Integer count = franchiseeBindElectricityBatteryService.queryCountByBattery(electricityBatteryId);

            if (count > 0) {
                return R.fail("SYSTEM.00113", "绑定失败，电池已绑定其他加盟商");
            }
            FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = new FranchiseeBindElectricityBattery();
            franchiseeBindElectricityBattery.setFranchiseeId(bindElectricityBatteryQuery.getFranchiseeId());
            franchiseeBindElectricityBattery.setElectricityBatteryId(electricityBatteryId);
            franchiseeBindElectricityBatteryService.insert(franchiseeBindElectricityBattery);
        }*/
        return R.ok();
    }

    @Override
    public R getElectricityBatteryList(Long id) {
//        return R.ok(franchiseeBindElectricityBatteryService.queryByFranchiseeId(id));
        return R.ok();
    }

    @Override
    public Franchisee queryByUid(Long uid) {
        return franchiseeMapper.selectOne(new LambdaQueryWrapper<Franchisee>().eq(Franchisee::getUid, uid).eq(Franchisee::getDelFlag, Franchisee.DEL_NORMAL));
    }

    @Override
    public R queryCount(FranchiseeQuery franchiseeQuery) {
        return R.ok(franchiseeMapper.queryCount(franchiseeQuery));
    }

    @Override
    public void deleteByUid(Long uid) {
        Franchisee franchisee = queryByUid(uid);
        if (Objects.nonNull(franchisee)) {
            //删除加盟商
            franchisee.setUpdateTime(System.currentTimeMillis());
            franchisee.setDelFlag(ElectricityCabinet.DEL_DEL);

            franchiseeMapper.updateById(franchisee);

        }
    }

    @Override
    public Integer queryByFanchisee(Long uid) {
        Franchisee franchisee = queryByUid(uid);
        if (Objects.isNull(franchisee)) {
            return 0;
        }
        //查询加盟商是否绑定门店，绑定门店则不能删除
        Integer count1 = storeService.queryCountByFranchiseeId(franchisee.getId());

        //查询加盟商是否绑定普通用户
        Integer count2 = franchiseeUserInfoService.queryCountByFranchiseeId(franchisee.getId());

        if (count1 > 0 || count2 > 0) {
            return 1;
        }

        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R setSplit(List<FranchiseeSetSplitQuery> franchiseeSetSplitQueryList) {
        if (ObjectUtils.isEmpty(franchiseeSetSplitQueryList)) {
            return R.fail("SYSTEM.0002", "参数不合法");
        }

        int totalPercent = 0;
        Long franchiseeId = null;

        for (FranchiseeSetSplitQuery franchiseeSetSplitQuery : franchiseeSetSplitQueryList) {

            //加盟商分账比列
            if (Objects.equals(franchiseeSetSplitQuery.getType(), FranchiseeSetSplitQuery.TYPE_FRANCHISEE)) {
                totalPercent = franchiseeSetSplitQuery.getPercent();
                franchiseeId = franchiseeSetSplitQuery.getId();
                Franchisee franchisee = new Franchisee();
                franchisee.setId(franchiseeSetSplitQuery.getId());
                franchisee.setPercent(franchiseeSetSplitQuery.getPercent());
                franchisee.setTenantId(TenantContextHolder.getTenantId());
                franchisee.setUpdateTime(System.currentTimeMillis());
                franchiseeMapper.update(franchisee);

            }

            //门店分账比列
            if (Objects.equals(franchiseeSetSplitQuery.getType(), FranchiseeSetSplitQuery.TYPE_STORE)) {
                Store store = new Store();
                store.setId(franchiseeSetSplitQuery.getId());
                store.setPercent(franchiseeSetSplitQuery.getPercent());
                store.setTenantId(TenantContextHolder.getTenantId());
                store.setUpdateTime(System.currentTimeMillis());
                storeService.updateById(store);
            }
        }

        if (Objects.nonNull(franchiseeId)) {
            List<Store> storeList = storeService.queryByFranchiseeId(franchiseeId);
            for (Store store : storeList) {
                totalPercent = totalPercent + store.getPercent();
            }
            if (totalPercent > 100) {
                throw new CustomBusinessException("总分账比列超过100");
            }
        }

        return R.ok();
    }

    @Override
    public Franchisee queryByElectricityBatteryId(Long id) {
        return franchiseeMapper.queryByElectricityBatteryId(id);
    }

    @Override
    public R queryByTenantId(Integer tenantId) {
        return R.ok(franchiseeMapper.selectList(new LambdaQueryWrapper<Franchisee>().eq(Franchisee::getTenantId, tenantId)
                .eq(Franchisee::getDelFlag, Franchisee.DEL_NORMAL)));
    }

    @Override
    public R queryByCabinetId(Integer id, Integer tenantId) {
        return R.ok(franchiseeMapper.queryByCabinetId(id,tenantId));
    }

	@Override
	public Franchisee queryByUserId(Long uid) {
		return franchiseeMapper.queryByUserId(uid);
	}

    @Override
    public Franchisee queryByIdAndTenantId(Long id, Integer tenantId) {
        return franchiseeMapper.selectOne(new LambdaQueryWrapper<Franchisee>().eq(Franchisee::getId, id).eq(Franchisee::getTenantId,tenantId));
    }

    @Override
    public Triple<Boolean, String, Object> selectListByQuery(FranchiseeQuery franchiseeQuery) {
        List<Franchisee> franchisees = franchiseeMapper.selectListByQuery(franchiseeQuery);
        if (CollectionUtils.isEmpty(franchisees)) {
            return Triple.of(true, "", Collections.EMPTY_LIST);
        }

        return Triple.of(true, "", franchisees);
    }
}

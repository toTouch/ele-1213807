package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.RolePermissionConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.TenantMapper;
import com.xiliulou.electricity.query.TenantAddAndUpdateQuery;
import com.xiliulou.electricity.query.TenantQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 租户表(Tenant)表服务实现类
 *
 * @author makejava
 * @since 2021-06-16 14:31:45
 */
@Service("tenantService")
@Slf4j
public class TenantServiceImpl implements TenantService {

    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private UserService userService;
    @Autowired
    RoleService roleService;
    @Autowired
    UserRoleService userRoleService;
    @Autowired
    private RolePermissionConfig permissionConfig;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private RedisService redisService;
    @Autowired
    EleAuthEntryService eleAuthEntryService;
    @Autowired
    ElectricityConfigService electricityConfigService;
    @Autowired
    private PermissionTemplateService permissionTemplateService;


    /**
     * 新增数据
     *
     * @param tenantAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addTenant(TenantAddAndUpdateQuery tenantAddAndUpdateQuery) {

        //限频
        boolean lockResult = redisService.setNx(CacheConstant.ELE_ADD_TENANT_CACHE + tenantAddAndUpdateQuery.getId(), "1", 5 * 1000L, false);
        if (!lockResult) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //判断用户名是否存在
        if (!Objects.isNull(userService.queryByUserName(tenantAddAndUpdateQuery.getName()))) {
            return R.fail("100105", "用户名已存在");
        }

        //1.保存租户信息
        tenantAddAndUpdateQuery.setCode(genTenantCode());
        tenantAddAndUpdateQuery.setCreateTime(System.currentTimeMillis());
        tenantAddAndUpdateQuery.setDelFlag(Tenant.DEL_NORMAL);
        tenantAddAndUpdateQuery.setStatus(Tenant.STA_NO_OUT);
        tenantAddAndUpdateQuery.setUpdateTime(System.currentTimeMillis());

        Tenant tenant = new Tenant();
        BeanUtil.copyProperties(tenantAddAndUpdateQuery, tenant);
        tenant.setExpireTime(System.currentTimeMillis() + 7 * 24 * 3600 * 1000);
        tenantMapper.insert(tenant);


        //3.构建三大角色，运营商，代理商，门店
        Role operateRole = new Role();
        operateRole.setName(CommonConstant.OPERATE_NAME);
        operateRole.setCode(CommonConstant.OPERATE_CODE);

        Role franchiseeRole = new Role();
        franchiseeRole.setName(CommonConstant.FRANCHISEE_NAME);
        franchiseeRole.setCode(CommonConstant.FRANCHISEE_CODE);

        Role storeRole = new Role();
        storeRole.setName(CommonConstant.STORE_NAME);
        storeRole.setCode(CommonConstant.STORE_CODE);

        //运维
        Role maintainRole = new Role();
        storeRole.setName(CommonConstant.MAINTAIN_NAME);
        storeRole.setCode(CommonConstant.MAINTAIN_CODE);

        ArrayList<Role> roleList = new ArrayList<>();
        roleList.add(operateRole);
        roleList.add(franchiseeRole);
        roleList.add(storeRole);
        roleList.add(maintainRole);

        roleList.forEach(item -> {
            item.setTenantId(tenant.getId());
            item.setUpdateTime(System.currentTimeMillis());
            item.setCreateTime(System.currentTimeMillis());
            item.setDesc("tenantRole");
            roleService.insert(item);
        });


        //新增用户
        AdminUserQuery adminUserQuery = new AdminUserQuery();
        adminUserQuery.setName(tenantAddAndUpdateQuery.getName());
        adminUserQuery.setPassword(tenantAddAndUpdateQuery.getPassword());
        adminUserQuery.setPhone(tenantAddAndUpdateQuery.getPhone());
        adminUserQuery.setGender(User.GENDER_MALE);
        adminUserQuery.setUserType(User.TYPE_USER_OPERATE);
        adminUserQuery.setLang(User.DEFAULT_LANG);
        adminUserQuery.setCityId(null);
        adminUserQuery.setProvinceId(null);
        adminUserQuery.setDataType(User.DATA_TYPE_OPERATE);
        TenantContextHolder.setTenantId(tenant.getId());
        R result = userService.addInnerUser(adminUserQuery);
        if (result.getCode() == 1) {
            return result;
        }


        //获取角色默认权限
        List<RolePermission> permissionList = buildDefaultPermission(operateRole, franchiseeRole, storeRole, maintainRole);
        //保存角色默认权限
        if(CollectionUtils.isNotEmpty(permissionList)){
            permissionList.parallelStream().forEach(e -> {
                rolePermissionService.insert(e);
            });
        }



/*        //5.角色赋予权限
        List<RolePermission> operateRolePermission = permissionConfig.getOperator().parallelStream().map(item -> {
            RolePermission operatorRP = new RolePermission();
            operatorRP.setPId(item);
            operatorRP.setRoleId(operateRole.getId());
            return operatorRP;
        }).collect(Collectors.toList());

        ArrayList<RolePermission> rolePermissionList = new ArrayList<>(operateRolePermission);

        List<RolePermission> franchiseeRolePermission = permissionConfig.getAlliance().parallelStream().map(item -> {
            RolePermission allianceRP = new RolePermission();
            allianceRP.setPId(item);
            allianceRP.setRoleId(franchiseeRole.getId());
            return allianceRP;
        }).collect(Collectors.toList());
        rolePermissionList.addAll(franchiseeRolePermission);

        List<RolePermission> storeRolePermission = permissionConfig.getShop().parallelStream().map(item -> {
            RolePermission shopRP = new RolePermission();
            shopRP.setPId(item);
            shopRP.setRoleId(storeRole.getId());
            return shopRP;
        }).collect(Collectors.toList());
        rolePermissionList.addAll(storeRolePermission);

        rolePermissionList.parallelStream().forEach(e -> {
            rolePermissionService.insert(e);
        });*/


        //新增实名认证审核项
        eleAuthEntryService.insertByTenantId(tenant.getId());

        //新增租户给租户添加的默认系统配置
        ElectricityConfig electricityConfig = ElectricityConfig.builder()
                .name("")
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(tenant.getId())
                .isManualReview(ElectricityConfig.MANUAL_REVIEW)
                .isWithdraw(ElectricityConfig.WITHDRAW)
                .isOpenDoorLock(ElectricityConfig.OPEN_DOOR_LOCK)
                .isBatteryReview(ElectricityConfig.NON_BATTERY_REVIEW).build();
        electricityConfigService.insertElectricityConfig(electricityConfig);

        return R.ok();
    }

    /**
     * 获取角色默认权限
     * @param operateRole
     * @param franchiseeRole
     * @param storeRole
     * @return
     */
    private List<RolePermission> buildDefaultPermission(Role operateRole, Role franchiseeRole, Role storeRole,Role maintainRole) {
        List<RolePermission> rolePermissionList = Lists.newArrayList();

        List<PermissionTemplate> permissions = permissionTemplateService.selectByPage(0, Integer.MAX_VALUE);
        if (CollectionUtils.isEmpty(permissions)) {
            return rolePermissionList;
        }

        //默认权限分组
        Map<Integer, List<PermissionTemplate>> permissionMap = permissions.stream().collect(Collectors.groupingBy(PermissionTemplate::getType));
        if (CollectionUtils.isEmpty(permissionMap)) {
            return rolePermissionList;
        }

        //运营商权限
        List<PermissionTemplate> operatePermissions = permissionMap.get(PermissionTemplate.TYPE_OPERATE);
        if (CollectionUtils.isNotEmpty(operatePermissions)) {
            List<RolePermission> operateRolePermission = operatePermissions.parallelStream().map(item -> {
                RolePermission operatorPermission = new RolePermission();
                operatorPermission.setPId(item.getPid());
                operatorPermission.setRoleId(operateRole.getId());
                return operatorPermission;
            }).collect(Collectors.toList());
            rolePermissionList.addAll(operateRolePermission);
        }

        //加盟商权限
        List<PermissionTemplate> franchiseePermissions = permissionMap.get(PermissionTemplate.TYPE_FRANCHISEE);
        if (CollectionUtils.isNotEmpty(franchiseePermissions)) {
            List<RolePermission> franchiseeRolePermission = franchiseePermissions.parallelStream().map(item -> {
                RolePermission franchiseePermission = new RolePermission();
                franchiseePermission.setPId(item.getPid());
                franchiseePermission.setRoleId(franchiseeRole.getId());
                return franchiseePermission;
            }).collect(Collectors.toList());
            rolePermissionList.addAll(franchiseeRolePermission);
        }

        //门店权限
        List<PermissionTemplate> storePermissions = permissionMap.get(PermissionTemplate.TYPE_STORE);
        if (CollectionUtils.isNotEmpty(storePermissions)) {
            List<RolePermission> storeRolePermission = storePermissions.parallelStream().map(item -> {
                RolePermission shopPermission = new RolePermission();
                shopPermission.setPId(item.getPid());
                shopPermission.setRoleId(storeRole.getId());
                return shopPermission;
            }).collect(Collectors.toList());
            rolePermissionList.addAll(storeRolePermission);
        }

        //运维权限
        List<PermissionTemplate> maintainPermissions = permissionMap.get(PermissionTemplate.TYPE_MAINTAIN);
        if (CollectionUtils.isNotEmpty(maintainPermissions)) {
            List<RolePermission> maintainRolePermission = maintainPermissions.parallelStream().map(item -> {
                RolePermission maintainPermission = new RolePermission();
                maintainPermission.setPId(item.getPid());
                maintainPermission.setRoleId(maintainRole.getId());
                return maintainPermission;
            }).collect(Collectors.toList());
            rolePermissionList.addAll(maintainRolePermission);
        }

        return rolePermissionList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R editTenant(TenantAddAndUpdateQuery tenantAddAndUpdateQuery) {
        Tenant tenant = tenantMapper.selectById(tenantAddAndUpdateQuery.getId());
        if (Objects.isNull(tenant)) {
            return R.fail("ELECTRICITY.00101", "找不到租户");
        }
        //修改租户信息
        tenant.setStatus(tenantAddAndUpdateQuery.getStatus());
        tenant.setUpdateTime(System.currentTimeMillis());
        tenant.setExpireTime(tenantAddAndUpdateQuery.getExpireTime());
        tenant.setName(tenantAddAndUpdateQuery.getName());
        tenantMapper.updateById(tenant);

        redisService.saveWithHash(CacheConstant.CACHE_TENANT_ID + tenant.getId(), tenant);
        return R.ok();
    }

    @Override
    public R queryListTenant(TenantQuery tenantQuery) {
        return R.ok(tenantMapper.queryAll(tenantQuery));
    }

    @Override
    public Tenant queryByIdFromCache(Integer tenantId) {
        Tenant cacheTenant = redisService.getWithHash(CacheConstant.CACHE_TENANT_ID + tenantId, Tenant.class);
        if (Objects.nonNull(cacheTenant)) {
            return cacheTenant;
        }

        Tenant tenant = this.tenantMapper.selectById(tenantId);
        if (Objects.isNull(tenant)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_TENANT_ID + tenantId, tenant);
        return tenant;
    }

    @Override
    public R queryCount(TenantQuery tenantQuery) {
        return R.ok(tenantMapper.queryCount(tenantQuery));
    }

    @Override
    public Integer querySumCount(TenantQuery tenantQuery) {
        return tenantMapper.queryCount(tenantQuery);
    }


    /**
     * 生成新的租户code
     */
    private String genTenantCode() {
        String code = RandomStringUtils.randomAlphabetic(6);
        if (tenantMapper.selectCount(Wrappers.<Tenant>lambdaQuery().eq(Tenant::getCode, code)) == 0) {
            return code;
        }
        return genTenantCode();
    }



}

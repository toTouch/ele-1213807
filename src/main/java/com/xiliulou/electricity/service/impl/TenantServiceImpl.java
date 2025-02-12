package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ChannelActivity;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityConfigExtra;
import com.xiliulou.electricity.entity.FreeDepositData;
import com.xiliulou.electricity.entity.PermissionTemplate;
import com.xiliulou.electricity.entity.Role;
import com.xiliulou.electricity.entity.RolePermission;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.TenantNote;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.TenantMapper;
import com.xiliulou.electricity.mapper.asset.AssetWarehouseMapper;
import com.xiliulou.electricity.query.TenantAddAndUpdateQuery;
import com.xiliulou.electricity.query.TenantQuery;
import com.xiliulou.electricity.query.asset.AssetWarehouseSaveOrUpdateQueryModel;
import com.xiliulou.electricity.request.InitTenantSubscriptRequest;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.ChannelActivityService;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.service.ElectricityConfigExtraService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.PermissionTemplateService;
import com.xiliulou.electricity.service.RolePermissionService;
import com.xiliulou.electricity.service.RoleService;
import com.xiliulou.electricity.service.TenantNoteService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserRoleService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.faq.FaqCategoryV2Service;
import com.xiliulou.electricity.service.retrofit.MsgCenterRetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.TenantVO;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.StringConstant.SPACE;

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
    private RolePermissionService rolePermissionService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    EleAuthEntryService eleAuthEntryService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    private PermissionTemplateService permissionTemplateService;
    
    @Autowired
    private FreeDepositDataService freeDepositDataService;
    
    @Autowired
    private BatteryModelService batteryModelService;
    
    @Autowired
    private ChannelActivityService channelActivityService;
    
    @Resource
    private TenantNoteService noteService;
    
    @Resource
    private AssetWarehouseMapper assetWarehouseMapper;
    
    @Resource
    private FaqCategoryV2Service faqCategoryV2Service;
    
    @Resource
    private MsgCenterRetrofitService msgCenterRetrofitService;
    
    @Resource
    private ElectricityConfigExtraService electricityConfigExtraService;
    
    
    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("tenantHandlerExecutors", 2, "TENANT_HANDLER_EXECUTORS");
    
    ExecutorService initOtherExecutorService = XllThreadPoolExecutors.newFixedThreadPool("initTenantOther", 2, "INIT_TENANT_OTHER");
    
    /**
     * 新增数据
     *
     * @param tenantAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addTenant(TenantAddAndUpdateQuery tenantAddAndUpdateQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail(false, "ELECTRICITY.0001", "未找到用户");
        }
        //限频
        boolean lockResult = redisService.setNx(CacheConstant.ELE_ADD_TENANT_CACHE + user.getUid(), "1", 5 * 1000L, false);
        if (!lockResult) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        tenantAddAndUpdateQuery.setName(tenantAddAndUpdateQuery.getName().replaceAll(SPACE, ""));
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
        
        //保存租户默认电池型号
        batteryModelService.batchInsertDefaultBatteryModel(BatteryModelServiceImpl.generateDefaultBatteryModel(tenant.getId()));
        
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
        
        // 商户角色，渠道角色，商户员工
        Role merchantRole = new Role();
        merchantRole.setName(CommonConstant.MERCHANT_NAME);
        merchantRole.setCode(CommonConstant.MERCHANTL_CODE);
        
        Role channelRole = new Role();
        channelRole.setName(CommonConstant.CHANNEL_NAME);
        channelRole.setCode(CommonConstant.CHANNEL_CODE);
        
        Role merchantEmployeeRole = new Role();
        merchantEmployeeRole.setName(CommonConstant.MERCHANT_EMPLOYEE_NAME);
        merchantEmployeeRole.setCode(CommonConstant.MERCHANT_EMPLOYEE_CODE);
        //运维
        Role maintainRole = new Role();
        maintainRole.setName(CommonConstant.MAINTAIN_NAME);
        maintainRole.setCode(CommonConstant.MAINTAIN_CODE);
        
        ArrayList<Role> roleList = new ArrayList<>();
        roleList.add(operateRole);
        roleList.add(franchiseeRole);
        roleList.add(storeRole);
        roleList.add(merchantRole);
        roleList.add(channelRole);
        roleList.add(merchantEmployeeRole);
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
        adminUserQuery.setUserType(User.TYPE_USER_NORMAL_ADMIN);
        adminUserQuery.setLang(User.DEFAULT_LANG);
        adminUserQuery.setCityId(null);
        adminUserQuery.setProvinceId(null);
        adminUserQuery.setDataType(User.DATA_TYPE_OPERATE);
        TenantContextHolder.setTenantId(tenant.getId());
        R result = userService.addInnerUser(adminUserQuery);
        if (result.getCode() == 1) {
            return result;
        }
        
        initOtherExecutorService.execute(() -> {
            //获取角色默认权限
            List<RolePermission> permissionList = buildDefaultPermission(operateRole, franchiseeRole, storeRole, maintainRole);
            //保存角色默认权限
            if (CollectionUtils.isNotEmpty(permissionList)) {
                permissionList.parallelStream().forEach(e -> {
                    rolePermissionService.insert(e);
                });
            }
        });
        
        //新增实名认证审核项
        eleAuthEntryService.insertByTenantId(tenant.getId());
        
        //新增租户给租户添加的默认系统配置
        ElectricityConfig electricityConfig = ElectricityConfig.builder().name("").createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(tenant.getId()).isManualReview(ElectricityConfig.MANUAL_REVIEW).isWithdraw(ElectricityConfig.NON_WITHDRAW)
                .isOpenDoorLock(ElectricityConfig.NON_OPEN_DOOR_LOCK).disableMemberCard(ElectricityConfig.DISABLE_MEMBER_CARD).isBatteryReview(ElectricityConfig.NON_BATTERY_REVIEW)
                .lowChargeRate(NumberConstant.TWENTY_FIVE_DB).fullChargeRate(NumberConstant.SEVENTY_FIVE_DB).chargeRateType(ElectricityConfig.CHARGE_RATE_TYPE_UNIFY).build();
        electricityConfigService.insertElectricityConfig(electricityConfig);
        
        // 给租户添加默认的系统配置扩展
        electricityConfigExtraService.insert(
                ElectricityConfigExtra.builder().tenantId(tenant.getId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build());
        
        //新增租户给租户增加渠道活动（产品提的需求）
        final ChannelActivity channelActivity = new ChannelActivity();
        channelActivity.setName("渠道活动");
        channelActivity.setValidDayLimit(ChannelActivity.VALID_DAY_NOT_LIMIT);
        channelActivity.setStatus(ChannelActivity.STATUS_FORBIDDEN);
        channelActivity.setBindActivityType(ChannelActivity.BIND_ACTIVITY_TYPE_NONE);
        channelActivity.setTenantId(tenant.getId().longValue());
        channelActivity.setCreateTime(System.currentTimeMillis());
        channelActivity.setUpdateTime(System.currentTimeMillis());
        executorService.submit(() -> channelActivityService.insert(channelActivity));
        final AssetWarehouseSaveOrUpdateQueryModel warehouseSaveOrUpdateQueryModel = AssetWarehouseSaveOrUpdateQueryModel.builder().name(AssetConstant.ASSET_WAREHOUSE_DEFAULT_NAME)
                .status(AssetConstant.ASSET_WAREHOUSE_STATUS_ENABLE).delFlag(AssetConstant.DEL_NORMAL).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .franchiseeId(0L).tenantId(TenantContextHolder.getTenantId()).build();
        executorService.submit(() -> assetWarehouseMapper.insertOne(warehouseSaveOrUpdateQueryModel));
        
        initOtherExecutorService.submit(() -> {
            InitTenantSubscriptRequest subscriptRequest = InitTenantSubscriptRequest.builder().tenantIds(Arrays.asList(tenant.getId())).build();
            msgCenterRetrofitService.initTenantSubscriptMsg(subscriptRequest);
        });
        
        // 初始化常见问题
        executorService.submit(() -> faqCategoryV2Service.initFaqByTenantId(tenant.getId(), user.getUid()));
        
        return R.ok();
    }
    
    /**
     * 获取角色默认权限
     *
     * @param operateRole
     * @param franchiseeRole
     * @param storeRole
     * @return
     */
    private List<RolePermission> buildDefaultPermission(Role operateRole, Role franchiseeRole, Role storeRole, Role maintainRole) {
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
        tenant.setName(tenantAddAndUpdateQuery.getName().replaceAll(SPACE, ""));
        tenantMapper.updateById(tenant);
        
        redisService.saveWithHash(CacheConstant.CACHE_TENANT_ID + tenant.getId(), tenant);
        return R.ok();
    }
    
    @Slave
    @Override
    public R queryListTenant(TenantQuery tenantQuery) {
        List<TenantVO> tenantVOS = tenantMapper.queryAll(tenantQuery);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(tenantVOS)) {
            return R.ok(Collections.EMPTY_LIST);
        }
    
        List<Integer> tenantIdList = tenantVOS.stream().map(TenantVO::getId).collect(Collectors.toList());
        List<TenantNote> tenantNoteList =  noteService.listByTenantIdList(tenantIdList);
        Map<Integer, Long>  tenantNoteNumMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(tenantNoteList)) {
            tenantNoteNumMap = tenantNoteList.stream().collect(Collectors.toMap(TenantNote::getTenantId, TenantNote::getNoteNum, (oldValue, newValue) -> newValue));
        }
        Map<Integer, Long> finalTenantNoteNumMap = tenantNoteNumMap;
        tenantVOS.parallelStream().peek(item->{
            FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(item.getId());
            if (Objects.nonNull(freeDepositData)) {
                item.setFreeDepositCapacity(freeDepositData.getFreeDepositCapacity());
                item.setFyFreeDepositCapacity(freeDepositData.getFyFreeDepositCapacity());
                item.setByStagesCapacity(freeDepositData.getByStagesCapacity());
            }
    
            // 查询短信次数
            if (finalTenantNoteNumMap.containsKey(item.getId())) {
                item.setNoteNum(finalTenantNoteNumMap.get(item.getId()));
            }
          
        }).collect(Collectors.toList());
        
        return R.ok(tenantVOS);
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
    
    @Slave
    @Override
    public R queryCount(TenantQuery tenantQuery) {
        return R.ok(tenantMapper.queryCount(tenantQuery));
    }
    
    @Slave
    @Override
    public Integer querySumCount(TenantQuery tenantQuery) {
        return tenantMapper.queryCount(tenantQuery);
    }
    
    @Slave
    @Override
    public List<Integer> queryIdListByStartId(Integer startId, Integer size) {
        return tenantMapper.selectIdListByStartId(startId, size);
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

package com.xiliulou.electricity.service.impl;

import cn.hutool.core.lang.Pair;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.async.AsyncTransaction;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.GrantRolePermission;
import com.xiliulou.electricity.entity.PermissionResource;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.enums.supper.GrantType;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.mapper.RoleMapper;
import com.xiliulou.electricity.mapper.RolePermissionMapper;
import com.xiliulou.electricity.query.supper.UserGrantSourceReq;
import com.xiliulou.electricity.service.PermissionResourceService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.service.supper.AdminSupperService;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import com.xiliulou.electricity.tx.AdminSupperTxService;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.web.query.battery.BatteryBatchOperateQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: Ant
 * @Date 2024/4/22
 * @Description:
 **/
@Slf4j
@Service
public class AdminSupperServiceImpl implements AdminSupperService {
    
    @Resource
    private BatteryPlatRetrofitService batteryPlatRetrofitService;
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private AssetInventoryService assetInventoryService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private AdminSupperTxService adminSupperTxService;
    
    @Resource
    private ElectricityBatteryMapper electricityBatteryMapper;
    
    @Resource
    private AsyncTransaction asyncTransaction;
    
    @Resource
    private RolePermissionMapper rolePermissionMapper;
    
    @Resource
    private RoleMapper roleMapper;
    
    private final TtlXllThreadPoolExecutorServiceWrapper serviceWrapper = TtlXllThreadPoolExecutorsSupport.get(
            XllThreadPoolExecutors.newFixedThreadPool("ADMIN_SUPPER_POOL_EXECUTOR", 1, "admin-supper-executor"));
    @Autowired
    private PermissionResourceService permissionResourceService;

    /**
     * 根据电池SN删除电池
     *
     * @param tenantId      租户ID
     * @param batterySnList 电池SN集
     * @param violentDel 是否暴力删除
     * @return Pair<已删除的电池编码 、 未删除的电池编码>
     */
    @Transactional
    @Override
    public Pair<List<String>, List<String>> delBatteryBySnList(Integer tenantId, List<String> batterySnList, Integer violentDel) {
        if (ObjectUtils.isEmpty(tenantId) || CollectionUtils.isEmpty(batterySnList)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 查询租户信息
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (ObjectUtils.isEmpty(tenant)) {
            throw new BizException("ELECTRICITY.00101", "找不到租户");
        }
        
        // 优先去重
        List<String> batterySnDistinctList = batterySnList.stream().distinct().collect(Collectors.toList());
        
        // 根据参数获取电池数据
        List<ElectricityBattery> dbBatteryList = electricityBatteryMapper.selectListBySnList(tenantId, batterySnDistinctList, null);
        if (CollectionUtils.isEmpty(dbBatteryList)) {
            log.warn("delBatteryBySnList failed. The dbBatteryList is empty.");
            return Pair.of(null, batterySnDistinctList);
        }
        
        // 获取 DB 数据的 SN 集
        List<String> dbBatterySnList = dbBatteryList.stream().map(ElectricityBattery::getSn).collect(Collectors.toList());
        // 定义失败的 SN 集
        List<String> batterySnFailList = new ArrayList<>();
        // 定义等待删除的电池
        List<ElectricityBattery> batteryWaitList = new ArrayList<>();
        
        // 比对入参和查询结果是否一致
        if (batterySnDistinctList.size() != dbBatterySnList.size()) {
            // 若不一致，差集比对，记录下来，以便返回
            List<String> batterySnDiffList = batterySnDistinctList.stream().filter(batterySn -> !dbBatterySnList.contains(batterySn)).collect(Collectors.toList());
            batterySnFailList.addAll(batterySnDiffList);
        }
        
        // 暴力删除
        if (ObjectUtils.isNotEmpty(violentDel) && 1 == violentDel) {
            batteryWaitList.addAll(dbBatteryList);
        } else {
            // 根据加盟商 ID 对 DB 数据进行分组
            Map<Long, List<ElectricityBattery>> dbBatteryFranchiseeIdGroupMap = dbBatteryList.stream().collect(Collectors.groupingBy(ElectricityBattery::getFranchiseeId));
            dbBatteryFranchiseeIdGroupMap.forEach((k, v) -> {
                // 校验加盟商是否正在进行资产盘点
                Integer status = assetInventoryService.queryInventoryStatusByFranchiseeId(k, AssetTypeEnum.ASSET_TYPE_BATTERY.getCode());
                if (Objects.equals(status, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                    List<String> stocktakingSnList = v.stream().map(ElectricityBattery::getSn).collect(Collectors.toList());
                    batterySnFailList.addAll(stocktakingSnList);
                } else {
                    batteryWaitList.addAll(v);
                }
            });
            
            // 根据 UID 是否为空分组
            Map<Boolean, List<ElectricityBattery>> rentGroupBatteryList = batteryWaitList.stream()
                    .collect(Collectors.groupingBy(batteryWait -> batteryWait.getUid() != null, Collectors.mapping(Function.identity(), Collectors.toList())));
            rentGroupBatteryList.forEach((k, v) -> {
                // 电池租用中的电池，不允许删除
                if (Boolean.TRUE.equals(k)) {
                    List<String> rentSnList = v.stream().map(ElectricityBattery::getSn).collect(Collectors.toList());
                    batterySnFailList.addAll(rentSnList);
                    
                    // 从待删除的里面删除这条数据
                    batteryWaitList.removeIf(batteryWait -> rentSnList.contains(batteryWait.getSn()));
                } else {
                    batteryWaitList.addAll(v);
                }
            });
        }
        
        // 对等待删除的数据，进行删除
        List<String> batteryWaitSnList = batteryWaitList.stream().map(ElectricityBattery::getSn).distinct().collect(Collectors.toList());
        
        // 1、调用 BMS 删除
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());
        
        BatteryBatchOperateQuery batteryBatchOperateQuery = new BatteryBatchOperateQuery();
        batteryBatchOperateQuery.setJsonBatterySnList(JsonUtil.toJson(batteryWaitSnList));
        
        R result = batteryPlatRetrofitService.batchDelete(headers, batteryBatchOperateQuery);
        if (!result.isSuccess()) {
            log.error("delBatteryBySnList failed. batteryPlatRetrofitService.batchDelete failed. msg is {}", result.getErrMsg());
            return Pair.of(null, batterySnFailList);
        }
        
        // 2. 使用待删除的数据，删除电池以及电池配置
        adminSupperTxService.delBatteryBySnList(tenantId, batteryWaitSnList);
        
        // 3. 删除缓存
        batteryWaitSnList.forEach(dbBatterySn -> {
            redisService.delete(CacheConstant.CACHE_BT_ATTR + dbBatterySn);
        });
        
        return Pair.of(batteryWaitSnList, batterySnFailList);
    }
    
    @Override
    public void grantPermission(UserGrantSourceReq userGrantSourceReq) {
        if (!SecurityUtils.isAdmin()) {
            log.error("Illegal call, current user is not a super administrator.");
            return;
        }

        // 判断当前资源是否存在
        List<PermissionResource> permissionResourceList = permissionResourceService.listByIdList(userGrantSourceReq.getSourceIds());
        if (CollectionUtils.isEmpty(permissionResourceList)) {
            log.error("Illegal call, current source is not exist.sourceIdList={}", permissionResourceList);
            return;
        }

        Map<Long, Long> sourcePermissionMap = permissionResourceList.stream().filter(item -> !Objects.isNull(item.getParent())).collect(Collectors.toMap(PermissionResource::getId, PermissionResource::getParent, (k1, k2) -> k1));

        asyncTransaction.runAsyncTransactional(grant -> {
            List<Integer> types = grant.getType();
            List<Long> sourceIds = grant.getSourceIds();
            List<Integer> tenantIds = grant.getTenantIds();
            Set<GrantRolePermission> rolePermissions = new HashSet<>();
            
            //根据权限类型和租户查询对应租户的所有角色信息
            List<Long> roleIds = roleMapper.selectIdsByNamesAndTenantIds(null, tenantIds);
            if (CollectionUtils.isEmpty(roleIds)) {
                log.info("grantPermission failed. roleIds is empty.");
                return null;
            }
            
            //根据角色id查询对应的权限id
            List<GrantRolePermission> checkRoleIds = rolePermissionMapper.selectRepeatGrant(roleIds);
            Map<Long, List<GrantRolePermission>> collected = null;
            //根据角色id对权限分组
            if (!CollectionUtils.isEmpty(checkRoleIds)){
                collected = checkRoleIds.stream().collect(Collectors.groupingBy(GrantRolePermission::getRoleId));
            }

            for (Long checkRoleId : roleIds) {
                Set<Long> copySourceIds = new HashSet<>(sourceIds);
                //不为空说明该角色已绑定过部分权限
                if (!Objects.isNull(collected)) {
                    //构建资源重复比对的数据
                    List<GrantRolePermission> roleBOS = collected.getOrDefault(checkRoleId, new ArrayList<>());
                    Set<Long> collect = roleBOS.stream().map(GrantRolePermission::getPId).collect(Collectors.toSet());
                    //如果存在则取交集,并在资源中移除重叠的部分
                    if (collect.retainAll(copySourceIds)) {
                        copySourceIds.removeAll(collect);
                    }
                    //移除完重叠,资源为空说明该权限已存在
                    if (CollectionUtils.isEmpty(copySourceIds)) {
                        continue;
                    }
                }

                // 封装当前角色对应的权限id的集合
                List<GrantRolePermission> roleBOS = collected.getOrDefault(checkRoleId, new ArrayList<>());
                List<Long> curRolePermissionIds = new ArrayList<>();
                if (ObjectUtils.isNotEmpty(roleBOS)) {
                    curRolePermissionIds = roleBOS.stream().map(GrantRolePermission::getPId).collect(Collectors.toList());
                }

                //未进入上层if,则为空说明所有权限都未被添加过，该角色无任何权限，添加资源中的所有
                //批量插入数据构建
                List<Long> finalCurRolePermissionIds = curRolePermissionIds;
                // 过滤掉权限对应的父节点在当前角色没有选中的数据
                List<GrantRolePermission> batchInsert = copySourceIds.stream().filter(id -> sourcePermissionMap.containsKey(id)
                        && finalCurRolePermissionIds.contains(sourcePermissionMap.get(id))).map(id -> {
                    GrantRolePermission rolePermission = new GrantRolePermission();
                    rolePermission.setRoleId(checkRoleId);
                    rolePermission.setPId(id);
                    return rolePermission;
                }).collect(Collectors.toList());

                if (ObjectUtils.isNotEmpty(batchInsert)) {
                    rolePermissions.addAll(batchInsert);
                }

            }

            if (CollectionUtils.isEmpty(rolePermissions)){
                log.info("grantPermission failed. rolePermissions is empty.");
                return null;
            }

            rolePermissionMapper.batchInsert(new ArrayList<>(rolePermissions));

            Set<Long> ids = rolePermissions.stream().map(GrantRolePermission::getRoleId).collect(Collectors.toSet());
            log.info("Grant Permission success. grant permission is : {}", rolePermissions);

            return ids;
        }, serviceWrapper, userGrantSourceReq, (ids) -> {
            if (CollectionUtils.isEmpty(ids)) {
                return;
            }
            //事务提交后删除对应缓存
            for (Long id : ids) {
                redisService.delete(CacheConstant.CACHE_ROLE_PERMISSION_RELATION + id);
            }
        });
    }
}

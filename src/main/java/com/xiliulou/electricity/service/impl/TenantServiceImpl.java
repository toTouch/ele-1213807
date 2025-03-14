package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.ElectricityCabinetCardInfoBO;
import com.xiliulou.electricity.bo.OperateDataAnalyzeBO;
import com.xiliulou.electricity.constant.*;
import com.xiliulou.electricity.dto.CabinetCardDTO;
import com.xiliulou.electricity.dto.EleCabinetIotCardPoolInfoDTO;
import com.xiliulou.electricity.dto.ElectricityCabinetCardInfoDTO;
import com.xiliulou.electricity.dto.OperateDataAnalyzeDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.TenantMapper;
import com.xiliulou.electricity.mapper.asset.AssetWarehouseMapper;
import com.xiliulou.electricity.query.TenantAddAndUpdateQuery;
import com.xiliulou.electricity.query.TenantQuery;
import com.xiliulou.electricity.query.TrafficExportQuery;
import com.xiliulou.electricity.query.asset.AssetWarehouseSaveOrUpdateQueryModel;
import com.xiliulou.electricity.request.InitTenantSubscriptRequest;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.faq.FaqCategoryV2Service;
import com.xiliulou.electricity.service.retrofit.MsgCenterRetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetCardInfoVO;
import com.xiliulou.electricity.vo.OperateDataAnalyzeExcelVO;
import com.xiliulou.electricity.vo.TenantVO;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Resource
    private OperateDataAnalyzeService operateDataAnalyzeService;

    @Resource
    private ElectricityCabinetService electricityCabinetService;


    @Qualifier("restTemplateServiceImpl")
    @Autowired
    RestTemplateService restTemplateService;

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
        List<TenantNote> tenantNoteList = noteService.listByTenantIdList(tenantIdList);
        Map<Integer, Long> tenantNoteNumMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(tenantNoteList)) {
            tenantNoteNumMap = tenantNoteList.stream().collect(Collectors.toMap(TenantNote::getTenantId, TenantNote::getNoteNum, (oldValue, newValue) -> newValue));
        }
        Map<Integer, Long> finalTenantNoteNumMap = tenantNoteNumMap;
        tenantVOS.parallelStream().peek(item -> {
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

    @Override
    public void dataAnalyze(String passWord, HttpServletResponse response) {
        String redisPassWord = redisService.get(CacheConstant.ADMIN_DATA_ANALYZE_PASSWORD_KEY);
        if (StrUtil.isEmpty(redisPassWord) || !Objects.equals(redisPassWord, passWord)) {
            throw new BizException("0001", "密码错误");
        }
        // 每次获取最新一批的数据
        String batch = operateDataAnalyzeService.queryLatestBatch();

        List<OperateDataAnalyzeBO> list = operateDataAnalyzeService.queryList(batch);
        if (CollUtil.isEmpty(list)) {
            return;
        }

        List<OperateDataAnalyzeDTO> dataAnalyzeDTOS = list.parallelStream().map(item -> {
            OperateDataAnalyzeDTO dto = BeanUtil.copyProperties(item, OperateDataAnalyzeDTO.class);
            dto.setCabinetUseRate(item.getCabinetUseRate() + "%");
            dto.setCellUseRate(item.getCellUseRate() + "%");
            dto.setBatteryRentRate(item.getBatteryRentRate() + "%");
            dto.setTotalBuyRate(item.getTotalBuyRate() + "%");
            dto.setTotalChurnRate(item.getTotalChurnRate() + "%");
            dto.setLastWeekAddBuyRate(item.getLastWeekAddBuyRate() + "%");
            dto.setLastWeekRepurchaseRate(item.getLastWeekRepurchaseRate() + "%");
            dto.setLoyalUserRate(item.getLoyalUserRate() + "%");
            dto.setPeopleCellRate(item.getPeopleCellRate() + "%");
            return dto;
        }).collect(Collectors.toList());

        String fileName = "运营数据统计分析.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            EasyExcel.write(outputStream, OperateDataAnalyzeExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                    .doWrite(dataAnalyzeDTOS);
        } catch (IOException e) {
            log.error("运营数据统计分析！", e);
        }
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


    @Override
    public void trafficExport(TrafficExportQuery query, HttpServletResponse response) {
        if (Objects.equals(query.getSelectTab(), TrafficExportQuery.ALONE_TENANT) && Objects.isNull(query.getTenantId())) {
            throw new BizException("选择单租户，必须选择一个租户");
        }
        List<Tenant> tenantList = new ArrayList<>();
        if (Objects.equals(query.getSelectTab(), TrafficExportQuery.ALONE_TENANT)) {
            tenantList.add(tenantMapper.selectById(query.getTenantId()));
        } else {
            tenantList = tenantMapper.selectListAll();
        }

        if (CollUtil.isEmpty(tenantList)) {
            return;
        }
        List<Integer> tenantIdList = tenantList.stream().map(Tenant::getId).collect(Collectors.toList());


        // 查询所有的柜机信息
        List<ElectricityCabinetCardInfoBO> boList = electricityCabinetService.queryEleCardInfoByTenant(tenantIdList);
        if (CollUtil.isEmpty(boList)){
            return;
        }

        // 获取柜机流量卡模式
        Map<String, Double> codeModeMap = this.getCodeMode();

        // 50一批请求获取流量信息
        Map<String, CabinetCardDTO> cabinetCardMap = new HashMap<>();
        int batchSize = 50;
        IntStream.range(0, (boList.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> boList.subList(i * batchSize, Math.min(boList.size(), (i + 1) * batchSize)))
                .forEach(subList -> {
                    this.processSubList(subList, codeModeMap, cabinetCardMap);
                });


        List<ElectricityCabinetCardInfoDTO> result = boList.stream().map(item -> {
            ElectricityCabinetCardInfoDTO dto = BeanUtil.copyProperties(item, ElectricityCabinetCardInfoDTO.class);
            CabinetCardDTO cabinetCardDTO = cabinetCardMap.get(item.getCardNumber());
            if (Objects.nonNull(cabinetCardDTO)) {
                dto.setDataBalance(cabinetCardDTO.getDataBalance());
                dto.setDataTrafficAmount(cabinetCardDTO.getDataTrafficAmount());
                dto.setCode(cabinetCardDTO.getCode());
                dto.setExpiryDate(cabinetCardDTO.getExpiryDate());
                dto.setCodeType(Objects.equals(cabinetCardDTO.getCodeType(), NumberConstant.ONE) ? "共享流量" : "单卡");
            }
            return dto;
        }).collect(Collectors.toList());


        String fileName = "柜机物联网卡流量.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            EasyExcel.write(outputStream, ElectricityCabinetCardInfoVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                    .doWrite(result);
        } catch (IOException e) {
            log.error("柜机物联网卡流量！", e);
        }
    }

    private void processSubList(List<ElectricityCabinetCardInfoBO> subList,Map<String, Double> codeModeMap,Map<String, CabinetCardDTO> cabinetCardMap) {
        if (CollUtil.isEmpty(subList)) {
            return;
        }
        List<String> cardNumberList = subList.stream().map(ElectricityCabinetCardInfoBO::getCardNumber).collect(Collectors.toList());
        JSONObject json = new JSONObject();
        json.put("iccids", cardNumberList);
        String endPoint = CardInfoConstant.url + CardInfoConstant.apiKey + CardInfoConstant.cardInfoPath + "?_sign=" + getMD5(json.toJSONString());

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        String result = restTemplateService.postForStringWithJson(endPoint, json.toJSONString(), headerMap);
        if (StrUtil.isEmpty(result)) {
            throw new BizException("获取卡池信息为空！");
        }

        JSONUtil.parseObj(result).getJSONArray("data").forEach(e -> {
            Map<String, Object> map = (Map<String, Object>) e;
            CabinetCardDTO dto = CabinetCardDTO.builder().code(map.get("code").toString())
                    .dataBalance(map.get("data_balance").toString())
                    .dataTrafficAmount(map.get("data_traffic_amount").toString())
                    .expiryDate(map.get("expiry_date").toString())
                    .codeType(codeModeMap.get(map.get("code").toString()).intValue())
                    .build();
            cabinetCardMap.put(map.get("iccid").toString(), dto);
        });
    }



    public static String getMD5(String str) {
        String base = str + CardInfoConstant.apiSecret;
        String md5 = DigestUtils.md5Hex(base.getBytes());
        return md5;
    }


    private Map<String, Double> getCodeMode() {
        Map<String, Double> billingModes = new HashMap<>();
        String params = StringUtils.EMPTY;
        String endPoint = CardInfoConstant.url + CardInfoConstant.apiKey + CardInfoConstant.billGroupPath + "?_sign=" + getMD5(params);
        System.out.println(endPoint);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");

        String result = restTemplateService.getForString(endPoint, null, headerMap);

        if (StrUtil.isEmpty(result)) {
            throw new BizException("获取卡池信息为空！");
        }

        EleCabinetIotCardPoolInfoDTO eleCabinetIotCardPoolInfoDTO = JsonUtil.fromJson(result, EleCabinetIotCardPoolInfoDTO.class);
        if (!Objects.equals(eleCabinetIotCardPoolInfoDTO.getCode(), 200)) {
            throw new BizException("获取卡池信息失败！");
        }
        EleCabinetIotCardPoolInfoDTO.CardPoolInfoDTO cardPoolInfoDTO = eleCabinetIotCardPoolInfoDTO.getData();
        cardPoolInfoDTO.getRows().forEach(map -> {
            if (Objects.isNull(map.get("bg_code"))) {
                return;
            }
            billingModes.put(String.valueOf(map.get("bg_code")), (Double) map.get("billing_mode"));
        });
        return billingModes;
    }

}

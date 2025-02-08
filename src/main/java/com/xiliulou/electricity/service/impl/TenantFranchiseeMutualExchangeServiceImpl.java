package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.ExportMutualBatteryBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.TenantFranchiseeMutualExchangeMapper;
import com.xiliulou.electricity.query.MutualExchangePageQuery;
import com.xiliulou.electricity.query.MutualExchangeUpdateQuery;
import com.xiliulou.electricity.request.MutualExchangeAddConfigRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.TenantFranchiseeMutualExchangeService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.BaseDataUtil;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ExportMutualBatteryVO;
import com.xiliulou.electricity.vo.MutualElectricityBatteryExcelVO;
import com.xiliulou.electricity.vo.MutualExchangeDetailVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName: TenantFranchiseeMutualExchangeServiceImpl
 * @description: 互通servie
 * @author: renhang
 * @create: 2024-11-27 15:25
 */
@Service
@Slf4j
@SuppressWarnings("all")
public class TenantFranchiseeMutualExchangeServiceImpl implements TenantFranchiseeMutualExchangeService {

    @Resource
    private TenantFranchiseeMutualExchangeMapper mutualExchangeMapper;

    @Resource
    RedisService redisService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private OperateRecordUtil operateRecordUtil;

    @Resource
    private ElectricityBatteryService electricityBatteryService;

    @Resource
    private ElectricityConfigService electricityConfigService;

    @Resource
    private AssertPermissionService assertPermissionService;


    public static final Integer MAX_MUTUAL_EXCHANGE_CONFIG_COUNT = 5;

    public static final Integer MAX_MUTUAL_NAME_LENGTH = 20;


    @Override
    public R addConfig(MutualExchangeAddConfigRequest request) {
        assertPermission();
        Integer tenantId = TenantContextHolder.getTenantId();
        // 校验
        assertMutualRequest(request, tenantId);

        // 校验配置重复
        List<Long> combinedFranchisee = request.getCombinedFranchisee();
        List<TenantFranchiseeMutualExchange> mutualExchangeList = assertMutualExchangeConfig(null, tenantId, combinedFranchisee);
        if (mutualExchangeList.size() >= MAX_MUTUAL_EXCHANGE_CONFIG_COUNT) {
            return R.fail("402002", "互换加盟商配置最大支持添加5条，请调整配置");
        }

        TenantFranchiseeMutualExchange mutualExchange = TenantFranchiseeMutualExchange.builder().combinedName(request.getCombinedName()).tenantId(tenantId)
                .combinedFranchisee(JsonUtil.toJson(combinedFranchisee)).status(request.getStatus()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .build();

        Map<Object, Object> newMap = MapUtil.builder().put("name", request.getCombinedName()).put("combinedFranchiseeNameList", buildFranchiseeNameByIdList(combinedFranchisee))
                .put("status", getStatusDesc(request.getStatus())).build();

        // 新增
        saveMutualExchange(mutualExchange);
        operateRecordUtil.record(null, newMap);
        return R.ok();
    }


    @Override
    public R editConfig(MutualExchangeAddConfigRequest request) {
        assertPermission();
        // 编辑
        TenantFranchiseeMutualExchange oldMutualExchange = mutualExchangeMapper.selectOneById(request.getId());
        if (Objects.isNull(oldMutualExchange)) {
            return R.fail("402003", "不存在的互换配置");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        // 校验
        assertMutualRequest(request, tenantId);

        List<Long> combinedFranchisee = request.getCombinedFranchisee();
        // 判断是否配置存在
        assertMutualExchangeConfig(request.getId(), tenantId, combinedFranchisee);

        TenantFranchiseeMutualExchange mutualExchange = TenantFranchiseeMutualExchange.builder().combinedName(request.getCombinedName()).tenantId(tenantId)
                .combinedFranchisee(JsonUtil.toJson(combinedFranchisee)).status(request.getStatus()).updateTime(System.currentTimeMillis()).build();
        mutualExchange.setId(request.getId());
        updateMutualExchange(mutualExchange);

        Map<Object, Object> oldMap = MapUtil.builder().put("name", oldMutualExchange.getCombinedName())
                .put("combinedFranchiseeNameList", buildFranchiseeNameByIdList(JsonUtil.fromJsonArray(oldMutualExchange.getCombinedFranchisee(), Long.class)))
                .put("status", getStatusDesc(oldMutualExchange.getStatus())).build();

        Map<Object, Object> newMap = MapUtil.builder().put("name", request.getCombinedName()).put("combinedFranchiseeNameList", buildFranchiseeNameByIdList(combinedFranchisee))
                .put("status", getStatusDesc(request.getStatus())).build();
        operateRecordUtil.record(oldMap, newMap);

        return R.ok();
    }


    @Override
    @Slave
    public MutualExchangeDetailVO queryMutualExchangeDetailById(Long id) {
        TenantFranchiseeMutualExchange mutualExchange = mutualExchangeMapper.selectOneById(id);
        if (Objects.isNull(mutualExchange)) {
            return null;
        }
        MutualExchangeDetailVO vo = BeanUtil.copyProperties(mutualExchange, MutualExchangeDetailVO.class);

        // 加盟商转换
        vo.setCombinedFranchiseeList(buildMutualExchangeDetailItemList(mutualExchange.getCombinedFranchisee()));
        return vo;
    }


    @Override
    public List<TenantFranchiseeMutualExchange> listMutualExchangeConfigListFromDB(Integer tenantId) {
        return mutualExchangeMapper.selectMutualExchangeConfigListFromDB(tenantId);
    }


    /**
     * 为避免redis的大key，这里只是将组合加盟商字段放在缓存中
     *
     * @param tenantId tenantId tenantId
     * @return List<String>
     */
    @Override
    public List<String> queryMutualFranchiseeExchangeCache(Integer tenantId) {
        if (Objects.isNull(tenantId)) {
            return CollUtil.newArrayList();
        }
        String mutualExchangesFromCache = redisService.get(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantId);
        if (StrUtil.isNotEmpty(mutualExchangesFromCache)) {
            return JsonUtil.fromJsonArray(mutualExchangesFromCache, String.class);
        }

        List<TenantFranchiseeMutualExchange> mutualExchangeList = listMutualExchangeConfigListFromDB(tenantId);
        if (CollUtil.isEmpty(mutualExchangeList)) {
            return CollUtil.newArrayList();
        }
        // 只需要加盟的互通
        List<String> combinedFranchiseeList = mutualExchangeList.stream().map(TenantFranchiseeMutualExchange::getCombinedFranchisee).collect(Collectors.toList());

        redisService.set(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantId, JsonUtil.toJson(combinedFranchiseeList));
        return combinedFranchiseeList;
    }

    @Override
    public void saveMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange) {
        mutualExchangeMapper.insert(tenantFranchiseeMutualExchange);
        redisService.delete(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantFranchiseeMutualExchange.getTenantId());
    }


    @Override
    public void updateMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange) {
        mutualExchangeMapper.updateMutualExchangeById(tenantFranchiseeMutualExchange);
        redisService.delete(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantFranchiseeMutualExchange.getTenantId());
    }


    @Slave
    @Override
    public List<MutualExchangeDetailVO> pageList(MutualExchangePageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        List<TenantFranchiseeMutualExchange> mutualExchangeList = mutualExchangeMapper.selectPageList(query);
        if (CollUtil.isEmpty(mutualExchangeList)) {
            return CollUtil.newArrayList();
        }

        // 运营商
        if ((SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return mutualExchangeList.stream().map(e -> {
                MutualExchangeDetailVO vo = BeanUtil.copyProperties(e, MutualExchangeDetailVO.class);
                vo.setCombinedFranchiseeList(buildMutualExchangeDetailItemList(e.getCombinedFranchisee()));
                return vo;
            }).collect(Collectors.toList());
        } else {
            //  加盟商
            Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
            if (!pair.getLeft()) {
                return CollUtil.newArrayList();
            }
            return mutualExchangeList.stream().map(e -> {
                List<Long> combinedFranchisee = JsonUtil.fromJsonArray(e.getCombinedFranchisee(), Long.class);
                if (!combinedFranchisee.containsAll(pair.getRight())) {
                    return null;
                }
                MutualExchangeDetailVO vo = BeanUtil.copyProperties(e, MutualExchangeDetailVO.class);
                vo.setCombinedFranchiseeList(buildMutualExchangeDetailItemList(e.getCombinedFranchisee()));
                return vo;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    @Slave
    @Override
    public Long pageCount(MutualExchangePageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        if ((SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return mutualExchangeMapper.countTotal(query);
        } else {
            query.setOffset(0L);
            query.setSize(1000L);
            // 运营商单独处理
            List<TenantFranchiseeMutualExchange> mutualExchangeList = mutualExchangeMapper.selectPageList(query);
            if (CollUtil.isEmpty(mutualExchangeList)) {
                return NumberConstant.ZERO_L;
            }

            Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
            if (!pair.getLeft()) {
                return NumberConstant.ZERO_L;
            }

            return mutualExchangeList.stream().map(e -> {
                List<Long> combinedFranchisee = JsonUtil.fromJsonArray(e.getCombinedFranchisee(), Long.class);
                if (!combinedFranchisee.containsAll(pair.getRight())) {
                    return null;
                }
                MutualExchangeDetailVO vo = BeanUtil.copyProperties(e, MutualExchangeDetailVO.class);
                vo.setCombinedFranchiseeList(buildMutualExchangeDetailItemList(e.getCombinedFranchisee()));
                return vo;
            }).filter(Objects::nonNull).count();
        }
    }


    @Override
    public R deleteById(Long id) {
        assertPermission();
        TenantFranchiseeMutualExchange mutualExchange = mutualExchangeMapper.selectOneById(id);
        if (Objects.isNull(mutualExchange)) {
            return R.fail("402003", "不存在的互换配置");
        }
        this.updateMutualExchange(
                TenantFranchiseeMutualExchange.builder().id(id).tenantId(TenantContextHolder.getTenantId()).updateTime(System.currentTimeMillis()).delFlag(1).build());

        Map<Object, Object> newMap = MapUtil.builder().put("name", mutualExchange.getCombinedName())
                .put("combinedFranchiseeNameList", buildFranchiseeNameByIdList(JsonUtil.fromJsonArray(mutualExchange.getCombinedFranchisee(), Long.class)))
                .put("status", getStatusDesc(mutualExchange.getStatus())).build();
        operateRecordUtil.record(null, newMap);
        return R.ok();
    }


    @Override
    public R updateStatus(MutualExchangeUpdateQuery query) {
        assertPermission();
        TenantFranchiseeMutualExchange mutualExchange = mutualExchangeMapper.selectOneById(query.getId());
        if (Objects.isNull(mutualExchange)) {
            return R.fail("402003", "不存在的互换配置");
        }
        this.updateMutualExchange(TenantFranchiseeMutualExchange.builder().id(query.getId()).tenantId(TenantContextHolder.getTenantId()).updateTime(System.currentTimeMillis())
                .status(query.getStatus()).build());
        return R.ok();
    }


    @Override
    public Pair<Boolean, Set<Long>> satisfyMutualExchangeFranchisee(Integer tenantId, Long franchiseeId) {
        try {
            if (Objects.isNull(tenantId) || Objects.isNull(franchiseeId)) {
                log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! tenantId or franchiseeId is null");
                return Pair.of(false, null);
            }

            // 查询互通配置开关
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            if (Objects.isNull(electricityConfig) || Objects.isNull(electricityConfig.getIsSwapExchange()) || Objects.equals(electricityConfig.getIsSwapExchange(), ElectricityConfig.SWAP_EXCHANGE_CLOSE)) {
                log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! electricityConfig is null or SwapExchange is close, tenantId is {}， electricityConfig is {}", tenantId,
                        Objects.isNull(electricityConfig) ? "null" : JsonUtil.toJson(electricityConfig));
                return Pair.of(false, null);
            }

            // 查询互通配置列表
            List<String> mutualFranchiseeList = queryMutualFranchiseeExchangeCache(tenantId);
            if (CollUtil.isEmpty(mutualFranchiseeList)) {
                log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! Current Tenant mutualFranchiseeList is null, tenantId is {}", tenantId);
                return Pair.of(false, null);
            }

            Set<Long> mutualFranchiseeSet = new HashSet<>();
            mutualFranchiseeList.forEach(e -> {
                List<Long> combinedFranchisee = JsonUtil.fromJsonArray(e, Long.class);
                if (combinedFranchisee.contains(franchiseeId)) {
                    mutualFranchiseeSet.addAll(combinedFranchisee);
                }
            });
            // 有互通配置，但是当前加盟商并没有在互通中
            if (CollUtil.isEmpty(mutualFranchiseeSet)) {
                log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! ExistMutualConfig, but notContainConfig, tenantId is {}, franchiseeId is {}", tenantId, franchiseeId);
                return Pair.of(false, null);
            }

            log.info("IsSatisfyFranchiseeIdMutualExchange Info! Current Franchisee SatisfyMutualExchange, tenantId is {}, franchiseeId is {}, MutualFranchiseeSet is {}", tenantId,
                    franchiseeId, JsonUtil.toJson(mutualFranchiseeSet));
            return Pair.of(true, mutualFranchiseeSet);
        } catch (Exception e) {
            log.error("IsSatisfyFranchiseeIdMutualExchange Error!", e);
        }
        return Pair.of(false, null);
    }


    @Override
    public Boolean isSatisfyFranchiseeMutualExchange(Integer tenantId, Long franchiseeId, Long otherFranchiseeId) {
        Pair<Boolean, Set<Long>> pair = satisfyMutualExchangeFranchisee(tenantId, franchiseeId);
        if (pair.getLeft()) {
            // 判断加盟商互通是否包含另一加盟商
            return pair.getRight().contains(otherFranchiseeId);
        }
        if (Objects.isNull(otherFranchiseeId)) {
            return false;
        }
        // 不符合互通配置,需要判断两个加盟商是否相等
        return Objects.equals(franchiseeId, otherFranchiseeId);
    }

    @Override
    public Boolean batteryReportIsSatisfyFranchiseeMutualExchange(Integer tenantId, Long franchiseeId, Long otherFranchiseeId, String sessionId) {
        log.info("batteryReportIsSatisfyFranchiseeMutualExchange Info! sessionId is {}", sessionId);
        Pair<Boolean, Set<Long>> pair = satisfyMutualExchangeFranchisee(tenantId, franchiseeId);
        if (pair.getLeft()) {
            // 判断加盟商互通是否包含另一加盟商
            return pair.getRight().contains(otherFranchiseeId);
        }
        if (Objects.isNull(otherFranchiseeId)) {
            return false;
        }
        // 不符合互通配置,需要判断两个加盟商是否相等
        return Objects.equals(franchiseeId, otherFranchiseeId);
    }

    @Override
    public Triple<Boolean, String, Object> orderExchangeMutualFranchiseeCheck(Integer tenantId, Long franchiseeId, Long otherFranchiseeId) {
        Pair<Boolean, Set<Long>> mutualExchangeFranchiseePair = satisfyMutualExchangeFranchisee(tenantId, franchiseeId);
        if (mutualExchangeFranchiseePair.getLeft()) {
            if (mutualExchangeFranchiseePair.getRight().contains(otherFranchiseeId)) {
                // 存在互通加盟商
                return Triple.of(true, null, mutualExchangeFranchiseePair.getRight());
            } else {
                log.warn("ORDER WARN! Mutual Check, user fId is not equal franchiseeId,tenantId is {}, uidF is {}, eidF is {}", tenantId, franchiseeId, otherFranchiseeId);
                return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
            }
        } else {
            if (Objects.nonNull(otherFranchiseeId) && Objects.equals(franchiseeId, otherFranchiseeId)) {
                Set<Long> set = CollUtil.newHashSet();
                set.add(franchiseeId);
                return Triple.of(true, null, set);
            } else {
                log.warn("ORDER WARN! Normal Check ,user fId is not equal franchiseeId,tenantId is {}, uidF is {}, eidF is {}", tenantId, franchiseeId, otherFranchiseeId);
                return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
            }
        }
    }

    @Override
    public void mutualBatteryExport(HttpServletResponse response) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
            if (!pair.getLeft()) {
                throw new BizException("120240", "当前加盟商无权限操作");
            }
            franchiseeIds = pair.getRight();
        }

        List<ExportMutualBatteryBO> mutualBatteryBOList = electricityBatteryService.queryMutualBattery(TenantContextHolder.getTenantId(), franchiseeIds);
        if (Objects.isNull(mutualBatteryBOList)) {
            throw new BizException("402010", "电池数据不存在");
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        // 查询互通配置列表
        List<String> mutualFranchiseeList = queryMutualFranchiseeExchangeCache(tenantId);
        if (CollUtil.isEmpty(mutualFranchiseeList)) {
            log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! Current Tenant mutualFranchiseeList is null, tenantId is {}", tenantId);
            throw new BizException("402007", "不存在互换加盟商配置");
        }

        List<ExportMutualBatteryVO> exportMutualBatteryVOList = mutualBatteryBOList.parallelStream().map(e -> {
            // 只导出互通加盟商的电池
            Set<Long> mutualFranchiseeSet = new HashSet<>();
            mutualFranchiseeList.stream().forEach(t -> {
                List<Long> combinedFranchisee = JsonUtil.fromJsonArray(t, Long.class);
                if (combinedFranchisee.contains(e.getFranchiseeId())) {
                    mutualFranchiseeSet.addAll(combinedFranchisee);
                }
            });
            // 存在互通
            if (CollUtil.isNotEmpty(mutualFranchiseeSet)) {
                ExportMutualBatteryVO excelVO = new ExportMutualBatteryVO();
                BeanUtil.copyProperties(e, excelVO);
                if (Objects.equals(e.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)) {
                    // 在自己柜机内的不展示
                    if (Objects.equals(e.getFranchiseeId(), e.getEFranchiseeId())) {
                        return null;
                    }
                    // 如果在仓，并且柜机和电池加盟商不互通情况 不导出
                    if(!mutualFranchiseeSet.contains(e.getEFranchiseeId())){
                        return null;
                    }
                    excelVO.setPhysicsStatus("在仓");
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getEFranchiseeId());
                    excelVO.setMutualFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : null);
                } else {
                    // 自己用户拿走的不展示
                    if (Objects.equals(e.getFranchiseeId(), e.getUserFranchiseeId()) || (Objects.equals(e.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_RETURN))) {
                        return null;
                    }
                    excelVO.setPhysicsStatus("不在仓");
                    // 用户加盟商
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getUserFranchiseeId());
                    excelVO.setMutualFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : null);
                }
                return excelVO;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());


        String fileName = "互通电池列表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            EasyExcel.write(outputStream, MutualElectricityBatteryExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                    .doWrite(exportMutualBatteryVOList);
        } catch (IOException e) {
            log.error("导出互通电池列表！", e);
        }
    }

    /**
     * 是否存在互换配置
     *
     * @param franchiseeList    franchiseeList
     * @param oldFranchiseeList list
     * @return Boolean
     */
    private Boolean isExistMutualExchangeConfig(Long id, List<Long> franchiseeList, List<TenantFranchiseeMutualExchange> oldFranchiseeList) {
        // 将所有的配置加盟商遍历，判断当前添加的加盟商是否存在配置
        Map<String, Set<Long>> oldFranchiseeMap = new HashMap<>(20);
        for (TenantFranchiseeMutualExchange exchange : oldFranchiseeList) {
            // 这里要排除掉自己id
            if (Objects.equals(id, exchange.getId())) {
                continue;
            }
            Set<Long> combinedFranchisee = new HashSet<>(JsonUtil.fromJsonArray(exchange.getCombinedFranchisee(), Long.class));
            // 存储所有已存在的数据集合
            BaseDataUtil.buildCombinations(combinedFranchisee, oldFranchiseeMap);
        }

        if (CollUtil.isEmpty(oldFranchiseeMap)) {
            return false;
        }

        // 尝试添加集合2的组合
        if (!BaseDataUtil.canAddCombination(new HashSet<>(franchiseeList), oldFranchiseeMap)) {
            return true;
        }
        return false;
    }

    private List<MutualExchangeDetailVO.Item> buildMutualExchangeDetailItemList(String combinedFranchisee) {
        List<Long> combinedFranchiseeIdList = JsonUtil.fromJsonArray(combinedFranchisee, Long.class);
        return combinedFranchiseeIdList.stream().map(e -> {
            MutualExchangeDetailVO.Item item = new MutualExchangeDetailVO.Item();
            item.setId(e);
            Franchisee franchisee = franchiseeService.queryByIdFromCache(e);
            item.setFranchiseeName(Objects.isNull(franchisee) ? null : franchisee.getName());
            return item;
        }).collect(Collectors.toList());
    }


    private void assertMutualRequest(MutualExchangeAddConfigRequest request, Integer tenantId) {
        if (Objects.isNull(request.getCombinedName())) {
            throw new BizException("402000", "组合名称不能为空");
        }
        if (request.getCombinedName().length() > MAX_MUTUAL_NAME_LENGTH) {
            throw new BizException("402000", "组合名称不能超过20字");
        }
        if (Objects.isNull(request.getStatus())) {
            throw new BizException("402000", "配置状态不能为空");
        }

        List<Long> combinedFranchisee = request.getCombinedFranchisee();
        if (CollUtil.isEmpty(combinedFranchisee)) {
            throw new BizException("402000", "互换配置不能为空");
        }

        // 不同类型加盟商校验
        Triple<Boolean, String, String> triple = assertFranchiseeModelType(combinedFranchisee);
        if (!triple.getLeft()) {
            throw new BizException(triple.getMiddle(), triple.getRight());
        }

    }

    private String getStatusDesc(Integer status) {
        return Objects.equals(status, TenantFranchiseeMutualExchange.STATUS_ENABLE) ? "启用" : "禁用";
    }

    private List<TenantFranchiseeMutualExchange> assertMutualExchangeConfig(Long id, Integer tenantId, List<Long> combinedFranchisee) {
        List<TenantFranchiseeMutualExchange> swapExchangeList = mutualExchangeMapper.selectSwapExchangeList(tenantId);
        if (isExistMutualExchangeConfig(id, combinedFranchisee, swapExchangeList)) {
            throw new BizException("402001", "该互换配置已存在");
        }
        return swapExchangeList;
    }

    private List<String> buildFranchiseeNameByIdList(List<Long> combinedFranchisee) {
        return combinedFranchisee.stream().map(e -> {
            Franchisee franchisee = franchiseeService.queryByIdFromCache(e);
            return Objects.nonNull(franchisee) ? franchisee.getName() : null;
        }).collect(Collectors.toList());
    }


    private Triple<Boolean, String, String> assertFranchiseeModelType(List<Long> combinedFranchisee) {
        Set<String> oldFranchiseeSet = new HashSet<>();
        Set<String> newFranchiseeSet = new HashSet<>();
        combinedFranchisee.forEach(e -> {
            Franchisee franchisee = franchiseeService.queryByIdFromCache(e);
            if (Objects.isNull(franchisee)) {
                log.warn("AssertFranchiseeModelType Warn! franchisee is null, franchiseeId is {}", e);
                throw new BizException("402005", "选择中部分加盟商不存在，请检查");
            }
            if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                newFranchiseeSet.add(franchisee.getName());
            } else {
                oldFranchiseeSet.add(franchisee.getName());
            }
        });

        if (CollUtil.isNotEmpty(oldFranchiseeSet) && CollUtil.isNotEmpty(newFranchiseeSet)) {
            String msg = " %s为标准型号加盟商，%s为多型号加盟商，系统仅支持型号类型相同的加盟商互换，请检查";
            return Triple.of(false, "402004", String.format(msg, String.join(",", oldFranchiseeSet), String.join(",", newFranchiseeSet)));
        }

        return Triple.of(true, null, null);
    }

    private void assertPermission() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            throw new BizException("120240", "当前加盟商无权限操作");
        }
    }
}

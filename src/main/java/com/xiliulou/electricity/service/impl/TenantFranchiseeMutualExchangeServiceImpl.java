package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.ExportMutualBatteryBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.TenantFranchiseeMutualExchange;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.TenantFranchiseeMutualExchangeMapper;
import com.xiliulou.electricity.query.MutualExchangePageQuery;
import com.xiliulou.electricity.query.MutualExchangeUpdateQuery;
import com.xiliulou.electricity.request.MutualExchangeAddConfigRequest;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.TenantFranchiseeMutualExchangeService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.BaseDataUtil;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.ExportMutualBatteryVO;
import com.xiliulou.electricity.vo.MutualElectricityBatteryExcelVO;
import com.xiliulou.electricity.vo.MutualExchangeDetailVO;
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
import java.util.ArrayList;
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
    
    
    public static final Integer MAX_MUTUAL_EXCHANGE_CONFIG_COUNT = 5;
    
    public static final Integer MAX_MUTUAL_NAME_LENGTH = 20;
    
    
    @Override
    public R addConfig(MutualExchangeAddConfigRequest request) {
        assertMutualName(request.getCombinedName(), request.getStatus());
        List<Long> combinedFranchisee = request.getCombinedFranchisee();
        if (CollUtil.isEmpty(combinedFranchisee)) {
            return R.fail("402000", "互换配置不能为空");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
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
        if (StrUtil.isNotBlank(request.getCombinedName())) {
            assertMutualName(request.getCombinedName(), request.getStatus());
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        // 判断是否配置存在
        List<Long> combinedFranchisee = request.getCombinedFranchisee();
        if (CollUtil.isNotEmpty(combinedFranchisee)) {
            assertMutualExchangeConfig(request.getId(), tenantId, combinedFranchisee);
        }
        
        // 编辑
        TenantFranchiseeMutualExchange oldMutualExchange = mutualExchangeMapper.selectOneById(request.getId());
        if (Objects.isNull(oldMutualExchange)) {
            return R.fail("402003", "不存在的互换配置");
        }
        
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
    public MutualExchangeDetailVO getMutualExchangeDetailById(Long id) {
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
    public List<TenantFranchiseeMutualExchange> getMutualExchangeConfigListFromDB(Integer tenantId) {
        return mutualExchangeMapper.selectMutualExchangeConfigListFromDB(tenantId);
    }
    
    
    /**
     * 为避免redis的大key，这里只是将组合加盟商字段放在缓存中
     *
     * @param tenantId tenantId tenantId
     * @return List<String>
     */
    @Override
    public List<String> getMutualFranchiseeExchangeCache(Integer tenantId) {
        if (Objects.isNull(tenantId)) {
            return CollUtil.newArrayList();
        }
        String mutualExchangesFromCache = redisService.get(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantId);
        if (StrUtil.isNotEmpty(mutualExchangesFromCache)) {
            return JsonUtil.fromJsonArray(mutualExchangesFromCache, String.class);
        }
        
        List<TenantFranchiseeMutualExchange> mutualExchangeList = getMutualExchangeConfigListFromDB(tenantId);
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
    
    
    @Override
    public List<MutualExchangeDetailVO> pageList(MutualExchangePageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());
        List<TenantFranchiseeMutualExchange> mutualExchangeList = mutualExchangeMapper.selectPageList(query);
        if (CollUtil.isEmpty(mutualExchangeList)) {
            return CollUtil.newArrayList();
        }
        
        return mutualExchangeList.stream().map(e -> {
            MutualExchangeDetailVO vo = BeanUtil.copyProperties(e, MutualExchangeDetailVO.class);
            vo.setCombinedFranchiseeList(buildMutualExchangeDetailItemList(e.getCombinedFranchisee()));
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Long pageCount(MutualExchangePageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());
        return mutualExchangeMapper.countTotal(query);
    }
    
    
    @Override
    public R deleteById(Long id) {
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
            if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsSwapExchange(), ElectricityConfig.SWAP_EXCHANGE_CLOSE)) {
                log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! electricityConfig is null or SwapExchange is close, tenantId is {}， electricityConfig is {}", tenantId,
                        Objects.isNull(electricityConfig) ? "null" : JsonUtil.toJson(electricityConfig));
                return Pair.of(false, null);
            }
            
            // 查询互通配置列表
            List<String> mutualFranchiseeList = getMutualFranchiseeExchangeCache(tenantId);
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
                return Triple.of(true, null, CollUtil.newHashSet().add(franchiseeId));
            } else {
                log.warn("ORDER WARN! Normal Check ,user fId is not equal franchiseeId,tenantId is {}, uidF is {}, eidF is {}", tenantId, franchiseeId, otherFranchiseeId);
                return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
            }
        }
    }
    
    @Override
    public void mutualBatteryExport(HttpServletResponse response) {
        List<ExportMutualBatteryBO> mutualBatteryBOList = electricityBatteryService.queryMutualBattery(TenantContextHolder.getTenantId());
        if (Objects.isNull(mutualBatteryBOList)) {
            throw new BizException("402010", "电池数据不存在");
        }
        
        List<ExportMutualBatteryVO> exportMutualBatteryVOList = CollUtil.newArrayList();
        for (ExportMutualBatteryBO bo : mutualBatteryBOList) {
            // 只导出互通加盟商的电池
            Pair<Boolean, Set<Long>> pair = satisfyMutualExchangeFranchisee(TenantContextHolder.getTenantId(), bo.getFranchiseeId());
            if (pair.getLeft()) {
                ExportMutualBatteryVO excelVO = new ExportMutualBatteryVO();
                BeanUtil.copyProperties(bo, excelVO);
                excelVO.setPhysicsStatus(Objects.equals(bo.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE) ? "在仓" : "不在仓");
                List<String> franchiseeNameList = buildFranchiseeNameByIdList(new ArrayList<>(pair.getRight()));
                excelVO.setMutualFranchiseeName(String.join(",", franchiseeNameList));
                exportMutualBatteryVOList.add(excelVO);
            }
        }
        
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
    
    
    private void assertMutualName(String combinedName, Integer status) {
        if (Objects.isNull(combinedName)) {
            throw new BizException("402000", "组合名称不能为空");
        }
        if (combinedName.length() > MAX_MUTUAL_NAME_LENGTH) {
            throw new BizException("402000", "组合名称不能超过20字");
        }
        if (Objects.isNull(status)) {
            throw new BizException("402000", "配置状态不能为空");
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
    
}

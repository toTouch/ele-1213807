package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMaterial;
import com.xiliulou.electricity.entity.BatteryModel;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.mapper.BatteryModelMapper;
import com.xiliulou.electricity.query.BatteryModelQuery;
import com.xiliulou.electricity.service.BatteryMaterialService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.BatteryMaterialVO;
import com.xiliulou.electricity.vo.BatteryModelAndMaterialVO;
import com.xiliulou.electricity.vo.BatteryModelPageVO;
import com.xiliulou.electricity.vo.BatteryModelVO;
import com.xiliulou.electricity.vo.BatteryTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 电池型号(BatteryModel)表服务实现类
 *
 * @author zzlong
 * @since 2023-04-11 10:59:51
 */
@Service("batteryModelService")
@Slf4j
public class BatteryModelServiceImpl implements BatteryModelService {
    
    private static final String SEPARATOR = "_";
    
    private static final String SEPARATE = "/";
    
    @Resource
    private BatteryModelMapper batteryModelMapper;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private BatteryMaterialService materialService;
    
    @Autowired
    private BatteryMaterialService batteryMaterialService;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Slave
    @Override
    public BatteryModel queryByIdFromDB(Long id) {
        return this.batteryModelMapper.queryById(id);
    }
    
    @Override
    public List<BatteryModel> queryByTenantIdFromCache(Integer tenantId) {
        List<BatteryModel> cacheBatteryModelList = redisService
                .getWithList(CacheConstant.CACHE_BATTERY_MODEL + tenantId, BatteryModel.class);
        if (CollectionUtils.isNotEmpty(cacheBatteryModelList)) {
            return cacheBatteryModelList;
        }
        
        List<BatteryModel> batteryModelList = this.queryByTenantIdFromDB(tenantId);
        if (CollectionUtils.isEmpty(batteryModelList)) {
            return Collections.emptyList();
        }
        
        redisService.saveWithList(CacheConstant.CACHE_BATTERY_MODEL + tenantId, batteryModelList);
        return batteryModelList;
    }
    
    @Slave
    @Override
    public List<BatteryModel> queryByTenantIdFromDB(Integer tenantId) {
        return this.batteryModelMapper.selectList(
                new LambdaQueryWrapper<BatteryModel>().eq(BatteryModel::getTenantId, tenantId)
                        .eq(BatteryModel::getDelFlag, BatteryModel.DEL_NORMAL).orderByAsc(BatteryModel::getId));
    }
    
    @Slave
    @Override
    public List<BatteryModelPageVO> selectByPage(BatteryModelQuery query) {
        List<BatteryModelPageVO> batteryModels = this.batteryModelMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(batteryModels)) {
            return Collections.emptyList();
        }
        
        List<BatteryMaterial> batteryMaterials = materialService.selectAllFromCache();
        if (CollectionUtils.isEmpty(batteryMaterials)) {
            return Collections.emptyList();
        }
        
        batteryModels.forEach(item -> item.setBatteryType(transformBatteryType(item, batteryMaterials)));
        return batteryModels;
    }
    
    @Slave
    @Override
    public Integer selectByPageCount(BatteryModelQuery query) {
        return this.batteryModelMapper.selectByPageCount(query);
    }
    
    @Override
    public List<BatteryTypeVO> selectBatteryTypeAll() {
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Collections.emptyList();
        }
        
        List<BatteryModel> batteryModels = this.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(batteryModels)) {
            return Collections.emptyList();
        }
        
        List<BatteryMaterial> batteryMaterials = materialService.selectAllFromCache();
        if (CollectionUtils.isEmpty(batteryMaterials)) {
            return Collections.emptyList();
        }
        
        return batteryModels.stream().map(item -> {
            BatteryTypeVO batteryTypeVO = new BatteryTypeVO();
            BeanUtils.copyProperties(item, batteryTypeVO);
            batteryTypeVO.setBatteryTypeName(transformBatteryType(item, batteryMaterials));
            return batteryTypeVO;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取用户自定义电池型号列表
     */
    @Override
    public List<BatteryModel> selectCustomizeBatteryType(BatteryModelQuery query) {
        
        List<BatteryModel> batteryModels = queryByTenantIdFromCache(query.getTenantId());
        if (CollectionUtils.isEmpty(batteryModels)) {
            return Collections.emptyList();
        }
        
        List<BatteryModel> list = batteryModels.stream()
                .filter(item -> Objects.equals(item.getType(), BatteryModel.TYPE_CUSTOMIZE))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        return list;
    }
    
    @Override
    public Triple<Boolean, String, Object> save(BatteryModelQuery batteryModelQuery) {
        List<BatteryModel> batteryModels = queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        Integer maxBatteryModel = batteryModels.stream()
                .sorted(Comparator.comparing(BatteryModel::getBatteryModel).reversed())
                .map(BatteryModel::getBatteryModel).findFirst().orElse(0);
        List<String> batteryTypeList = batteryModels.stream()
                .sorted(Comparator.comparing(BatteryModel::getBatteryModel).reversed())
                .map(BatteryModel::getBatteryType).collect(Collectors.toList());
        
        //电池型号数量
        if (batteryModels.size() >= 50) {
            return Triple.of(false, "100342", "电池型号超出限制，请联系管理员");
        }
        
        BatteryMaterial batteryMaterial = materialService.queryByIdFromDB(batteryModelQuery.getMid());
        if (Objects.isNull(batteryMaterial)) {
            return Triple.of(false, "100346", "电池材质不存在");
        }
        
        if (Objects.isNull(batteryMaterial.getKind()) || StringUtils.isBlank(batteryMaterial.getType()) || StringUtils
                .isBlank(batteryMaterial.getShortType())) {
            return Triple.of(false, "", "电池材质异常");
        }
        
        //生成电池型号
        String batteryType = generateBatteryType(batteryModelQuery, batteryMaterial);
        if (batteryTypeList.contains(batteryType)) {
            return Triple.of(false, "100347", "电池型号已存在");
        }
        
        //生成短电池型号
        String batteryShortType = generateBatteryShortType(batteryModelQuery, batteryMaterial);
        
        BatteryModel batteryModel = new BatteryModel();
        batteryModel.setMid(batteryModelQuery.getMid());
        batteryModel.setBatteryModel(++maxBatteryModel);
        batteryModel.setBatteryType(batteryType);
        batteryModel.setBatteryVShort(batteryShortType);
        batteryModel.setBatteryV(batteryModelQuery.getChargeV());
        batteryModel.setTenantId(TenantContextHolder.getTenantId());
        batteryModel.setType(BatteryModel.TYPE_CUSTOMIZE);
        batteryModel.setDelFlag(BatteryModel.DEL_NORMAL);
        batteryModel.setCreateTime(System.currentTimeMillis());
        batteryModel.setUpdateTime(System.currentTimeMillis());
        this.insert(batteryModel);
        
        redisService.delete(CacheConstant.CACHE_BATTERY_MODEL + TenantContextHolder.getTenantId());
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> modify(BatteryModelQuery batteryModelQuery) {
        BatteryModel batteryModel = new BatteryModel();
        BeanUtils.copyProperties(batteryModelQuery, batteryModel);
        batteryModel.setUpdateTime(System.currentTimeMillis());
        
        this.update(batteryModel);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> delete(Long id) {
        BatteryModel batteryModel = this.queryByIdFromDB(id);
        if (Objects.isNull(batteryModel)) {
            return Triple.of(true, null, null);
        }
        
        if (Objects.equals(batteryModel.getType(), BatteryModel.TYPE_SYSTEM)) {
            return Triple.of(false, "", "系统默认型号不允许删除");
        }
        
        Integer result = franchiseeService
                .checkBatteryModelIsUse(batteryModel.getBatteryModel(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(result)) {
            return Triple.of(false, "", "电池型号已绑定加盟商不允许删除");
        }
        
        this.deleteById(id);
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public Integer checkMidExist(Long mid) {
        return this.batteryModelMapper.checkMidExist(mid);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatteryModel insert(BatteryModel batteryModel) {
        this.batteryModelMapper.insertOne(batteryModel);
        
        redisService.delete(CacheConstant.CACHE_BATTERY_MODEL + TenantContextHolder.getTenantId());
        return batteryModel;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BatteryModel batteryModel) {
        int update = this.batteryModelMapper.update(batteryModel);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_BATTERY_MODEL + TenantContextHolder.getTenantId());
        });
        return update;
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteById(Long id) {
        int delete = this.batteryModelMapper.deleteById(id);
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_BATTERY_MODEL + TenantContextHolder.getTenantId());
        });
        return delete;
    }
    
    @Override
    public Integer batchInsertDefaultBatteryModel(List<BatteryModel> generateDefaultBatteryModel) {
        Integer result = this.batteryModelMapper.batchInsertDefaultBatteryModel(generateDefaultBatteryModel);
        DbUtils.dbOperateSuccessThenHandleCache(result, i -> {
            redisService.delete(CacheConstant.CACHE_BATTERY_MODEL + TenantContextHolder.getTenantId());
        });
        return result;
    }
    
    @Slave
    @Override
    public BatteryModelAndMaterialVO selectBatteryModels(Integer tenantId) {
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            return null;
        }
        
        List<BatteryModel> batteryModels = this.queryByTenantIdFromCache(tenantId);
        if (CollectionUtils.isEmpty(batteryModels)) {
            return null;
        }
        
        List<BatteryModelVO> modelVOS = batteryModels.stream().map(item -> {
            BatteryModelVO batteryModelVO = new BatteryModelVO();
            BeanUtils.copyProperties(item, batteryModelVO);
            return batteryModelVO;
        }).collect(Collectors.toList());
        
        List<BatteryMaterial> batteryMaterials = batteryMaterialService.selectAllFromCache();
        if (CollectionUtils.isEmpty(batteryMaterials)) {
            return null;
        }
        
        List<BatteryMaterialVO> materialVOS = batteryMaterials.stream().map(item -> {
            BatteryMaterialVO batteryMaterialVO = new BatteryMaterialVO();
            BeanUtils.copyProperties(item, batteryMaterialVO);
            return batteryMaterialVO;
        }).collect(Collectors.toList());
        
        BatteryModelAndMaterialVO batteryModelAndMaterialVO = new BatteryModelAndMaterialVO();
        batteryModelAndMaterialVO.setBatteryModels(modelVOS);
        batteryModelAndMaterialVO.setBatteryMaterials(materialVOS);
        return batteryModelAndMaterialVO;
    }
    
    /**
     * 根据batteryModel获取batteryType
     */
    @Override
    public String acquireBatteryShort(Integer batteryModel, Integer tenantId) {
        List<BatteryModel> batteryModels = this.queryByTenantIdFromCache(tenantId);
        if (CollectionUtils.isEmpty(batteryModels)) {
            log.warn("BATTERY MODEL WARN!batteryModels is empty,tenantId={}", tenantId);
            return "";
        }
        
        return batteryModels.stream().collect(
                Collectors.toMap(BatteryModel::getBatteryModel, BatteryModel::getBatteryType, (item1, item2) -> item2))
                .getOrDefault(batteryModel, "");
    }
    
    /**
     * 根据batteryType获取batteryModel
     */
    @Override
    public Integer acquireBatteryModel(String type, Integer tenantId) {
        List<BatteryModel> batteryModels = this.queryByTenantIdFromCache(tenantId);
        if (CollectionUtils.isEmpty(batteryModels)) {
            log.warn("BATTERY MODEL WARN!batteryModels is empty,tenantId={}", tenantId);
            return NumberConstant.ZERO;
        }
        
        return batteryModels.stream().collect(
                Collectors.toMap(BatteryModel::getBatteryType, BatteryModel::getBatteryModel, (item1, item2) -> item2))
                .getOrDefault(type, NumberConstant.ZERO);
    }
    
    @Override
    public String analysisBatteryTypeByBatteryName(String batteryName) {
        String type = "";
        
        try {
            //获取系统定义的电池材质
            List<BatteryMaterial> batteryMaterials = materialService.selectAllFromCache();
            if (CollectionUtils.isEmpty(batteryMaterials)) {
                log.error("ELE ERROR!battery type analysis fail,batteryMaterials is null,batteryName={}", batteryName);
                return type;
            }
            
            if (StringUtils.isBlank(batteryName) || batteryName.length() < 11) {
                log.error("ELE ERROR!battery type analysis fail,batteryName is illegal,batteryName={}", batteryName);
                return type;
            }
            
            StringBuilder modelTypeName = new StringBuilder("B_");
            char[] batteryChars = batteryName.toCharArray();
            
            //获取电压
            String chargeV = split(batteryChars, 4, 6);
            modelTypeName.append(chargeV).append("V").append(SEPARATOR);
            
            //获取材料体系
            char material = batteryChars[2];
            Map<String, String> materialMap = batteryMaterials.stream().collect(Collectors
                    .toMap(item -> String.valueOf(item.getKind()), BatteryMaterial::getType, (item1, item2) -> item2));
            
            //如果电池编码对应的材质不存在，返回空
            String materialName = materialMap.get(String.valueOf(material));
            if (StringUtils.isBlank(materialName)) {
                log.error("ELE ERROR!battery type analysis fail,materialName is blank,batteryName={}", batteryName);
                return type;
            }
            
            modelTypeName.append(materialName).append(SEPARATOR);
            modelTypeName.append(split(batteryChars, 9, 11));
            return modelTypeName.toString();
        } catch (Exception e) {
            log.error("ELE ERROR!battery type analysis fail,batteryName={}", batteryName, e);
        }
        
        return type;
    }
    
    private static String split(char[] strArray, int beginIndex, int endIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            stringBuilder.append(strArray[i]);
        }
        return stringBuilder.toString();
    }
    
    private String generateBatteryShortType(BatteryModelQuery batteryModelQuery, BatteryMaterial batteryMaterial) {
        
        String V = "V/";
        
        StringBuilder builder = new StringBuilder();
        return builder.append(batteryModelQuery.getStandardV()).append(V).append(batteryMaterial.getShortType())
                .append(SEPARATE).append(String.format("%02d", batteryModelQuery.getNumber())).toString();
    }
    
    private String generateBatteryType(BatteryModelQuery batteryModelQuery, BatteryMaterial batteryMaterial) {
        String B = "B_";
        String V = "V_";
        
        StringBuilder builder = new StringBuilder();
        return builder.append(B).append(batteryModelQuery.getStandardV()).append(V).append(batteryMaterial.getType())
                .append(SEPARATOR).append(batteryModelQuery.getNumber()).toString();
    }
    
    /**
     * 型号名称转换为中文
     */
    private String transformBatteryType(BatteryModel batteryModel, List<BatteryMaterial> batteryMaterials) {
        String batteryType = "";
        if (Objects.isNull(batteryModel) || StringUtils.isBlank(batteryModel.getBatteryType()) || StringUtils
                .isBlank(batteryModel.getBatteryVShort())) {
            return batteryType;
        }
        
        batteryType = acquireBatteryType(batteryModel.getBatteryType(), batteryModel.getBatteryVShort(),
                batteryMaterials);
        
        return batteryType;
    }
    
    private String transformBatteryType(BatteryModelPageVO batteryModel, List<BatteryMaterial> batteryMaterials) {
        String batteryType = "";
        if (Objects.isNull(batteryModel) || StringUtils.isBlank(batteryModel.getBatteryType()) || StringUtils
                .isBlank(batteryModel.getBatteryVShort())) {
            return batteryType;
        }
        
        batteryType = acquireBatteryType(batteryModel.getBatteryType(), batteryModel.getBatteryVShort(),
                batteryMaterials);
        
        return batteryType;
    }
    
    private String acquireBatteryType(String batteryType, String batteryVShort,
            List<BatteryMaterial> batteryMaterials) {
        Map<String, String> materialMap = batteryMaterials.stream().collect(Collectors
                .toMap(BatteryMaterial::getType, BatteryMaterial::getName, (String item1, String item2) -> item2));
        
        String[] split = batteryVShort.split(SEPARATE);
        if (ArrayUtils.isEmpty(split) || split.length < 2) {
            return batteryType;
        }
        
        String temp = batteryType;
        String typeName = temp.substring(temp.indexOf(SEPARATOR, batteryType.indexOf(SEPARATOR) + 1) + 1,
                temp.lastIndexOf(SEPARATOR));
        
        String materialName = materialMap.getOrDefault(typeName, "UNKNOWNAME");
        
        return split[0] + SEPARATE + materialName + SEPARATE + split[2] + "串";
    }
    
    /**
     * 生成系统默认电池型号
     */
    public static List<BatteryModel> generateDefaultBatteryModel(Integer tenantId) {
        List<BatteryModel> list = new ArrayList<>();
        BatteryModel b1 = new BatteryModel();
        b1.setBatteryModel(1);
        b1.setBatteryType("B_12V_TERNARY_LITHIUM_03");
        b1.setBatteryV(12.6);
        b1.setBatteryVShort("12V/T/3");
        b1.setMid(2L);
        b1.setTenantId(tenantId);
        b1.setType(BatteryModel.TYPE_SYSTEM);
        b1.setDelFlag(BatteryModel.DEL_NORMAL);
        b1.setCreateTime(System.currentTimeMillis());
        b1.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b2 = new BatteryModel();
        b2.setBatteryModel(2);
        b2.setBatteryType("B_12V_IRON_LITHIUM_03");
        b2.setBatteryV(14.6);
        b2.setBatteryVShort("12V/I/3");
        b2.setMid(1L);
        b2.setTenantId(tenantId);
        b2.setType(BatteryModel.TYPE_SYSTEM);
        b2.setDelFlag(BatteryModel.DEL_NORMAL);
        b2.setCreateTime(System.currentTimeMillis());
        b2.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b3 = new BatteryModel();
        b3.setBatteryModel(3);
        b3.setBatteryType("B_24V_TERNARY_LITHIUM_07");
        b3.setBatteryV(29.4);
        b3.setBatteryVShort("24V/T/7");
        b3.setMid(2L);
        b3.setTenantId(tenantId);
        b3.setType(BatteryModel.TYPE_SYSTEM);
        b3.setDelFlag(BatteryModel.DEL_NORMAL);
        b3.setCreateTime(System.currentTimeMillis());
        b3.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b4 = new BatteryModel();
        b4.setBatteryModel(4);
        b4.setBatteryType("B_24V_IRON_LITHIUM_08");
        b4.setBatteryV(29.2);
        b4.setBatteryVShort("24V/I/8");
        b4.setMid(1L);
        b4.setTenantId(tenantId);
        b4.setType(BatteryModel.TYPE_SYSTEM);
        b4.setDelFlag(BatteryModel.DEL_NORMAL);
        b4.setCreateTime(System.currentTimeMillis());
        b4.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b5 = new BatteryModel();
        b5.setBatteryModel(5);
        b5.setBatteryType("B_36V_TERNARY_LITHIUM_10");
        b5.setBatteryV(42D);
        b5.setBatteryVShort("36V/T/10");
        b5.setMid(2L);
        b5.setTenantId(tenantId);
        b5.setType(BatteryModel.TYPE_SYSTEM);
        b5.setDelFlag(BatteryModel.DEL_NORMAL);
        b5.setCreateTime(System.currentTimeMillis());
        b5.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b6 = new BatteryModel();
        b6.setBatteryModel(6);
        b6.setBatteryType("B_36V_IRON_LITHIUM_10");
        b6.setBatteryV(36.5);
        b6.setBatteryVShort("36V/I/10");
        b6.setMid(1L);
        b6.setTenantId(tenantId);
        b6.setType(BatteryModel.TYPE_SYSTEM);
        b6.setDelFlag(BatteryModel.DEL_NORMAL);
        b6.setCreateTime(System.currentTimeMillis());
        b6.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b7 = new BatteryModel();
        b7.setBatteryModel(7);
        b7.setBatteryType("B_36V_IRON_LITHIUM_11");
        b7.setBatteryV(40.15);
        b7.setBatteryVShort("36V/I/11");
        b7.setMid(1L);
        b7.setTenantId(tenantId);
        b7.setType(BatteryModel.TYPE_SYSTEM);
        b7.setDelFlag(BatteryModel.DEL_NORMAL);
        b7.setCreateTime(System.currentTimeMillis());
        b7.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b8 = new BatteryModel();
        b8.setBatteryModel(8);
        b8.setBatteryType("B_36V_IRON_LITHIUM_12");
        b8.setBatteryV(43.8);
        b8.setBatteryVShort("36V/I/12");
        b8.setMid(1L);
        b8.setTenantId(tenantId);
        b8.setType(BatteryModel.TYPE_SYSTEM);
        b8.setDelFlag(BatteryModel.DEL_NORMAL);
        b8.setCreateTime(System.currentTimeMillis());
        b8.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b9 = new BatteryModel();
        b9.setBatteryModel(9);
        b9.setBatteryType("B_48V_TERNARY_LITHIUM_13");
        b9.setBatteryV(54.6);
        b9.setBatteryVShort("48V/T/13");
        b9.setMid(2L);
        b9.setTenantId(tenantId);
        b9.setType(BatteryModel.TYPE_SYSTEM);
        b9.setDelFlag(BatteryModel.DEL_NORMAL);
        b9.setCreateTime(System.currentTimeMillis());
        b9.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b10 = new BatteryModel();
        b10.setBatteryModel(10);
        b10.setBatteryType("B_48V_TERNARY_LITHIUM_14");
        b10.setBatteryV(58.8);
        b10.setBatteryVShort("48V/T/14");
        b10.setMid(2L);
        b10.setTenantId(tenantId);
        b10.setType(BatteryModel.TYPE_SYSTEM);
        b10.setDelFlag(BatteryModel.DEL_NORMAL);
        b10.setCreateTime(System.currentTimeMillis());
        b10.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b11 = new BatteryModel();
        b11.setBatteryModel(11);
        b11.setBatteryType("B_48V_IRON_LITHIUM_15");
        b11.setBatteryV(54.8);
        b11.setBatteryVShort("48/I/15");
        b11.setMid(1L);
        b11.setTenantId(tenantId);
        b11.setType(BatteryModel.TYPE_SYSTEM);
        b11.setDelFlag(BatteryModel.DEL_NORMAL);
        b11.setCreateTime(System.currentTimeMillis());
        b11.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b12 = new BatteryModel();
        b12.setBatteryModel(12);
        b12.setBatteryType("B_48V_IRON_LITHIUM_16");
        b12.setBatteryV(58.4);
        b12.setBatteryVShort("48V/I/16");
        b12.setMid(1L);
        b12.setTenantId(tenantId);
        b12.setType(BatteryModel.TYPE_SYSTEM);
        b12.setDelFlag(BatteryModel.DEL_NORMAL);
        b12.setCreateTime(System.currentTimeMillis());
        b12.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b13 = new BatteryModel();
        b13.setBatteryModel(13);
        b13.setBatteryType("B_60V_TERNARY_LITHIUM_17");
        b13.setBatteryV(71.4);
        b13.setBatteryVShort("60V/T/17");
        b13.setMid(2L);
        b13.setTenantId(tenantId);
        b13.setType(BatteryModel.TYPE_SYSTEM);
        b13.setDelFlag(BatteryModel.DEL_NORMAL);
        b13.setCreateTime(System.currentTimeMillis());
        b13.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b14 = new BatteryModel();
        b14.setBatteryModel(14);
        b14.setBatteryType("B_60V_IRON_LITHIUM_20");
        b14.setBatteryV(73D);
        b14.setBatteryVShort("60V/I/20");
        b14.setMid(1L);
        b14.setTenantId(tenantId);
        b14.setType(BatteryModel.TYPE_SYSTEM);
        b14.setDelFlag(BatteryModel.DEL_NORMAL);
        b14.setCreateTime(System.currentTimeMillis());
        b14.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b15 = new BatteryModel();
        b15.setBatteryModel(15);
        b15.setBatteryType("B_72V_TERNARY_LITHIUM_20");
        b15.setBatteryV(84D);
        b15.setBatteryVShort("72V/T/20");
        b15.setMid(2L);
        b15.setTenantId(tenantId);
        b15.setType(BatteryModel.TYPE_SYSTEM);
        b15.setDelFlag(BatteryModel.DEL_NORMAL);
        b15.setCreateTime(System.currentTimeMillis());
        b15.setUpdateTime(System.currentTimeMillis());
        
        BatteryModel b16 = new BatteryModel();
        b16.setBatteryModel(16);
        b16.setBatteryType("B_72V_IRON_LITHIUM_24");
        b16.setBatteryV(87.6);
        b16.setBatteryVShort("72V/I/24");
        b16.setMid(1L);
        b16.setTenantId(tenantId);
        b16.setType(BatteryModel.TYPE_SYSTEM);
        b16.setDelFlag(BatteryModel.DEL_NORMAL);
        b16.setCreateTime(System.currentTimeMillis());
        b16.setUpdateTime(System.currentTimeMillis());
        
        list.add(b1);
        list.add(b2);
        list.add(b3);
        list.add(b4);
        list.add(b5);
        list.add(b6);
        list.add(b7);
        list.add(b8);
        list.add(b9);
        list.add(b10);
        list.add(b11);
        list.add(b12);
        list.add(b13);
        list.add(b14);
        list.add(b15);
        list.add(b16);
        
        return list;
    }
}

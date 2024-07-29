package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.entity.merchant.RebateConfig;
import com.xiliulou.electricity.mapper.merchant.RebateConfigMapper;
import com.xiliulou.electricity.request.merchant.RebateConfigRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.merchant.RebateConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 返利配置表(RebateConfig)表服务实现类
 *
 * @author zzlong
 * @since 2024-02-04 16:32:07
 */
@Service("rebateConfigService")
@Slf4j
public class RebateConfigServiceImpl implements RebateConfigService {
    
    @Resource
    private RebateConfigMapper rebateConfigMapper;
    
    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    private MerchantLevelService merchantLevelService;
    
    @Autowired
    private RedisService redisService;
    
    @Slave
    @Override
    public RebateConfig queryById(Long id) {
        return this.rebateConfigMapper.selectById(id);
    }
    
    @Override
    public RebateConfig queryByIdFromCache(Long id) {
        RebateConfig cacheRebateConfig = redisService.getWithHash(CacheConstant.CACHE_REBATE_CONFIG + id, RebateConfig.class);
        if (Objects.nonNull(cacheRebateConfig)) {
            return cacheRebateConfig;
        }
        
        RebateConfig rebateConfig = this.queryById(id);
        if (Objects.isNull(rebateConfig)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_REBATE_CONFIG + id, rebateConfig);
        return rebateConfig;
    }
    
    @Slave
    @Override
    public List<RebateConfigVO> listByPage(RebateConfigRequest rebateConfigRequest) {
        List<MerchantLevel> merchantLevels = merchantLevelService.listByTenantId(TenantContextHolder.getTenantId());
        
        List<RebateConfig> rebateConfigs = this.rebateConfigMapper.selectByPage(rebateConfigRequest);
        if (CollectionUtils.isEmpty(rebateConfigs) || CollectionUtils.isEmpty(merchantLevels)) {
            return Collections.emptyList();
        }
        
        Map<String, String> merchantLevelMap = merchantLevels.stream().collect(Collectors.toMap(MerchantLevel::getLevel, MerchantLevel::getName, (k1, k2) -> k1));
        
        return rebateConfigs.stream().map(item -> {
            RebateConfigVO rebateConfigVO = new RebateConfigVO();
            BeanUtils.copyProperties(item, rebateConfigVO);
            
            //            MerchantLevel merchantLevel = merchantLevelService.queryByMerchantLevelAndTenantId(item.getLevel(), item.getTenantId());
            rebateConfigVO.setLevelName(merchantLevelMap.getOrDefault(item.getLevel(), ""));
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMid());
            if (Objects.nonNull(batteryMemberCard)) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(batteryMemberCard.getFranchiseeId());
                rebateConfigVO.setMemberCardName(batteryMemberCard.getName());
                rebateConfigVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            }
            
            return rebateConfigVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer existsRebateConfigByMidAndLevel(Long mid, String level) {
        return this.rebateConfigMapper.existsRebateConfigByMidAndLevel(mid, level);
    }
    
    @Override
    public RebateConfig insert(RebateConfig rebateConfig) {
        this.rebateConfigMapper.insert(rebateConfig);
        return rebateConfig;
    }
    
    @Override
    public Integer update(RebateConfig rebateConfig) {
        int update = this.rebateConfigMapper.updateById(rebateConfig);
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> redisService.delete(CacheConstant.CACHE_REBATE_CONFIG + rebateConfig.getId()));
        
        return update;
    }
    
    @Override
    public Triple<Boolean, String, Object> save(RebateConfigRequest request) {
        //判断套餐是否存在
        Integer result = this.existsRebateConfigByMidAndLevel(request.getMid(), request.getLevel());
        if (Objects.nonNull(result)) {
            return Triple.of(false, "100318", "套餐返利配置已存在");
        }
    
        RebateConfig rebateConfig = new RebateConfig();
        BeanUtils.copyProperties(request, rebateConfig);
        rebateConfig.setDelFlag(CommonConstant.DEL_N);
        rebateConfig.setTenantId(TenantContextHolder.getTenantId());
        rebateConfig.setCreateTime(System.currentTimeMillis());
        rebateConfig.setUpdateTime(System.currentTimeMillis());
        this.insert(rebateConfig);
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> modify(RebateConfigRequest request) {
        RebateConfig rebateConfig = this.queryByIdFromCache(request.getId());
        if (Objects.isNull(rebateConfig) || !Objects.equals(rebateConfig.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100319", "返利配置不存在");
        }
        
        RebateConfig rebateConfigUpdate = new RebateConfig();
        BeanUtils.copyProperties(request, rebateConfigUpdate);
        rebateConfigUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(rebateConfigUpdate);
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public RebateConfig queryByMidAndMerchantLevel(Long memberCardId, String level) {
        return this.rebateConfigMapper.selectByMidAndMerchantLevel(memberCardId, level);
    }
    
    @Slave
    @Override
    public List<RebateConfig> listRebateConfigByMid(Long memberCardId) {
        return this.rebateConfigMapper.selectRebateConfigByMid(memberCardId);
    }
    
    @Slave
    @Override
    public RebateConfig queryLatestByMidAndMerchantLevel(Long memberCardId, String level) {
        return this.rebateConfigMapper.selectLatestByMidAndMerchantLevel(memberCardId, level);
    }
    
}

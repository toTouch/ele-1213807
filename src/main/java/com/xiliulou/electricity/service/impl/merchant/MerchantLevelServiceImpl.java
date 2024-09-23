package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.dto.merchant.MerchantLevelDTO;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.mapper.merchant.MerchantLevelMapper;
import com.xiliulou.electricity.request.merchant.MerchantLevelRequest;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SpinLockRedisService;
import com.xiliulou.electricity.vo.merchant.MerchantLevelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商户等级表(MerchantLevel)表服务实现类
 *
 * @author zzlong
 * @since 2024-02-04 14:35:06
 */
@Service("merchantLevelService")
@Slf4j
public class MerchantLevelServiceImpl implements MerchantLevelService {
    
    @Resource
    private MerchantLevelMapper merchantLevelMapper;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private MerchantAttrService merchantAttrService;
    
    @Resource
    private SpinLockRedisService spinLockRedisService;
    
    @Slave
    @Override
    public MerchantLevel queryById(Long id) {
        return this.merchantLevelMapper.selectById(id);
    }
    
    @Slave
    @Override
    public List<MerchantLevel> listByFranchiseeId(Integer tenantId, Long franchiseeId) {
        return this.merchantLevelMapper.selectByFranchiseeId(tenantId, franchiseeId);
    }
    
    @Slave
    @Override
    public List<MerchantLevel> listByTenantId(Integer tenantId) {
        return this.merchantLevelMapper.selectListByTenantId(tenantId);
    }
    
    @Override
    public MerchantLevel insert(MerchantLevel merchantLevel) {
        this.merchantLevelMapper.insert(merchantLevel);
        return merchantLevel;
    }
    
    @Override
    public Integer updateById(MerchantLevel merchantLevel) {
        return this.merchantLevelMapper.updateById(merchantLevel);
    }
    
    @Override
    public Integer deleteByFranchiseeId(Long franchiseeId) {
        return this.merchantLevelMapper.deleteByFranchiseeId(franchiseeId);
    }
    
    @Slave
    @Override
    public MerchantLevel queryNextByMerchantLevel(String level, Long franchiseeId) {
        return this.merchantLevelMapper.selectNextByMerchantLevel(level, franchiseeId);
    }
    
    @Slave
    @Override
    public MerchantLevel queryLastByMerchantLevel(String level, Long franchiseeId) {
        return this.merchantLevelMapper.selectLastByMerchantLevel(level, franchiseeId);
    }
    
    @Slave
    @Override
    public MerchantLevel queryByMerchantLevelAndFranchiseeId(String level, Long franchiseeId) {
        return this.merchantLevelMapper.selectByMerchantLevelAndFranchiseeId(level, franchiseeId);
    }
    
    @Slave
    @Override
    public List<MerchantLevel> queryListByIdList(List<Long> levelIdList) {
        return merchantLevelMapper.queryListByIdList(levelIdList);
    }
    
    @Slave
    @Override
    public Integer existsLevelName(String name, Long franchiseeId) {
        return merchantLevelMapper.existsLevelName(name, franchiseeId);
    }
    
    @Override
    public List<MerchantLevelVO> list(Integer tenantId, Long franchiseeId) {
        List<MerchantLevel> merchantLevels = applicationContext.getBean(MerchantLevelServiceImpl.class).listByFranchiseeId(tenantId, franchiseeId);
        if (CollectionUtils.isEmpty(merchantLevels)) {
            return Collections.emptyList();
        }
        
        return merchantLevels.stream().map(item -> {
            MerchantLevelVO merchantLevelVO = new MerchantLevelVO();
            BeanUtils.copyProperties(item, merchantLevelVO);
            if (StringUtils.isNotBlank(item.getRule())) {
                MerchantLevelDTO merchantLevelDTO = JsonUtil.fromJson(item.getRule(), MerchantLevelDTO.class);
                if (Objects.nonNull(merchantLevelDTO)) {
                    merchantLevelVO.setRenewalUserCount(merchantLevelDTO.getRenewalUserCount());
                    merchantLevelVO.setInvitationUserCount(merchantLevelDTO.getInvitationUserCount());
                } else {
                    merchantLevelVO.setRenewalUserCount(0L);
                    merchantLevelVO.setInvitationUserCount(0L);
                }
            }
            
            return merchantLevelVO;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Triple<Boolean, String, Object> modify(MerchantLevelRequest request) {
        if (!spinLockRedisService.tryLockWithSpin(CacheConstant.CACHE_MERCHANT_LEVEL_UPDATE_LOCK + request.getId())) {
            return Triple.of(true, null, null);
        }
        
        try {
            MerchantLevel merchantLevel = this.queryById(request.getId());
            if (Objects.isNull(merchantLevel) || !Objects.equals(merchantLevel.getTenantId(), TenantContextHolder.getTenantId())) {
                return Triple.of(true, null, null);
            }
            
            if (!Objects.equals(merchantLevel.getName(), request.getName()) && Objects.nonNull(existsLevelName(request.getName(), merchantLevel.getFranchiseeId()))) {
                return Triple.of(false, "100322", "等级名称已存在");
            }
            
            //商户升级条件
            MerchantAttr merchantAttr = merchantAttrService.queryByFranchiseeIdFromCache(merchantLevel.getFranchiseeId());
            if (Objects.isNull(merchantAttr)) {
                return Triple.of(false, "100323", "商户升级条件不存在");
            }
            
            if (!(Objects.equals(merchantAttr.getUpgradeCondition(), MerchantConstant.UPGRADE_CONDITION_ALL) || Objects
                    .equals(merchantAttr.getUpgradeCondition(), MerchantConstant.UPGRADE_CONDITION_INVITATION) || Objects
                    .equals(merchantAttr.getUpgradeCondition(), MerchantConstant.UPGRADE_CONDITION_RENEWAL))) {
                return Triple.of(false, "100323", "商户升级条件不合法");
            }
            
            MerchantLevel merchantLevelUpdate = new MerchantLevel();
            merchantLevelUpdate.setId(merchantLevel.getId());
            merchantLevelUpdate.setUpdateTime(System.currentTimeMillis());
            
            //仅修改商户等级名称
            if (Objects.nonNull(request.getName())) {
                merchantLevelUpdate.setName(request.getName());
            }
            
            //仅修改商户拉新人数或续费人数
            if (Objects.nonNull(request.getInvitationUserCount()) || Objects.nonNull(request.getRenewalUserCount())) {
                MerchantLevel nextMerchantLevel = this.queryNextByMerchantLevel(merchantLevel.getLevel(), merchantLevel.getFranchiseeId());
                if (Objects.nonNull(nextMerchantLevel) && StringUtils.isNotBlank(nextMerchantLevel.getRule())) {
                    MerchantLevelDTO merchantLevelDTO = JsonUtil.fromJson(nextMerchantLevel.getRule(), MerchantLevelDTO.class);
                    if (Objects.nonNull(merchantLevelDTO)) {
                        //拉新人数
                        if (Objects.equals(merchantAttr.getUpgradeCondition(), MerchantConstant.UPGRADE_CONDITION_INVITATION)) {
                            if (Objects.nonNull(merchantLevelDTO.getInvitationUserCount()) && Objects.nonNull(request.getInvitationUserCount())
                                    && merchantLevelDTO.getInvitationUserCount() < request.getInvitationUserCount()) {
                                return Triple.of(false, "100320", "当前等级设置的人数需小于上一级别，请进行调整");
                            }
                        }
                        
                        //续费人数
                        if (Objects.equals(merchantAttr.getUpgradeCondition(), MerchantConstant.UPGRADE_CONDITION_RENEWAL)) {
                            if (Objects.nonNull(merchantLevelDTO.getRenewalUserCount()) && Objects.nonNull(request.getRenewalUserCount())
                                    && merchantLevelDTO.getRenewalUserCount() < request.getRenewalUserCount()) {
                                return Triple.of(false, "100320", "当前等级设置的人数需小于上一级别，请进行调整");
                            }
                        }
                        
                        //拉新人数+续费人数
                        if (Objects.equals(merchantAttr.getUpgradeCondition(), MerchantConstant.UPGRADE_CONDITION_ALL)) {
                            if ((Objects.nonNull(merchantLevelDTO.getInvitationUserCount()) && Objects.nonNull(request.getInvitationUserCount())
                                    && merchantLevelDTO.getInvitationUserCount() < request.getInvitationUserCount()) || (Objects.nonNull(merchantLevelDTO.getRenewalUserCount())
                                    && Objects.nonNull(request.getRenewalUserCount()) && merchantLevelDTO.getRenewalUserCount() < request.getRenewalUserCount())) {
                                return Triple.of(false, "100320", "当前等级设置的人数需小于上一级别，请进行调整");
                            }
                        }
                    }
                }
                
                MerchantLevel lastMerchantLevel = this.queryLastByMerchantLevel(merchantLevel.getLevel(), merchantLevel.getFranchiseeId());
                if (Objects.nonNull(lastMerchantLevel) && StringUtils.isNotBlank(lastMerchantLevel.getRule())) {
                    MerchantLevelDTO merchantLevelDTO = JsonUtil.fromJson(lastMerchantLevel.getRule(), MerchantLevelDTO.class);
                    if (Objects.nonNull(merchantLevelDTO)) {
                        //拉新人数
                        if (Objects.equals(merchantAttr.getUpgradeCondition(), MerchantConstant.UPGRADE_CONDITION_INVITATION)) {
                            if (Objects.nonNull(merchantLevelDTO.getInvitationUserCount()) && Objects.nonNull(request.getInvitationUserCount())
                                    && merchantLevelDTO.getInvitationUserCount() > request.getInvitationUserCount()) {
                                return Triple.of(false, "100321", "当前等级设置的人数需大于下一级别，请进行调整");
                            }
                        }
                        
                        //续费人数
                        if (Objects.equals(merchantAttr.getUpgradeCondition(), MerchantConstant.UPGRADE_CONDITION_RENEWAL)) {
                            if (Objects.nonNull(merchantLevelDTO.getRenewalUserCount()) && Objects.nonNull(request.getRenewalUserCount())
                                    && merchantLevelDTO.getRenewalUserCount() > request.getRenewalUserCount()) {
                                return Triple.of(false, "100321", "当前等级设置的人数需大于下一级别，请进行调整");
                            }
                        }
                        
                        //拉新人数+续费人数
                        if (Objects.equals(merchantAttr.getUpgradeCondition(), MerchantConstant.UPGRADE_CONDITION_ALL)) {
                            if ((Objects.nonNull(merchantLevelDTO.getInvitationUserCount()) && Objects.nonNull(request.getInvitationUserCount())
                                    && merchantLevelDTO.getInvitationUserCount() > request.getInvitationUserCount()) || (Objects.nonNull(merchantLevelDTO.getRenewalUserCount())
                                    && Objects.nonNull(request.getRenewalUserCount()) && merchantLevelDTO.getRenewalUserCount() > request.getRenewalUserCount())) {
                                return Triple.of(false, "100321", "当前等级设置的人数需大于下一级别，请进行调整");
                            }
                        }
                    }
                }
                
                if (Objects.nonNull(request.getInvitationUserCount()) || Objects.nonNull(request.getRenewalUserCount())) {
                    MerchantLevelDTO merchantLevelDTO = new MerchantLevelDTO();
                    MerchantLevelDTO oldMerchantLevel = JsonUtil.fromJson(merchantLevel.getRule(), MerchantLevelDTO.class);
                    BeanUtils.copyProperties(oldMerchantLevel, merchantLevelDTO);
                    
                    if (Objects.nonNull(request.getInvitationUserCount())) {
                        merchantLevelDTO.setInvitationUserCount(request.getInvitationUserCount());
                    }
                    
                    if (Objects.nonNull(request.getRenewalUserCount())) {
                        merchantLevelDTO.setRenewalUserCount(request.getRenewalUserCount());
                    }
                    
                    merchantLevelUpdate.setRule(JsonUtil.toJson(merchantLevelDTO));
                }
            }
            
            this.updateById(merchantLevelUpdate);
            return Triple.of(true, null, null);
        } finally {
            spinLockRedisService.delete(CacheConstant.CACHE_MERCHANT_LEVEL_UPDATE_LOCK + request.getId());
        }
    }
    
    @Override
    public Integer initMerchantLevel(Long franchiseeId, Integer tenantId) {
        var merchantLevelList = new ArrayList<MerchantLevel>();
        
        for (int i = 1; i < 6; i++) {
            MerchantLevelDTO merchantLevelDTO = new MerchantLevelDTO(0L, 0L);
            MerchantLevel merchantLevel = new MerchantLevel();
            merchantLevel.setLevel(String.valueOf(i));
            merchantLevel.setName("");
            merchantLevel.setRule(JsonUtil.toJson(merchantLevelDTO));
            merchantLevel.setDelFlag(CommonConstant.DEL_N);
            merchantLevel.setFranchiseeId(franchiseeId);
            merchantLevel.setTenantId(tenantId);
            merchantLevel.setCreateTime(System.currentTimeMillis());
            merchantLevel.setUpdateTime(System.currentTimeMillis());
            merchantLevelList.add(merchantLevel);
        }
        
        return this.merchantLevelMapper.batchInsert(merchantLevelList);
    }
}

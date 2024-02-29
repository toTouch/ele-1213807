package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.MerchantLevelDTO;
import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.mapper.merchant.MerchantLevelMapper;
import com.xiliulou.electricity.request.merchant.MerchantLevelRequest;
import com.xiliulou.electricity.service.merchant.MerchantLevelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.merchant.MerchantLevelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
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
    
    @Override
    public MerchantLevel queryById(Long id) {
        return this.merchantLevelMapper.selectById(id);
    }
    
    @Slave
    @Override
    public List<MerchantLevel> listByTenantId(Integer tenantId) {
        return this.merchantLevelMapper.selectByTenantId(tenantId);
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
    public Integer deleteById(Long id) {
        return this.merchantLevelMapper.deleteById(id);
    }
    
    @Slave
    @Override
    public MerchantLevel queryNextByMerchantLevel(String level, Integer tenantId) {
        return this.merchantLevelMapper.selectNextByMerchantLevel(level, tenantId);
    }
    
    @Slave
    @Override
    public MerchantLevel queryLastByMerchantLevel(String level, Integer tenantId) {
        return this.merchantLevelMapper.selectLastByMerchantLevel(level, tenantId);
    }
    
    @Slave
    @Override
    public MerchantLevel queryByMerchantLevelAndTenantId(String level, Integer tenantId) {
        return this.merchantLevelMapper.selectByMerchantLevelAndTenantId(level, tenantId);
    }
    
    @Slave
    @Override
    public List<MerchantLevel> queryListByIdList(List<Long> levelIdList) {
        return merchantLevelMapper.queryListByIdList(levelIdList);
    }
    
    @Slave
    @Override
    public Integer existsLevelName(String name, Integer tenantId) {
        return merchantLevelMapper.existsLevelName(name, tenantId);
    }
    
    @Override
    public List<MerchantLevelVO> list(Integer tenantId) {
        List<MerchantLevel> merchantLevels = this.listByTenantId(tenantId);
        if (CollectionUtils.isEmpty(merchantLevels)) {
            return Collections.emptyList();
        }
        
        return merchantLevels.stream().map(item -> {
            MerchantLevelVO merchantLevelVO = new MerchantLevelVO();
            BeanUtils.copyProperties(item, merchantLevelVO);
            if (StringUtils.isNotBlank(item.getRule())) {
                MerchantLevelDTO merchantLevelDTO = JsonUtil.fromJson(item.getRule(), MerchantLevelDTO.class);
                merchantLevelVO.setRenewalUserCount(Objects.nonNull(merchantLevelDTO) ? merchantLevelDTO.getRenewalUserCount() : 0);
                merchantLevelVO.setInvitationUserCount(Objects.nonNull(merchantLevelDTO) ? merchantLevelDTO.getInvitationUserCount() : 0);
            }
            
            return merchantLevelVO;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Triple<Boolean, String, Object> modify(MerchantLevelRequest request) {
        MerchantLevel merchantLevel = this.queryById(request.getId());
        if (Objects.isNull(merchantLevel) || !Objects.equals(merchantLevel.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        if (!Objects.equals(merchantLevel.getName(), request.getName()) && Objects.nonNull(existsLevelName(request.getName(), merchantLevel.getTenantId()))) {
            return Triple.of(false, "100322", "等级名称已存在");
        }
        
        MerchantLevel nextMerchantLevel = this.queryNextByMerchantLevel(merchantLevel.getLevel(), merchantLevel.getTenantId());
        if (Objects.nonNull(nextMerchantLevel) && StringUtils.isNotBlank(nextMerchantLevel.getRule())) {
            MerchantLevelDTO merchantLevelDTO = JsonUtil.fromJson(nextMerchantLevel.getRule(), MerchantLevelDTO.class);
            if (Objects.nonNull(merchantLevelDTO)) {
                if ((Objects.nonNull(merchantLevelDTO.getInvitationUserCount()) && merchantLevelDTO.getInvitationUserCount() <= request.getInvitationUserCount()) || (
                        Objects.nonNull(merchantLevelDTO.getRenewalUserCount()) && merchantLevelDTO.getRenewalUserCount() <= request.getRenewalUserCount())) {
                    return Triple.of(false, "100320", "当前等级设置的人数需于小下一级别，请进行调整");
                }
            }
        }
        
        MerchantLevel lastMerchantLevel = this.queryLastByMerchantLevel(merchantLevel.getLevel(), merchantLevel.getTenantId());
        if (Objects.nonNull(lastMerchantLevel) && StringUtils.isNotBlank(lastMerchantLevel.getRule())) {
            MerchantLevelDTO merchantLevelDTO = JsonUtil.fromJson(lastMerchantLevel.getRule(), MerchantLevelDTO.class);
            if (Objects.nonNull(merchantLevelDTO)) {
                if ((Objects.nonNull(merchantLevelDTO.getInvitationUserCount()) && merchantLevelDTO.getInvitationUserCount() >= request.getInvitationUserCount()) || (
                        Objects.nonNull(merchantLevelDTO.getRenewalUserCount()) && merchantLevelDTO.getRenewalUserCount() >= request.getRenewalUserCount())) {
                    return Triple.of(false, "100321", "当前等级设置的人数需大于下一级别，请进行调整");
                }
            }
        }
        
        MerchantLevel merchantLevelUpdate = new MerchantLevel();
        merchantLevelUpdate.setId(merchantLevel.getId());
        merchantLevelUpdate.setName(request.getName());
        merchantLevelUpdate.setUpdateTime(System.currentTimeMillis());
        
        if (Objects.nonNull(request.getInvitationUserCount()) || Objects.nonNull(request.getRenewalUserCount())) {
            merchantLevelUpdate.setRule(JsonUtil.toJson(new MerchantLevelDTO(request.getInvitationUserCount(), request.getRenewalUserCount())));
        }
        
        this.updateById(merchantLevelUpdate);
        return Triple.of(true, null, null);
    }
    
    @Override
    public Integer initMerchantLevel(Integer tenantId) {
        var merchantLevelList = new ArrayList<MerchantLevel>();
        
        for (int i = 1; i < 6; i++) {
            MerchantLevel merchantLevel = new MerchantLevel();
            merchantLevel.setLevel(String.valueOf(i));
            merchantLevel.setName("");
            merchantLevel.setRule(null);
            merchantLevel.setDelFlag(CommonConstant.DEL_N);
            merchantLevel.setTenantId(tenantId);
            merchantLevel.setCreateTime(System.currentTimeMillis());
            merchantLevel.setUpdateTime(System.currentTimeMillis());
            merchantLevelList.add(merchantLevel);
        }
        
        return this.merchantLevelMapper.batchInsert(merchantLevelList);
    }
}

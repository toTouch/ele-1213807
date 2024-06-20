package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.merchant.AreaCabinetNumBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.mapper.merchant.MerchantAreaMapper;
import com.xiliulou.electricity.query.merchant.MerchantAreaQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantAreaRequest;
import com.xiliulou.electricity.request.merchant.MerchantAreaSaveOrUpdateRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.merchant.MerchantAreaVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 区域管理服务
 * @date 2024/2/6 13:54:06
 */
@Service
@Slf4j
public class MerchantAreaServiceImpl implements MerchantAreaService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private MerchantAreaMapper merchantAreaMapper;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private ChannelEmployeeService channelEmployeeService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Override
    public R save(MerchantAreaSaveOrUpdateRequest saveRequest, Long operator) {
        boolean result = redisService.setNx(CacheConstant.CACHE_MERCHANT_AREA_SAVE_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
    
        // 检测选中的加盟商和当前登录加盟商是否一致
        if (Objects.nonNull(saveRequest.getBindFranchiseeId()) && !Objects.equals(saveRequest.getBindFranchiseeId(), saveRequest.getFranchiseeId())) {
            log.info("merchant area save info, franchisee is not different ,name = {}, franchiseeId={}, bindFranchiseeId={}", saveRequest.getName(), saveRequest.getFranchiseeId(), saveRequest.getBindFranchiseeId());
            return R.fail(false, "120240", "当前加盟商无权限操作");
        }
        
        try {
            String areaName = saveRequest.getName();
            Integer areaExist = merchantAreaMapper.existsByAreaName(areaName, TenantContextHolder.getTenantId());
            if (Objects.nonNull(areaExist)) {
                return R.fail("120103", "区域名称不能重复，请修改后操作");
            }
            
            long now = System.currentTimeMillis();
            MerchantArea merchantArea = MerchantArea.builder().name(saveRequest.getName()).remark(saveRequest.getRemark()).createTime(now).updateTime(now)
                    .franchiseeId(saveRequest.getFranchiseeId()).delFlag(NumberConstant.ZERO).tenantId(TenantContextHolder.getTenantId()).build();
            
            return R.ok(merchantAreaMapper.insertOne(merchantArea));
        } finally {
            redisService.delete(CacheConstant.CACHE_MERCHANT_AREA_SAVE_LOCK + operator);
        }
    }
    
    /**
     * 逻辑删
     */
    @Override
    public R deleteById(Long id, Long bindFranchiseeId) {
        MerchantArea merchantArea = merchantAreaMapper.selectById(id);
        if (Objects.isNull(merchantArea)) {
            return R.fail("120218", "区域不存在");
        }
        
        if (Objects.nonNull(bindFranchiseeId) && !Objects.equals(merchantArea.getFranchiseeId(), bindFranchiseeId)) {
            log.info("merchant area delete info, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", id, merchantArea.getFranchiseeId(), bindFranchiseeId);
            return R.fail("120240", "当前加盟商无权限操作");
        }
        
        Integer cabinetExist = electricityCabinetService.existsByAreaId(id);
        if (Objects.nonNull(cabinetExist)) {
            return R.fail("120100", "该区域有电柜正在使用，请先解绑后操作");
        }
        
        Integer placeExist = merchantPlaceService.existsByAreaId(id);
        if (Objects.nonNull(placeExist)) {
            return R.fail("120101", "该区域有场地正在使用，请先解绑后操作");
        }
        
        Integer channelEmpExist = channelEmployeeService.existsByAreaId(id);
        if (Objects.nonNull(channelEmpExist)) {
            return R.fail("120102", "该区域有渠道员正在使用，请先解绑后操作");
        }
        
        return R.ok(merchantAreaMapper.deleteById(id, TenantContextHolder.getTenantId()));
    }
    
    @Override
    public R updateById(MerchantAreaSaveOrUpdateRequest updateRequest) {
        String areaName = updateRequest.getName();
        MerchantArea oldMerchantArea = this.queryById(updateRequest.getId());
        if (Objects.nonNull(oldMerchantArea)) {
            if (!Objects.equals(oldMerchantArea.getName(), areaName)) {
                Integer areaExist = merchantAreaMapper.existsByAreaName(areaName, TenantContextHolder.getTenantId());
                if (Objects.nonNull(areaExist)) {
                    return R.fail("300904", "区域名称不能重复，请修改后操作");
                }
            }
        }
    
        // 判断修改的加盟商是否有改变
        if (!Objects.equals(updateRequest.getFranchiseeId(), oldMerchantArea.getFranchiseeId())) {
            log.info("merchant area update info, franchisee not allow change id={}, franchiseeId={}, updateFranchiseeId={}", updateRequest.getId(), oldMerchantArea.getFranchiseeId(), updateRequest.getFranchiseeId());
            return R.fail( "120123", "商户加盟商不允许修改");
        }
    
        // 检测选中的加盟商和当前登录加盟商是否一致
        if (Objects.nonNull(updateRequest.getBindFranchiseeId()) && !Objects.equals(updateRequest.getBindFranchiseeId(), updateRequest.getFranchiseeId())) {
            log.info("merchant area update info, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", updateRequest.getId(), updateRequest.getFranchiseeId(), updateRequest.getBindFranchiseeId());
            return R.fail("120240", "当前加盟商无权限操作");
        }
        
        MerchantArea merchantArea = MerchantArea.builder().id(updateRequest.getId()).name(areaName).remark(updateRequest.getRemark()).updateTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(merchantAreaMapper.updateById(merchantArea));
    }
    
    @Slave
    @Override
    public List<MerchantAreaVO> listByPage(MerchantAreaRequest request) {
        MerchantAreaQueryModel queryModel = new MerchantAreaQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<MerchantArea> merchantAreaList = merchantAreaMapper.selectPage(queryModel);
        if (CollectionUtils.isEmpty(merchantAreaList)) {
            return Collections.emptyList();
        }
        
        // 查询各区域柜机数
        List<Long> areaIdList = merchantAreaList.stream().map(MerchantArea::getId).collect(Collectors.toList());
        List<AreaCabinetNumBO> cabinetNumList = electricityCabinetService.countByAreaGroup(areaIdList);
        Map<Long, Integer> cabinetNumMap = null;
        if (CollectionUtils.isNotEmpty(cabinetNumList)) {
            cabinetNumMap = cabinetNumList.stream().collect(
                    Collectors.toMap(AreaCabinetNumBO::getAreaId, areaCabinetNumBO -> Optional.ofNullable(areaCabinetNumBO.getCabinetNum()).orElse(NumberConstant.ZERO),
                            (k, v) -> v));
        }
        
        // 填充柜机数量
        Map<Long, Integer> finalCabinetNumMap = cabinetNumMap;
        return merchantAreaList.stream().map(item -> {
            MerchantAreaVO merchantAreaVO = new MerchantAreaVO();
            BeanUtils.copyProperties(item, merchantAreaVO);
            
            merchantAreaVO.setCabinetNun(Optional.ofNullable(finalCabinetNumMap).map(map -> map.get(item.getId())).orElse(NumberConstant.ZERO));
    
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            if (Objects.nonNull(franchisee)) {
                merchantAreaVO.setFranchiseeName(franchisee.getName());
            }
            
            return merchantAreaVO;
            
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantAreaRequest request) {
        MerchantAreaQueryModel queryModel = new MerchantAreaQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return merchantAreaMapper.countTotal(queryModel);
    }
    
    @Slave
    @Override
    public List<MerchantArea> listAll(MerchantAreaRequest request) {
        MerchantAreaQueryModel queryModel = new MerchantAreaQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<MerchantArea> merchantAreaList = merchantAreaMapper.selectPage(queryModel);
        if (CollectionUtils.isEmpty(merchantAreaList)) {
            return Collections.emptyList();
        }
        
        return merchantAreaList;
    }
    
    @Slave
    @Override
    public List<MerchantArea> queryList(MerchantAreaRequest request) {
        MerchantAreaQueryModel queryModel = new MerchantAreaQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return merchantAreaMapper.queryList(queryModel);
    }
    
    @Override
    public MerchantArea queryById(Long id) {
        return merchantAreaMapper.selectById(id);
    }
    
}

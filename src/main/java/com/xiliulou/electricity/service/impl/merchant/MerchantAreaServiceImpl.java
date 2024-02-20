package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.merchant.AreaCabinetNumBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.mapper.merchant.MerchantAreaMapper;
import com.xiliulou.electricity.query.merchant.MerchantAreaQuery;
import com.xiliulou.electricity.request.merchant.MerchantAreaSaveOrUpdateRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.merchant.MerchantAreaVO;
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
public class MerchantAreaServiceImpl implements MerchantAreaService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private MerchantAreaMapper merchantAreaMapper;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Override
    public R save(MerchantAreaSaveOrUpdateRequest saveRequest, Long operator) {
        boolean result = redisService.setNx(CacheConstant.CACHE_MERCHANT_AREA_SAVE_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            String areaName = saveRequest.getName();
            Integer areaExist = merchantAreaMapper.existsByAreaName(areaName, TenantContextHolder.getTenantId());
            if (Objects.nonNull(areaExist)) {
                return R.fail("300904", "区域名称不能重复，请修改后操作");
            }
            
            long now = System.currentTimeMillis();
            MerchantArea merchantArea = MerchantArea.builder().name(saveRequest.getName()).remark(saveRequest.getRemark()).createTime(now).updateTime(now)
                    .delFlag(NumberConstant.ZERO).tenantId(TenantContextHolder.getTenantId()).build();
            
            return R.ok(merchantAreaMapper.insertOne(merchantArea));
        } finally {
            redisService.delete(CacheConstant.CACHE_MERCHANT_AREA_SAVE_LOCK + operator);
        }
    }
    
    @Override
    public R deleteById(Long id) {
        Integer cabinetExist = electricityCabinetService.existsByAreaId(id);
        if (Objects.nonNull(cabinetExist)) {
            return R.fail("300900", "该区域有电柜正在使用，请先解绑后操作");
        }
    
        Integer placeExist = merchantPlaceService.existsByAreaId(id);
        if (Objects.nonNull(placeExist)) {
            return R.fail("300901", "该区域有场地正在使用，请先解绑后操作");
        }
    
        // todo:渠道员
    
        return R.ok(merchantAreaMapper.deleteById(id, TenantContextHolder.getTenantId()));
    }
    
    @Override
    public R updateById(MerchantAreaSaveOrUpdateRequest updateRequest) {
        String areaName = updateRequest.getName();
        Integer areaExist = merchantAreaMapper.existsByAreaName(areaName, TenantContextHolder.getTenantId());
        if (Objects.nonNull(areaExist)) {
            return R.fail("300904", "区域名称不能重复，请修改后操作");
        }
        
        MerchantArea merchantArea = MerchantArea.builder().id(updateRequest.getId()).name(areaName).remark(updateRequest.getRemark()).updateTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(merchantAreaMapper.updateById(merchantArea));
    }
    
    @Slave
    @Override
    public List<MerchantAreaVO> listByPage(MerchantAreaQuery query) {
        List<MerchantArea> merchantAreaList = merchantAreaMapper.selectPage(query);
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
            
            return merchantAreaVO;
            
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantAreaQuery query) {
        return merchantAreaMapper.countTotal(query);
    }
    
    @Slave
    @Override
    public List<MerchantArea> listAll(MerchantAreaQuery query) {
        List<MerchantArea> merchantAreaList = merchantAreaMapper.selectPage(query);
        if (CollectionUtils.isEmpty(merchantAreaList)) {
            return Collections.emptyList();
        }
    
        return merchantAreaList;
    }
    
    @Slave
    @Override
    public List<MerchantArea> queryList(MerchantAreaQuery areaQuery) {
        return merchantAreaMapper.queryList(areaQuery);
    }
    
    @Override
    public MerchantArea queryById(Long id) {
        return merchantAreaMapper.selectById(id);
    }
    
}

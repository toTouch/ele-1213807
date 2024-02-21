package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.EleSumPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceUserVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 商户/场地/下 柜机电量/电费
 * @date 2024/2/20 19:14:12
 */
@Service
public class MerchantCabinetPowerServiceImpl implements MerchantCabinetPowerService {
    
    @Resource
    private MerchantPlaceBindService merchantPlaceBindService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private ElePowerService elePowerService;
    
    @Slave
    @Override
    public Integer isShowPowerPage(Long merchantId) {
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchantId);
        if (CollectionUtils.isNotEmpty(bindList)) {
            return NumberConstant.ONE;
        }
        return NumberConstant.ZERO;
    }
    
    @Slave
    @Override
    public MerchantPlaceAndCabinetUserVO listPlaceAndCabinetByMerchantId(Long merchantId) {
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchantId);
        if (CollectionUtils.isEmpty(bindList)) {
            return null;
        }
        
        // 获取场地列表
        Set<Long> placeIdSet = bindList.parallelStream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
        List<MerchantPlaceUserVO> placeList = placeIdSet.parallelStream().map(placeId -> {
            MerchantPlaceUserVO merchantPlaceUserVO = new MerchantPlaceUserVO();
            merchantPlaceUserVO.setPlaceId(placeId);
            merchantPlaceUserVO.setPlaceName(Optional.ofNullable(merchantPlaceService.queryFromCacheById(placeId)).orElse(new MerchantPlace()).getName());
            
            return merchantPlaceUserVO;
        }).collect(Collectors.toList());
        
        // 获取柜机列表
        List<MerchantPlaceCabinetVO> cabinetList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBindList = merchantPlaceCabinetBindService.listByPlaceIds(placeIdSet);
        if (CollectionUtils.isNotEmpty(merchantPlaceCabinetBindList)) {
            Set<Long> cabinetIdSet = merchantPlaceCabinetBindList.parallelStream().map(MerchantPlaceCabinetBind::getCabinetId).collect(Collectors.toSet());
            cabinetIdSet.forEach(cabinetId -> {
                MerchantPlaceCabinetVO merchantPlaceCabinetVO = new MerchantPlaceCabinetVO();
                merchantPlaceCabinetVO.setCabinetId(cabinetId);
                merchantPlaceCabinetVO.setCabinetName(
                        Optional.ofNullable(electricityCabinetService.queryByIdFromCache(cabinetId.intValue())).orElse(new ElectricityCabinet()).getName());
                
                cabinetList.add(merchantPlaceCabinetVO);
            });
        }
        
        // 封装VO
        MerchantPlaceAndCabinetUserVO merchantPlaceAndCabinetUserVO = new MerchantPlaceAndCabinetUserVO();
        merchantPlaceAndCabinetUserVO.setMerchantId(merchantId);
        merchantPlaceAndCabinetUserVO.setPlaceList(CollectionUtils.isEmpty(placeList) ? Collections.emptyList() : placeList);
        merchantPlaceAndCabinetUserVO.setCabinetList(CollectionUtils.isEmpty(cabinetList) ? Collections.emptyList() : cabinetList);
        
        return merchantPlaceAndCabinetUserVO;
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetVO> listCabinetByPlaceId(Long merchantId, Long placeId) {
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchantId);
        if (CollectionUtils.isEmpty(bindList)) {
            return null;
        }
        
        // 判断所选场地是否存在
        Set<Long> placeIdSet = bindList.stream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
        if (!placeIdSet.contains(placeId)) {
            return null;
        }
        
        // 获取柜机列表
        List<MerchantPlaceCabinetVO> cabinetList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBindList = merchantPlaceCabinetBindService.listByPlaceIds(Set.of(placeId));
        if (CollectionUtils.isNotEmpty(merchantPlaceCabinetBindList)) {
            Set<Long> cabinetIdSet = merchantPlaceCabinetBindList.stream().map(MerchantPlaceCabinetBind::getCabinetId).collect(Collectors.toSet());
            cabinetIdSet.forEach(cabinetId -> {
                MerchantPlaceCabinetVO merchantPlaceCabinetVO = new MerchantPlaceCabinetVO();
                merchantPlaceCabinetVO.setCabinetId(cabinetId);
                merchantPlaceCabinetVO.setCabinetName(
                        Optional.ofNullable(electricityCabinetService.queryByIdFromCache(cabinetId.intValue())).orElse(new ElectricityCabinet()).getName());
                
                cabinetList.add(merchantPlaceCabinetVO);
            });
        }
        
        if (CollectionUtils.isEmpty(cabinetList)) {
            return Collections.emptyList();
        }
        
        return cabinetList;
    }
    
    @Slave
    @Override
    public MerchantCabinetPowerVO todayPowerAndCharge(MerchantCabinetPowerRequest request) {
        // 获取要查询电量的柜机
        List<Long> cabinetIdList = this.getSearchedCabinetIdList(request);
    
        if (CollectionUtils.isEmpty(cabinetIdList)) {
            return null;
        }
        
        long todayStartTime = DateUtils.getTodayStartTime();
        EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(todayStartTime, System.currentTimeMillis(), cabinetIdList, TenantContextHolder.getTenantId());
        if (Objects.isNull(eleSumPowerVO)) {
            return null;
        }
        
        // 封装数据
        MerchantCabinetPowerVO merchantCabinetPowerVO = new MerchantCabinetPowerVO();
        BeanUtils.copyProperties(eleSumPowerVO, merchantCabinetPowerVO);
        
        return merchantCabinetPowerVO;
    }
    
    @Slave
    @Override
    public MerchantCabinetPowerVO yesterdayPowerAndCharge(MerchantCabinetPowerRequest request) {
        // 获取要查询电量的柜机
        List<Long> cabinetIdList = this.getSearchedCabinetIdList(request);
    
        if (CollectionUtils.isEmpty(cabinetIdList)) {
            return null;
        }
    
        long yesterdayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
        long yesterdayEndTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(yesterdayStartTime, yesterdayEndTime, cabinetIdList, TenantContextHolder.getTenantId());
        if (Objects.isNull(eleSumPowerVO)) {
            return null;
        }
    
        // 封装数据
        MerchantCabinetPowerVO merchantCabinetPowerVO = new MerchantCabinetPowerVO();
        BeanUtils.copyProperties(eleSumPowerVO, merchantCabinetPowerVO);
    
        return merchantCabinetPowerVO;
    }
    
    @Override
    public MerchantCabinetPowerVO thisMonthPowerAndCharge(MerchantCabinetPowerRequest request) {
        return null;
    }
    
    /**
     * 获取要查询电量的柜机
     */
    private List<Long> getSearchedCabinetIdList(MerchantCabinetPowerRequest request) {
        Long merchantId = request.getMerchantId();
        if (Objects.isNull(merchantId)) {
            return null;
        }
        
        Long placeId = request.getPlaceId();
        Long cabinetId = request.getCabinetId();
        List<Long> cabinetIdList = null;
        // 1.场地和柜机为null，查全量
        if (Objects.isNull(placeId) && Objects.isNull(cabinetId)) {
            MerchantPlaceAndCabinetUserVO merchantPlaceAndCabinetUserVO = this.listPlaceAndCabinetByMerchantId(merchantId);
            if (Objects.isNull(merchantPlaceAndCabinetUserVO) || CollectionUtils.isEmpty(merchantPlaceAndCabinetUserVO.getCabinetList())) {
                return null;
            }
            
            List<MerchantPlaceCabinetVO> cabinetList = merchantPlaceAndCabinetUserVO.getCabinetList();
            cabinetIdList = cabinetList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        // 2.场地不为null，柜机为null
        if (Objects.nonNull(placeId) && Objects.isNull(cabinetId)) {
            List<MerchantPlaceCabinetVO> placeCabinetVOList = this.listCabinetByPlaceId(merchantId, merchantId);
            cabinetIdList = placeCabinetVOList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        return cabinetIdList;
    }
}

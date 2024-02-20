package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceBindMapper;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:50
 * @desc
 */
@Service("merchantPlaceBindService")
@Slf4j
public class MerchantPlaceBindServiceImpl implements MerchantPlaceBindService {
    @Resource
    private MerchantPlaceBindMapper placeBindMapper;
    
    @Resource
    private MerchantPlaceService mergePlaceService;
    
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Override
    public int batchInsert(List<MerchantPlaceBind> merchantPlaceBindList) {
        return placeBindMapper.batchInsert(merchantPlaceBindList);
    }
    
    @Override
    public int batchUnBind(Set<Long> unBindList, Long merchantId, long updateTime) {
        return placeBindMapper.batchUnBind(unBindList, merchantId, updateTime);
    }
    
    @Slave
    @Override
    public Integer isShowPowerPage(Long merchantId) {
        List<MerchantPlaceBind> bindList = placeBindMapper.selectListByMerchantId(merchantId);
        if (CollectionUtils.isNotEmpty(bindList)) {
            return NumberConstant.ONE;
        }
        return NumberConstant.ZERO;
    }
    
    @Slave
    @Override
    public MerchantPlaceAndCabinetUserVO listPlaceAndCabinetByMerchantId(Long merchantId) {
        List<MerchantPlaceBind> bindList = placeBindMapper.selectListByMerchantId(merchantId);
        if (CollectionUtils.isEmpty(bindList)) {
            return null;
        }
    
        // 获取场地列表
        Set<Long> placeIdSet = bindList.stream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
        List<MerchantPlaceUserVO> placeList = placeIdSet.stream().map(placeId -> {
            MerchantPlaceUserVO merchantPlaceUserVO = new MerchantPlaceUserVO();
            merchantPlaceUserVO.setPlaceId(placeId);
            merchantPlaceUserVO.setPlaceName(Optional.ofNullable(mergePlaceService.queryFromCacheById(placeId)).orElse(new MerchantPlace()).getName());
        
            return merchantPlaceUserVO;
        }).collect(Collectors.toList());
    
        // 获取柜机列表
        List<MerchantPlaceCabinetVO> cabinetList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBindList = merchantPlaceCabinetBindService.listByPlaceIds(placeIdSet);
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
        List<MerchantPlaceBind> bindList = placeBindMapper.selectListByMerchantId(merchantId);
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
    
}

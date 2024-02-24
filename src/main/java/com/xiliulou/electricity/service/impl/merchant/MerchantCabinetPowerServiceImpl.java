package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetConditionRequest;
import com.xiliulou.electricity.request.merchant.MerchantPowerAndPlaceFeeRequest;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPowerVO;
import org.apache.commons.collections.CollectionUtils;
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
    
    @Resource
    private RedisService redisService;
    
    @Slave
    @Override
    public Integer isShowPowerPage(Long merchantId) {
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchantId, null);
        if (CollectionUtils.isNotEmpty(bindList)) {
            return NumberConstant.ONE;
        }
        return NumberConstant.ZERO;
    }
    
    @Slave
    @Override
    public MerchantPlaceAndCabinetUserVO listPlaceAndCabinetByMerchantId(Long merchantId) {
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchantId, null);
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
        MerchantPlaceCabinetConditionRequest conditionRequest = MerchantPlaceCabinetConditionRequest.builder().placeIds(placeIdSet).build();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBindList = merchantPlaceCabinetBindService.listByConditions(conditionRequest);
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
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchantId, null);
        if (CollectionUtils.isEmpty(bindList)) {
            return null;
        }
        
        // 判断所选场地是否存在
        Set<Long> placeIdSet = bindList.stream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(placeIdSet) && !placeIdSet.contains(placeId)) {
            return null;
        }
        
        // 获取柜机列表
        List<MerchantPlaceCabinetVO> cabinetList = new ArrayList<>();
        MerchantPlaceCabinetConditionRequest conditionRequest = MerchantPlaceCabinetConditionRequest.builder().placeIds(Set.of(placeId)).build();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBindList = merchantPlaceCabinetBindService.listByConditions(conditionRequest);
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
    
    @Override
    public List<MerchantPowerVO> powerData(MerchantCabinetPowerRequest request) {
        Long merchantId = request.getMerchantId();
        Long placeIdReq = request.getPlaceId();
        Long cabinetIdReq = request.getCabinetId();
        
        if (Objects.isNull(merchantId)) {
            return Collections.emptyList();
        }
        
        // 1.全量查
        if (Objects.isNull(placeIdReq) && Objects.isNull(cabinetIdReq)) {
            // 1.1 根据merchantId获取商户场地绑定记录中所有未结算的记录（2个月前的数据在月结表已统计，比如：当前2月，去年11月的数据已月结统计）
            List<MerchantPlaceBind> placeBindList = merchantPlaceBindService.listByMerchantId(merchantId, MerchantPlaceConstant.MONTH_SETTLEMENT_POWER_NO);
            if (CollectionUtils.isEmpty(placeBindList)) {
                return Collections.emptyList();
            }
            
            // 1.2 遍历场地
            Set<Long> placeIdSet = placeBindList.stream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
            for (Long placeId : placeIdSet) {
                // 根据placeId获取场地柜机绑定记录中所有未结束的记录
                MerchantPlaceCabinetConditionRequest conditionRequest = MerchantPlaceCabinetConditionRequest.builder().placeIds(Set.of(placeId))
                        .powerSettleStatus(MerchantPlaceConstant.PLACE_MONTH_SETTLEMENT_POWER_NO).build();
                List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listByConditions(conditionRequest);
                if (CollectionUtils.isEmpty(cabinetBindList)) {
                    continue;
                }
                
                Set<Long> cabinetIdSet = cabinetBindList.stream().map(MerchantPlaceCabinetBind::getCabinetId).collect(Collectors.toSet());
                // 4.执行
                //                List<CabinetPowerMonthRecordProRunnable> collect = cabinetIdSet.parallelStream()
                //                        .map(eid -> new CabinetPowerMonthRecordProRunnable(eid, merchantCabinetPowerMonthRecordProService, merchantCabinetPowerMonthDetailProService,
                //                                elePowerService, cabinetBindList, startTime, endTime, date, merchantId, placeId)).collect(Collectors.toList());
                //
                //                try {
                //                    List<Future<Integer>> futures = executorService.invokeAll(collect);
                //                } catch (InterruptedException e) {
                //                    log.error("Merchant Cabinet Power Month Record Pro Exception occur! date={}", date, e);
                //                }
                
            }
        }
        
        // 2.查询所选场地下所有柜机
        if (Objects.nonNull(placeIdReq) && Objects.isNull(cabinetIdReq)) {
        
        }
        
        // 3.查询指定柜机
        if (Objects.nonNull(placeIdReq) && Objects.nonNull(cabinetIdReq)) {
        
        }
        
        return null;
    }
    
    @Slave
    @Override
    public List<Long> getRequestedCabinetIds(MerchantPowerAndPlaceFeeRequest request) {
        Long merchantId = request.getMerchantId();
        if (Objects.isNull(merchantId)) {
            return null;
        }
        
        Long placeId = request.getPlaceId();
        Long cabinetId = request.getCabinetId();
        
        // 先从缓存获取，如果未获取到再从数据库获取
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
            List<MerchantPlaceCabinetVO> placeCabinetVOList = this.listCabinetByPlaceId(merchantId, placeId);
            cabinetIdList = placeCabinetVOList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        // 3. 场地不为null,柜机不为null
        if (Objects.nonNull(placeId) && Objects.nonNull(cabinetId)) {
            List<MerchantPlaceCabinetVO> placeCabinetVOList = this.listCabinetByPlaceId(merchantId, merchantId);
            cabinetIdList = placeCabinetVOList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
            
            if (CollectionUtils.isNotEmpty(cabinetIdList) && cabinetIdList.contains(cabinetId)) {
                cabinetIdList = List.of(cabinetId);
            }
        }
        
        if (CollectionUtils.isEmpty(cabinetIdList)) {
            return Collections.emptyList();
        }
        
        return cabinetIdList;
    }
    
}

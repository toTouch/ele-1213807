package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElePower;
import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.EleSumPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetDayPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 商户/场地/下 柜机电量/电费
 * @date 2024/2/20 19:14:12
 */
@Service
public class MerchantCabinetPowerServiceImpl implements MerchantCabinetPowerService {
    
    @Resource
    MerchantPlaceBindService merchantPlaceBindService;
    
    @Resource
    private ElePowerService elePowerService;
    
    @Slave
    @Override
    public MerchantCabinetDayPowerVO todayPowerAndCharge(MerchantCabinetPowerRequest request) {
        Long merchantId = request.getMerchantId();
        if (Objects.isNull(merchantId)) {
            return null;
        }
        
        Long placeId = request.getPlaceId();
        Long cabinetId = request.getCabinetId();
        List<Long> cabinetIdList = null;
        // 1.场地和柜机为null，查全量
        if (Objects.isNull(placeId) && Objects.isNull(cabinetId)) {
            MerchantPlaceAndCabinetUserVO merchantPlaceAndCabinetUserVO = merchantPlaceBindService.listPlaceAndCabinetByMerchantId(merchantId);
            if (Objects.isNull(merchantPlaceAndCabinetUserVO) || CollectionUtils.isEmpty(merchantPlaceAndCabinetUserVO.getCabinetList())) {
                return null;
            }
    
            List<MerchantPlaceCabinetVO> cabinetList = merchantPlaceAndCabinetUserVO.getCabinetList();
            cabinetIdList = cabinetList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        // 2.场地不为null，柜机为null
        if (Objects.nonNull(placeId) && Objects.isNull(cabinetId)) {
            List<MerchantPlaceCabinetVO> placeCabinetVOList = merchantPlaceBindService.listCabinetByPlaceId(merchantId, merchantId);
            cabinetIdList = placeCabinetVOList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
    
        if (CollectionUtils.isEmpty(cabinetIdList)) {
            return null;
        }
        
        long todayStartTime = DateUtils.getTodayStartTime();
        EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(todayStartTime, System.currentTimeMillis(), cabinetIdList, TenantContextHolder.getTenantId());
        if (Objects.isNull(eleSumPowerVO)) {
            return null;
        }
    
        // 3.封装数据
        MerchantCabinetDayPowerVO merchantCabinetDayPowerVO = new MerchantCabinetDayPowerVO();
        BeanUtils.copyProperties(eleSumPowerVO, merchantCabinetDayPowerVO);
    
        return merchantCabinetDayPowerVO;
    }
}

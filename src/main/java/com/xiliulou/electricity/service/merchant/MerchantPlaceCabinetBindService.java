package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetBindSaveRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetConditionRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetPageRequest;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindTimeCheckVo;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/11 9:52
 * @desc 场地柜机绑定
 */
public interface MerchantPlaceCabinetBindService {
    
    List<MerchantPlaceCabinetBind> queryList(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
    
    List<MerchantPlaceCabinetBindVO> queryListByMerchantId(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
    
    List<MerchantPlaceCabinetBindVO> queryBindCabinetName(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
    
    Triple<Boolean, String, Object> bind(MerchantPlaceCabinetBindSaveRequest placeCabinetBindSaveRequest);
    
    Triple<Boolean, String, Object> unBind(MerchantPlaceCabinetBindSaveRequest placeCabinetBindSaveRequest);
    
    Triple<Boolean, String, Object> remove(Long id, List<Long> franchiseeId);
    
    Integer countTotal(MerchantPlaceCabinetPageRequest placeCabinetPageRequest);
    
    List<MerchantPlaceCabinetBindVO> listByPage(MerchantPlaceCabinetPageRequest placeCabinetPageRequest);
    
    Integer removeByPlaceId(Long placeId, long updateTime, Integer delFlag);
    
    MerchantPlaceCabinetBindTimeCheckVo checkBindTime(Long placeId, Long time, Integer cabinetId);
    
    List<MerchantPlaceCabinetBind> queryListByPlaceId(List<Long> placeIdList, Integer placeMonthNotSettlement);
    
    Integer checkIsBindByPlaceId(Long placeId, Long cabinetId);
    
    List<MerchantPlaceCabinetBind> listByPlaceIds(Set<Long> placeId);
    
    List<MerchantPlaceCabinetBind> listBindRecord(MerchantPlaceCabinetConditionRequest request);
    
    List<MerchantPlaceCabinetBind> listUnbindRecord(MerchantPlaceCabinetConditionRequest request);
}

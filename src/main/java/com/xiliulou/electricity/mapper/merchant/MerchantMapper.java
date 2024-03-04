package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeMerchantNumQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 11:06
 * @desc
 */

public interface MerchantMapper {
    
    Integer existsByAreaId(Long areaId);
    
    int insert(Merchant merchant);
    
    Merchant selectById(@Param("id") Long id);
    
    int update(Merchant merchantUpdate);
    
    int removeById(Merchant deleteMerchant);
    
    List<Merchant> selectListByPage(MerchantQueryModel queryModel);
    
    Integer countTotal(MerchantQueryModel merchantQueryModel);
    
    Merchant selectByUid(@Param("uid") Long uid);
    
    List<Merchant> selectByChannelEmployeeUid(@Param("channelEmployeeUid") Long channelEmployeeUid);
    
    Integer updateById(Merchant merchant);
    
    Integer existsByName(@Param("name") String name, @Param("tenantId") Integer tenantId, @Param("id") Long id);
    
    Integer countMerchantNumByTime(MerchantPromotionFeeMerchantNumQueryModel queryModel);
    
    Integer batchUpdateExistPlaceFee(@Param("merchantIdList") List<Long> merchantIdList,@Param("existsPlaceFee") Integer existsPlaceFee,@Param("updateTime") Long updateTime);
}

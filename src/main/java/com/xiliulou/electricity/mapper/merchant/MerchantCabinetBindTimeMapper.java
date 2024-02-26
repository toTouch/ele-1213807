package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantCabinetBindTime;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/26 13:00
 * @desc
 */
public interface MerchantCabinetBindTimeMapper {
    
    Integer batchInsert(@Param("list") List<MerchantCabinetBindTime> list);
    
    Integer updateById(MerchantCabinetBindTime merchantCabinetBindTime);
    
    List<MerchantCabinetBindTime> queryListByMerchantId(@Param("merchantId") Long merchantId);
}

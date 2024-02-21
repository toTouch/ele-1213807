package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/11 9:54
 * @desc
 */
public interface MerchantPlaceCabinetBindMapper {
    
    List<MerchantPlaceCabinetBind> list(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
    
    List<MerchantPlaceCabinetBindVO> listByMerchantId(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
    
    List<MerchantPlaceCabinetBindVO> queryBindCabinetName(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
    
    Integer insert(MerchantPlaceCabinetBind placeCabinetBind);
    
    MerchantPlaceCabinetBind selectById(Long id);
    
    Integer unBind(MerchantPlaceCabinetBind unBind);
    
    Integer remove(MerchantPlaceCabinetBind update);
    
    Integer countTotal(MerchantPlaceCabinetBindQueryModel merchantQueryModel);
    
    List<MerchantPlaceCabinetBind> selectListByPage(MerchantPlaceCabinetBindQueryModel merchantQueryModel);
    
    List<MerchantPlaceCabinetBind> selectListByConditions(@Param("placeIds")Set<Long> placeIds, @Param("cabinetIds") Set<Long> cabinetIds, @Param("status") Integer status, @Param("startTime") Long startTime, @Param("endTime") Long endTime);
    
}

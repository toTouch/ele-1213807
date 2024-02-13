package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.query.merchant.MerchantQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 11:06
 * @desc
 */

public interface MerchantMapper {
    
    int insert(Merchant merchant);
    
    Merchant select(@Param("id") Long id);
    
    int update(Merchant merchantUpdate);
    
    int deleteById(Merchant deleteMerchant);
    
    List<Merchant> selectListByPage(MerchantQueryModel queryModel);
    
    Integer countTotal(MerchantQueryModel merchantQueryModel);
}

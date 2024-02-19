package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantEmployee;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/18 21:24
 */
public interface MerchantEmployeeMapper {
    
    MerchantEmployee selectById(Long id);
  
    Integer insertOne(MerchantEmployee merchantEmployee);
    
    Integer updateOne(MerchantEmployee merchantEmployee);
    
    Integer removeById(Long id);
    
}

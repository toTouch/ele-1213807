package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantEmployee;
import com.xiliulou.electricity.request.merchant.MerchantEmployeeRequest;
import com.xiliulou.electricity.vo.merchant.MerchantEmployeeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/18 21:24
 */
public interface MerchantEmployeeMapper {
    MerchantEmployee selectById(@Param("id") Long id);
    
    MerchantEmployee selectByUid(@Param("id") Long id);
    List<MerchantEmployeeVO> selectListByCondition(MerchantEmployeeRequest merchantEmployeeRequest);
    Integer countByCondition(MerchantEmployeeRequest merchantEmployeeRequest);
  
    Integer insertOne(MerchantEmployee merchantEmployee);
    
    Integer updateOne(MerchantEmployee merchantEmployee);
    
    Integer removeById(@Param("id") Long id);
    
}

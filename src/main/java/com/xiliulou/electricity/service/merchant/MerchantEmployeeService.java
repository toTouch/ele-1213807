package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.request.merchant.MerchantEmployeeRequest;
import com.xiliulou.electricity.vo.merchant.MerchantEmployeeVO;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/19 9:37
 */
public interface MerchantEmployeeService {
    
    Integer saveMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest);
    
    Integer updateMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest);
    
    Integer removeMerchantEmployee(Long id);
    
    MerchantEmployeeVO queryMerchantEmployeeById(Long id);
    
    MerchantEmployeeVO queryMerchantEmployeeByUid(Long uid);
    
    List<MerchantEmployeeVO> listMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest);
    
    Integer countMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest);
    
}

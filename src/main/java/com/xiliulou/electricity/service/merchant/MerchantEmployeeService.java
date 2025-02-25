package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.bo.merchant.MerchantEmployeeBO;
import com.xiliulou.electricity.entity.merchant.MerchantEmployee;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantEmployeeRequest;
import com.xiliulou.electricity.vo.merchant.MerchantEmployeeQrCodeVO;
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
    
    Integer batchUnbindPlaceId(List<Long> employeeUidList);
    
    Integer removeMerchantEmployee(Long id);
    
    MerchantEmployeeVO queryMerchantEmployeeById(Long id);
    
    MerchantEmployeeVO queryMerchantEmployeeByUid(Long uid);
    
    MerchantEmployeeQrCodeVO queryEmployeeQrCodeByUid(Long uid);
    
    List<MerchantEmployeeVO> listMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest);
    
    Integer countMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest);
    
    List<MerchantEmployee> queryListByPlaceId(List<Long> placeIdList);
    
    List<MerchantEmployee> selectByMerchantUid(MerchantPromotionEmployeeDetailQueryModel queryModel);
    
    List<MerchantEmployeeQrCodeVO> selectMerchantEmployeeQrCodes(MerchantEmployeeRequest merchantEmployeeRequest);
    
    List<MerchantEmployeeVO> selectAllMerchantEmployees(MerchantEmployeeRequest merchantEmployeeRequest);
    
    Integer batchRemoveByUidList(List<Long> employeeUidList, Long timeMillis);
    
    List<MerchantEmployee> queryListByMerchantUid(Long merchantUid, Integer tenantId);

    List<MerchantEmployeeBO> listMerchantAndEmployeeInfoByUidList(List<Long> merchantEmployeesUidList);

    MerchantEmployeeBO queryMerchantAndEmployeeInfoByUid(Long uid);
}

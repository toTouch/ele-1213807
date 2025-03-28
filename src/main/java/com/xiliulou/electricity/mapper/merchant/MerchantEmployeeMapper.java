package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.bo.merchant.MerchantEmployeeBO;
import com.xiliulou.electricity.entity.merchant.MerchantEmployee;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailQueryModel;
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
    MerchantEmployeeVO selectById(@Param("id") Long id);
    
    MerchantEmployeeVO selectByUid(@Param("uid") Long uid);
    
    List<MerchantEmployeeVO> selectListByCondition(MerchantEmployeeRequest merchantEmployeeRequest);
    
    Integer countByCondition(MerchantEmployeeRequest merchantEmployeeRequest);
  
    Integer insertOne(MerchantEmployee merchantEmployee);
    
    Integer updateOne(MerchantEmployee merchantEmployee);
    
    Integer batchUnbindPlaceId(@Param("employeeUidList") List<Long> employeeUidList, @Param("updateTime") Long updateTime);
    
    Integer removeById(@Param("id") Long id, @Param("updateTime") Long updateTime);
    
    List<MerchantEmployee> selectListByPlaceId(@Param("placeIdList") List<Long> placeIdList);
    
    List<MerchantEmployee> selectListByMerchantUid(MerchantPromotionEmployeeDetailQueryModel queryModel);
    
    List<MerchantEmployeeVO> selectMerchantUsers(MerchantEmployeeRequest merchantEmployeeRequest);
    
    Integer batchRemoveByUidList(@Param("uidList") List<Long> uidList,@Param("updateTime") Long timeMillis);
    
    List<MerchantEmployee> selectListAllByMerchantUid(MerchantPromotionEmployeeDetailQueryModel queryModel);

    List<MerchantEmployeeBO> selectListMerchantAndEmployeeInfoByUidList(@Param("merchantEmployeesUidList") List<Long> merchantEmployeesUidList);

    MerchantEmployeeBO selectMerchantAndEmployeeInfoByUid(@Param("uid") Long uid);
}

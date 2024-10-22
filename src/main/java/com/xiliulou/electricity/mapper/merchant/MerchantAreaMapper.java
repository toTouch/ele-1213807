package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.query.merchant.MerchantAreaQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 区域管理
 * @date 2024/2/6 14:06:11
 */
public interface MerchantAreaMapper {
    
    Integer insertOne(MerchantArea merchantArea);
    
    Integer deleteById(@Param("id") Long id, @Param("tenantId") Integer tenantId);
    
    Integer existsByAreaName(@Param("areaName") String areaName, @Param("tenantId") Integer tenantId);
    
    Integer updateById(MerchantArea merchantArea);
    
    List<MerchantArea> selectPage(MerchantAreaQueryModel queryModel);
    
    Integer countTotal(MerchantAreaQueryModel queryModel);
    
    List<MerchantArea> queryList(MerchantAreaQueryModel queryModel);
    
    MerchantArea selectById(@Param("id") Long id);
    
    List<MerchantArea> selectListByIdList(@Param("idList") List<Long> areaIdList);
}

package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商户等级表(MerchantLevel)表数据库访问层
 *
 * @author zzlong
 * @since 2024-02-04 14:35:06
 */
public interface MerchantLevelMapper {
    
    MerchantLevel selectById(Long id);
    
    int insert(MerchantLevel merchantLevel);
    
    int batchInsert(List<MerchantLevel> merchantLevels);
    
    int updateById(MerchantLevel merchantLevel);
    
    int deleteByFranchiseeId(Long franchiseeId);
    
    MerchantLevel selectNextByMerchantLevel(@Param("level") String level, @Param("franchiseeId") Long franchiseeId);
    
    List<MerchantLevel> queryListByIdList(@Param("idList") List<Long> levelIdList);
    
    MerchantLevel selectLastByMerchantLevel(@Param("level") String level, @Param("franchiseeId") Long franchiseeId);
    
    MerchantLevel selectByMerchantLevelAndFranchiseeId(@Param("level") String level, @Param("franchiseeId") Long franchiseeId);
    
    Integer existsLevelName(@Param("name") String name, @Param("franchiseeId") Long franchiseeId);
    
    List<MerchantLevel> selectByFranchiseeId(@Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);
    
    List<MerchantLevel> selectListByTenantId(Integer tenantId);
}

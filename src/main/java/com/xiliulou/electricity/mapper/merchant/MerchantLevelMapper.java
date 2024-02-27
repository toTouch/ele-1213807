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

    List<MerchantLevel> selectByTenantId(Integer tenantId);

    int insert(MerchantLevel merchantLevel);
    
    int batchInsert(List<MerchantLevel> merchantLevels);

    int updateById(MerchantLevel merchantLevel);

    int deleteById(Long id);
    
    MerchantLevel selectNextByMerchantLevel(@Param("level") String level, @Param("tenantId") Integer tenantId);
    
    List<MerchantLevel> queryListByIdList(@Param("idList") List<Long> levelIdList);
    
    MerchantLevel selectLastByMerchantLevel(@Param("level") String level, @Param("tenantId") Integer tenantId);
}

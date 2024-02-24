package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.RebateConfig;

import java.util.List;

import com.xiliulou.electricity.request.merchant.RebateConfigRequest;
import org.apache.ibatis.annotations.Param;

/**
 * 返利配置表(RebateConfig)表数据库访问层
 *
 * @author zzlong
 * @since 2024-02-04 16:32:06
 */
public interface RebateConfigMapper {

    RebateConfig selectById(Long id);

    List<RebateConfig> selectByPage(RebateConfigRequest rebateConfigRequest);

    int insert(RebateConfig rebateConfig);

    int updateById(RebateConfig rebateConfig);

    int deleteById(Long id);
    
    Integer existsRebateConfigByMidAndLevel(@Param("mid") Long mid, @Param("level") String level);
    
    RebateConfig selectByMidAndMerchantLevel(@Param("mid") Long mid, @Param("level") String level);
}

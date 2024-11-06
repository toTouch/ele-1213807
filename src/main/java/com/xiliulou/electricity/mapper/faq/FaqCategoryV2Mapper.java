package com.xiliulou.electricity.mapper.faq;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.bo.faq.FaqV2BO;
import com.xiliulou.electricity.entity.faq.FaqCategoryV2;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 常见问题分类Mapper接口
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
public interface FaqCategoryV2Mapper extends BaseMapper<FaqCategoryV2> {
    
    int deleteByPrimaryKey(Long id);
    
    int insert(FaqCategoryV2 record);
    
    int insertSelective(FaqCategoryV2 record);
    
    FaqCategoryV2 selectByPrimaryKey(Long id);
    
    int updateByPrimaryKeySelective(FaqCategoryV2 record);
    
    int updateByPrimaryKey(FaqCategoryV2 record);
    
    List<FaqCategoryV2> selectListByTenantId(@Param(value = "tenantId") Integer tenantId);
    
    Integer countFaqCategoryByTenantId(@Param(value = "tenantId") Integer tenantId);
    
    List<FaqV2BO> selectLeftJoinByParams(@Param(value = "tenantId") Integer tenantId, @Param("title") String title, @Param("typeId") Integer typeId);
    
    Integer batchInsert(@Param("list") List<FaqCategoryV2> list);
}





package com.xiliulou.electricity.mapper.faq;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.faq.FaqCategory;
import com.xiliulou.electricity.vo.faq.FaqCategoryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 常见问题分类Mapper接口
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
public interface FaqCategoryMapper extends BaseMapper<FaqCategory> {
    
    int deleteByPrimaryKey(Long id);
    
    int insert(FaqCategory record);
    
    int insertSelective(FaqCategory record);
    
    FaqCategory selectByPrimaryKey(Long id);
    
    int updateByPrimaryKeySelective(FaqCategory record);
    
    int updateByPrimaryKey(FaqCategory record);
    
    List<FaqCategoryVo> selectListByTenantId(@Param(value = "tenantId") Integer tenantId);
    
}





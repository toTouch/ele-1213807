package com.xiliulou.electricity.mapper.faq;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.bo.faq.FaqV2BO;
import com.xiliulou.electricity.entity.faq.FaqV2;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 常见问题分类Mapper接口
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
public interface FaqV2Mapper extends BaseMapper<FaqV2> {
    
    int deleteByPrimaryKey(Long id);
    
    int insert(FaqV2 record);
    
    int insertSelective(FaqV2 record);
    
    FaqV2 selectByPrimaryKey(Long id);
    
    int updateByPrimaryKeySelective(FaqV2 record);
    
    int updateByPrimaryKey(FaqV2 record);
    
    List<FaqV2BO> selectLeftJoinByParams(@Param(value = "params") FaqV2 params);
    
    List<FaqV2> selectListByParams(@Param(value = "params") FaqV2 params);
    
    List<FaqV2> selectListByParamsPage(@Param(value = "params") FaqV2 params, @Param(value = "pageOffset") Object pageOffset, @Param(value = "pageSize") Object pageSize);
    
    int removeByIds(@Param(value = "ids") List<Long> ids);
    
    List<FaqV2> selectListByIds(List<Long> ids);
    
    Integer countFaqReqByTypeId(@Param("typeId") Long typeId);
    
    Integer batchUpdateByIds(@Param(value = "faqV2") FaqV2 faqV2, @Param(value = "ids") List<Long> ids);
    
    Integer selectMinimumSort(@Param(value = "tenantId") Integer tenantId);
    
    Integer batchInsert(@Param("list") List<FaqV2> list);
}





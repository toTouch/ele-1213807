package com.xiliulou.electricity.mapper.faq;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.faq.FaqV2;
import com.xiliulou.electricity.vo.faq.FaqVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
    
    List<FaqVo> selectLeftJoinByParams(@Param(value = "params") Map<String, Object> params);
    
    List<FaqV2> selectListByParams(@Param(value = "params") Map<String, Object> params);
    
    List<FaqV2> selectListByParamsPage(@Param(value = "params") Map<String, Object> params,
            @Param(value = "pageOffset") Object pageOffset,
            @Param(value = "pageSize") Object pageSize);
    
    int removeByIds(List<Long> ids);
}





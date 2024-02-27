package com.xiliulou.electricity.mapper.faq;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.faq.Faq;
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
public interface FaqMapper extends BaseMapper<Faq> {

    int deleteByPrimaryKey(Long id);

    int insert(Faq record);

    int insertSelective(Faq record);
    
    Faq selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Faq record);

    int updateByPrimaryKey(Faq record);
    
    List<FaqVo> selectLeftJoinByParams(@Param(value = "params") Map<String, Object> params);
    
    List<Faq> selectListByParams(@Param(value = "params") Map<String, Object> params);
    
    List<Faq> selectListByParamsPage(@Param(value = "params") Map<String, Object> params,
            @Param(value = "pageOffset") Object pageOffset,
            @Param(value = "pageSize") Object pageSize);
    
    int removeByIds(List<Long> ids);
}





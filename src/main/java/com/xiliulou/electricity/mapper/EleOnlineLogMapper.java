package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleOnlineLog;
import com.xiliulou.electricity.vo.ELeOnlineLogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 *
 * @author makejava
 * @since 2022-08-16 09:28:22
 */
public interface EleOnlineLogMapper extends BaseMapper<EleOnlineLog>{
    
    
    List<ELeOnlineLogVO> queryOnlineLogList(@Param("size") Integer size, @Param("offset") Integer offset,
            @Param("type") String type, @Param("eleId") Integer eleId, @Param("tenantId") Integer tenantId);


    Integer queryOnlineLogCount(@Param("type") String type, @Param("eleId") Integer eleId);

}

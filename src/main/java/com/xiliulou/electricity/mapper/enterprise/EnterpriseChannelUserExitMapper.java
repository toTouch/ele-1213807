package com.xiliulou.electricity.mapper.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUserExit;
import com.xiliulou.electricity.queryModel.enterprise.EnterpriseChannelUserExitQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/1/11 9:30
 * @desc
 */
public interface EnterpriseChannelUserExitMapper extends BaseMapper<EnterpriseChannelUserExit> {
    int insertOne(EnterpriseChannelUserExit channelUserHistory);
    
    int batchInsert(@Param("list") List<EnterpriseChannelUserExit> list);
    
    void updateById(@Param("errorMsg") String errorMsg,@Param("type") Integer type,@Param("id") Long id, @Param("updateTime") Long updateTime);
    
    List<EnterpriseChannelUserExit> list(EnterpriseChannelUserExitQueryModel queryModel);
    
    void batchUpdateById(@Param("errorMsg") String errorMsg,@Param("type") Integer type,@Param("idList") List<Long> idList, @Param("updateTime") Long updateTime);
}

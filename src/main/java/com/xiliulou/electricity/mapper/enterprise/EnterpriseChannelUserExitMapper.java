package com.xiliulou.electricity.mapper.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUserExit;
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
}

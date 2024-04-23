package com.xiliulou.electricity.mapper.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUserHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/1/11 9:30
 * @desc
 */
public interface EnterpriseChannelUserHistoryMapper extends BaseMapper<EnterpriseChannelUserHistory> {
    int insertOne(EnterpriseChannelUserHistory channelUserHistory);
    
    int batchInsert(@Param("list") List<EnterpriseChannelUserHistory> list);
}

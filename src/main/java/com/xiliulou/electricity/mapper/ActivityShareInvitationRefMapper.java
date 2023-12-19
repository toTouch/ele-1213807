package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ActivityShareInvitationRef;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author HeYafeng
 * @description 邀请返券活动和套餐返现活动映射 悦来能源2023/12需求特殊处理：邀请返券的数据迁移到邀请返现，并在扫码相关邀请人的返券二维码时跳转到套餐返券逻辑
 * @date 2023/12/19 10:46:21
 */

@Mapper
public interface ActivityShareInvitationRefMapper {
    
    ActivityShareInvitationRef selectByInviterAndShareActivityId(@Param("tenantId") Integer tenantId, @Param("inviterUid") Long inviterUid,
            @Param("shareActivityId") Long shareActivityId);
}

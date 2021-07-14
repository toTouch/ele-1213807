package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ShareActivityRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 发起邀请活动记录(ShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
public interface ShareActivityRecordMapper  extends BaseMapper<ShareActivityRecord>{



    /**
     * 通过实体作为筛选条件查询
     *
     * @param shareActivityRecord 实例对象
     * @return 对象列表
     */
    List<ShareActivityRecord> queryAll(ShareActivityRecord shareActivityRecord);



}

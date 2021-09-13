package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.StoreSplitAccountHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * (StoreSplitAccountHistory)表数据库访问层
 *
 * @author makejava
 * @since 2021-05-06 20:09:27
 */
public interface StoreSplitAccountHistoryMapper extends BaseMapper<StoreSplitAccountHistory> {

    @Select("select sum(split_amount) from t_store_split_account_history where store_id = #{storeId} and create_time between #{startTime} and #{endTime}")
    Double querySumPayAmountByCondition(@Param("storeId") Long storeId, @Param("startTime") long startTime, @Param("endTime") long endTime);

    List<StoreSplitAccountHistory> queryListByCondition(@Param("size") Integer size, @Param("offset") Integer offset, @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("storeId") Long storeId, @Param("tenantId") Integer tenantId);
}

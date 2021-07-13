package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ShareActivityRule;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * 加盟商活动绑定表(TActivityBindCoupon)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-23 16:43:23
 */
public interface ShareActivityRuleMapper extends BaseMapper<ShareActivityRule>{


    /**
     * 查询指定行数据
     *
     */
    List<ShareActivityRule> queryList(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param shareActivityRule 实例对象
     * @return 对象列表
     */
    List<ShareActivityRule> queryAll(ShareActivityRule shareActivityRule);

    /**
     * 新增数据
     *
     * @param shareActivityRule 实例对象
     * @return 影响行数
     */
    int insertOne(ShareActivityRule shareActivityRule);

    /**
     * 修改数据
     *
     * @param shareActivityRule 实例对象
     * @return 影响行数
     */
    int update(ShareActivityRule shareActivityRule);


    @Update("update  t_activity_bind_coupon set status=2,update_time=#{currentTime} where activity_id = #{id} and status = 1 and del_flg = 0 ")
    void updateByActivity(Integer id, long currentTimeMillis);
}

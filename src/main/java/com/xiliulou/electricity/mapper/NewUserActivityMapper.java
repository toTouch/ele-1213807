package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.NewUserActivity;
import com.xiliulou.electricity.query.NewUserActivityPageQuery;
import com.xiliulou.electricity.query.NewUserActivityQuery;
import com.xiliulou.electricity.vo.ShareAndUserActivityVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活动表(TActivity)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface NewUserActivityMapper extends BaseMapper<NewUserActivity>{



    /**
     * 查询指定行数据
     *
     */
    List<NewUserActivity> queryList(@Param("query") NewUserActivityQuery newUserActivityQuery);


    Integer queryCount(@Param("query") NewUserActivityQuery newUserActivityQuery);

    NewUserActivity selectByCouponId(@Param("couponId") Long couponId);
    
    /**
     * <p>
     *    Description: removeById
     *    9. 活动管理-套餐返现活动里面的套餐配置记录想能够手动删除
     * </p>
     * @param id id 主键id
     * @return com.xiliulou.core.web.R<?>
     * <p>Project: saas-electricity</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#UH1YdEuCwojVzFxtiK6c3jltneb"></a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/14
     */
    int removeById(@Param("id") Long id,@Param("tenantId") Long tenantId);
    
    List<ShareAndUserActivityVO> listNewUserActivity(NewUserActivityPageQuery query);
}

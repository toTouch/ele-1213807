package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.query.ShareActivityPageQuery;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.vo.ShareAndUserActivityVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动表(TActivity)表数据库访问层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
public interface ShareActivityMapper extends BaseMapper<ShareActivity>{



    /**
     * 查询指定行数据
     *
     */
    List<ShareActivity> queryList(@Param("query") ShareActivityQuery shareActivityQuery);


    Integer queryCount(@Param("query") ShareActivityQuery shareActivityQuery);

    @Select("select  id, name, type, status,  description, del_flg, create_time,\n" +
            "        update_time, uid,user_name\n" +
            "        from t_share_activity where end_time < #{currentTime} and status = 1 and del_flg = 0 ")
    List<ShareActivity> getExpiredActivity(long currentTimeMillis);
    
    Integer existShareActivity(@Param("tenantId") Integer tenantId);
    
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
    
    List<ShareActivity> selectOnlineActivity(@Param("tenantId") Integer tenantId);
    
    List<ShareAndUserActivityVO> listShareActivity(ShareActivityPageQuery query);
}

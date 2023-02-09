package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.query.ShareActivityRecordQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 发起邀请活动记录(ShareActivityRecord)表服务接口
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
public interface ShareActivityRecordService {


      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivityRecord queryByIdFromDB(Long id);


    /**
     * 新增数据
     *
     * @param shareActivityRecord 实例对象
     * @return 实例对象
     */
    ShareActivityRecord insert(ShareActivityRecord shareActivityRecord);

    /**
     * 修改数据
     *
     * @param shareActivityRecord 实例对象
     * @return 实例对象
     */
    Integer update(ShareActivityRecord shareActivityRecord);


	R generateSharePicture(Integer activityId,String page);


	ShareActivityRecord queryByUid(Long uid,Integer activityId);


	void addCountByUid(Long uid);

	void reduceAvailableCountByUid(Long uid, Integer count);

	R queryList(ShareActivityRecordQuery shareActivityRecordQuery);

	R queryCount(ShareActivityRecordQuery shareActivityRecordQuery);
    
    void shareActivityRecordExportExcel(ShareActivityRecordQuery shareActivityRecordQuery,
            HttpServletResponse response);
}

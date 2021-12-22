package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ShareMoneyActivityRecord;
import com.xiliulou.electricity.query.ShareMoneyActivityRecordQuery;

import java.math.BigDecimal;

/**
 * 发起邀请活动记录(ShareActivityRecord)表服务接口
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
public interface ShareMoneyActivityRecordService {


      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
      ShareMoneyActivityRecord queryByIdFromDB(Long id);


    /**
     * 新增数据
     *
     * @param shareMoneyActivityRecord 实例对象
     * @return 实例对象
     */
    ShareMoneyActivityRecord insert(ShareMoneyActivityRecord shareMoneyActivityRecord);

    /**
     * 修改数据
     *
     * @param shareMoneyActivityRecord 实例对象
     * @return 实例对象
     */
    Integer update(ShareMoneyActivityRecord shareMoneyActivityRecord);


	R generateSharePicture(Integer activityId,String page);


	ShareMoneyActivityRecord queryByUid(Long uid,Integer activityId);


	void addCountByUid(Long uid, BigDecimal money);


	R queryList(ShareMoneyActivityRecordQuery shareMoneyActivityRecordQuery);

	R queryCount(ShareMoneyActivityRecordQuery shareMoneyActivityRecordQuery);
}

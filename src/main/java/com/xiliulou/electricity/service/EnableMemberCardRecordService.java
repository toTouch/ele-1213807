package com.xiliulou.electricity.service;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.EnableMemberCardRecord;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.query.EnableMemberCardRecordQuery;

/**
 * 启用套餐(TEnableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
public interface EnableMemberCardRecordService {


    /**
     * 新增数据
     *
     * @param enableMemberCardRecord 实例对象
     * @return 实例对象
     */
    R insert(EnableMemberCardRecord enableMemberCardRecord);

    /**
     * 修改数据
     *
     * @param enableMemberCardRecord 实例对象
     * @return 实例对象
     */
    Integer update(EnableMemberCardRecord enableMemberCardRecord);



    R queryList(EnableMemberCardRecordQuery enableMemberCardRecordQuery);



    R queryCount(EnableMemberCardRecordQuery enableMemberCardRecordQuery);

    EnableMemberCardRecord queryByDisableCardNO(String disableCardNO,Integer tenantId);

    EnableMemberCardRecord selectLatestByUid(Long uid);
}

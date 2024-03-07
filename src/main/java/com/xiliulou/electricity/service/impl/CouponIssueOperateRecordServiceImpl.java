package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.CouponIssueOperateRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.CouponIssueOperateRecordMapper;
import com.xiliulou.electricity.query.CouponIssueOperateRecordQuery;
import com.xiliulou.electricity.service.CouponIssueOperateRecordService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.vo.CouponIssueOperateRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 优惠券规则表(t_coupon_issue_operate_record)表服务接口
 *
 * @author makejava
 * @since 2022-08-19 09:28:22
 */
@Service("couponIssueOperateRecordService")
@Slf4j
public class CouponIssueOperateRecordServiceImpl implements CouponIssueOperateRecordService {


    @Resource
    CouponIssueOperateRecordMapper couponIssueOperateRecordMapper;
    
    @Autowired
    private UserService userService;

    @Override
    public void insert(CouponIssueOperateRecord couponIssueOperateRecord) {
        couponIssueOperateRecordMapper.insert(couponIssueOperateRecord);
    }

    @Deprecated
    @Slave
    @Override
    public R queryList(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        return R.ok(couponIssueOperateRecordMapper.queryList(couponIssueOperateRecordQuery));
    }

    @Deprecated
    @Slave
    @Override
    public R queryCount(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        return R.ok(couponIssueOperateRecordMapper.queryCount(couponIssueOperateRecordQuery));
    }
    @Slave
    @Override
    public R queryRecordList(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        List<CouponIssueOperateRecordVO> operateRecordVOS = couponIssueOperateRecordMapper.queryRecordList(couponIssueOperateRecordQuery);
        if (CollectionUtils.isEmpty(operateRecordVOS)) {
            return R.ok(operateRecordVOS);
        }
        //*********************************查询优惠劵发放人*************************/
        operateRecordVOS.forEach(n->{
            Long issuedUid = n.getIssuedUid();
            User user = userService.queryByUidFromCache(issuedUid);
            n.setIssuedName(ObjectUtil.isNull(user)?null:user.getName());
        });
        //******************************优惠劵发放人查询完毕*************************/
        return R.ok(operateRecordVOS);
    }
    @Slave
    @Override
    public R queryRecordCount(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        return R.ok(couponIssueOperateRecordMapper.queryRecordCount(couponIssueOperateRecordQuery));
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid,String newPhone) {
        return couponIssueOperateRecordMapper.updatePhoneByUid(tenantId,uid,newPhone);
    }

    @Override
    public Integer batchInsert(List<CouponIssueOperateRecord> couponIssueOperateRecords) {
        return couponIssueOperateRecordMapper.batchInsert(couponIssueOperateRecords);
    }
}

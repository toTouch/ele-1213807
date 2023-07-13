package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.query.CouponQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户停卡绑定(TServiceFeeUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
public interface ServiceFeeUserInfoMapper extends BaseMapper<ServiceFeeUserInfo>{


    int update(ServiceFeeUserInfo serviceFeeUserInfo);

    int updateByUid(ServiceFeeUserInfo serviceFeeUserInfo);

    int deleteByUid(Long uid);
}

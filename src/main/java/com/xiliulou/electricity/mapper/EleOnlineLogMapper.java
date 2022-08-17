package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.EleOnlineLog;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.vo.ELeOnlineLogVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 优惠券规则表(TEleOnLineLog)表服务接口
 *
 * @author makejava
 * @since 2022-08-16 09:28:22
 */
public interface EleOnlineLogMapper extends BaseMapper<EleOnlineLog>{


    List<ELeOnlineLogVO> queryOnlineLogList(@Param("size") Integer size, @Param("offset") Integer offset,
                                            @Param("type") String type, @Param("eleId") Integer eleId);


    Integer queryOnlineLogCount(@Param("type") String type, @Param("eleId") Integer eleId);

}

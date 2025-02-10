package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜保险订单(InsuranceOrder)表数据库访问层
 *
 * @author makejava
 * @since 2022-11-03 14:44:12
 */
public interface InsuranceOrderMapper extends BaseMapper<InsuranceOrder> {

    List<InsuranceOrderVO> queryList(@Param("query") InsuranceOrderQuery insuranceOrderQuery);

    Integer queryCount(@Param("query") InsuranceOrderQuery insuranceOrderQuery);

    int updateIsUseByOrderId(InsuranceOrder insuranceOrder);

    Integer updateUseStatusByOrderId(@Param("orderId") String orderId, @Param("useStatus") Integer useStatus);

    Integer updateUseStatusForRefund(@Param("orderId") String orderId, @Param("useStatus") Integer useStatus);
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);

    InsuranceOrder selectByUid(@Param("uid") Long uid, @Param("type") Integer type, @Param("status") Integer status);

    List<InsuranceOrder> selectListByUid(@Param("uid") Long uid, @Param("type") Integer type);
}

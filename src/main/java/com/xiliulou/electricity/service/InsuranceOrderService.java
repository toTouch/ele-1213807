package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 换电柜保险订单(InsuranceOrder)表服务接口
 *
 * @author makejava
 * @since 2022-11-03 13:37:11
 */
public interface InsuranceOrderService {

    /**
     * 根据来源订单编码、类型查询保险订单信息
     * @param sourceOrderNo 来源订单编码
     * @param insuranceType 类型：0-电、1-车、2-车电
     * @return 保险订单
     */
    InsuranceOrder selectBySourceOrderNoAndType(String sourceOrderNo, Integer insuranceType);

    R queryList(InsuranceOrderQuery insuranceOrderQuery);

    R queryCount(InsuranceOrderQuery insuranceOrderQuery);

    R createOrder(InsuranceOrderAdd insuranceOrderAdd, HttpServletRequest request);

    InsuranceOrder queryByOrderId(String orderNo);

    Integer updateOrderStatusById(InsuranceOrder insuranceOrder);

    int updateIsUseByOrderId(InsuranceOrder insuranceOrder);

    R queryInsurance();

    R homeOneQueryInsurance(Integer model,Long franchiseeId);

    void insert(InsuranceOrder insuranceOrder);

    Triple<Boolean, String, Object> handleRentBatteryInsurance(Integer insuranceId, UserInfo userInfo);

    List<InsuranceOrderVO> queryListByStatus(InsuranceOrderQuery insuranceOrderQuery);
}

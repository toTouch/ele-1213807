package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.FreeDepositExpireRecord;
import com.xiliulou.electricity.query.FreeDepositExpireRecordQuery;
import com.xiliulou.electricity.vo.FreeDepositExpireRecordVO;

import java.util.List;

/**
 * @Description: FreeDepositExpireRecordService
 * @Author: RenHang
 * @Date: 2025/02/25
 */

public interface FreeDepositExpireRecordService {

    /**
     * page
     *
     * @param query query
     * @return: @return {@link List }<{@link FreeDepositExpireRecordVO }>
     */

    List<FreeDepositExpireRecordVO> selectByPage(FreeDepositExpireRecordQuery query);

    /**
     * count
     *
     * @param query query
     * @return: @return {@link Integer }
     */

    Integer queryCount(FreeDepositExpireRecordQuery query);

    /**
     * offLineDeal
     *
     * @param id id
     * @return:
     */

    void offLineDeal(List<Long> id);

    /**
     * editRemark
     *
     * @param id id
     * @param remark remark
     * @return:
     */

    void editRemark(Long id, String remark);

    /**
     * queryByOrderId
     *
     * @param orderId orderId
     * @return: @return {@link FreeDepositExpireRecord }
     */

    FreeDepositExpireRecord queryByOrderId(String orderId);

    /**
     * deleteById
     *
     * @param id id
     * @return:
     */

    void deleteById(Long id);

    /**
     * 解冻后移除
     *
     * @param orderId orderId
     * @return:
     */

    void unfreeAfterDel(String orderId);
}

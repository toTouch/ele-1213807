package com.xiliulou.electricity.service;


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
}

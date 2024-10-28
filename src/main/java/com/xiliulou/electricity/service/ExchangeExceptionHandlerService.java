package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @ClassName: ExchangeExceptionHandlerService
 * @description:
 * @author: renhang
 * @create: 2024-09-29 15:12
 */
public interface ExchangeExceptionHandlerService {
    
    /**
     * 保存换电异常柜机格挡号
     *
     * @param orderStatus 订单状态
     * @param eid         柜机id
     * @param oldCell     旧仓门
     * @param newCell     新仓门
     * @param sessionId   sessionId
     */
    void saveExchangeExceptionCell(String orderStatus, Integer eid, Integer oldCell, Integer newCell, String sessionId);
    
    /**
     * 保存租借异常柜机格挡号
     *
     * @param orderStatus 订单状态
     * @param eid         柜机id
     * @param cellNo      仓门
     * @param sessionId   sessionId
     */
    void saveRentReturnExceptionCell(String orderStatus, Integer eid, Integer cellNo, String sessionId);
    
    /**
     * 过滤掉异常空闲的格挡号
     *
     * @param eid       eid
     * @param emptyList emptyList
     * @return Pair
     */
    Pair<Boolean, List<ElectricityCabinetBox>> filterEmptyExceptionCell(Integer eid, List<ElectricityCabinetBox> emptyList);
    
    /**
     * 过滤掉异常满电的格挡号
     *
     * @param fullList fullList
     * @return
     */
    Pair<Boolean, List<ElectricityCabinetBox>> filterFullExceptionCell(List<ElectricityCabinetBox> fullList);
}

package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetBoxLock;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.exchange.ElectricityCabinetBoxLockPageQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxLockPageVO;

import java.util.List;

/**
 * 换电柜仓门表(TElectricityCabinetBoxLock)表服务接口
 *
 * @author renhang
 */
public interface ElectricityCabinetBoxLockService {

    /**
     * insertElectricityCabinetBoxLock
     *
     * @param electricityCabinetBoxLock electricityCabinetBoxLock
     */
    void insertElectricityCabinetBoxLock(ElectricityCabinetBoxLock electricityCabinetBoxLock);

    /**
     * updateElectricityCabinetBoxLock
     *
     * @param eid    eid
     * @param cellNo cellNo
     */
    void updateElectricityCabinetBoxLock(Integer eid, String cellNo);

    /**
     * queryList
     *
     * @param query query
     * @return List
     */
    List<ElectricityCabinetBoxLockPageVO> queryList(ElectricityCabinetBoxLockPageQuery query);

    /**
     * queryCount
     *
     * @param query query
     * @return Long
     */
    Long queryCount(ElectricityCabinetBoxLockPageQuery query);

    /**
     * 启用仓门
     *
     * @param eleOuterCommandQuery eleOuterCommandQuery
     * @return R
     */
    R enableBoxCell(EleOuterCommandQuery eleOuterCommandQuery);
}

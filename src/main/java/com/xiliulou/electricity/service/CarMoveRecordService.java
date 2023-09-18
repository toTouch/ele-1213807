package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.CarMoveRecord;
import com.xiliulou.electricity.query.CarMoveRecordQuery;
import com.xiliulou.electricity.vo.CarMoveRecordVO;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/15 14:48
 * @Description:
 */
public interface CarMoveRecordService {

    List<CarMoveRecordVO> queryCarMoveRecords(CarMoveRecordQuery carMoveRecordQuery);

    Integer queryCarMoveRecordsCount(CarMoveRecordQuery carMoveRecordQuery);


}

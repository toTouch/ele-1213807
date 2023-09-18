package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.CarMoveRecord;
import com.xiliulou.electricity.query.CarMoveRecordQuery;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/15 14:04
 * @Description:
 */
public interface CarMoveRecordMapper {
    Integer insertCarMoveRecord(CarMoveRecord carMoveRecord);
    Integer batchInsertCarMoveRecord(List<CarMoveRecord> carMoveRecordList);

    List<CarMoveRecord> selectByPage(CarMoveRecordQuery carMoveRecordQuery);

    Integer selectCount(CarMoveRecordQuery carMoveRecordQuery);

}

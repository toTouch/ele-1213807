package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.NotExistSn;
import com.xiliulou.electricity.query.NotExistSnQuery;

/**
 * (Faq)表服务接口
 *
 * @author makejava
 * @since 2021-09-26 14:06:23
 */
public interface NotExistSnService {

    void insert(NotExistSn notExistSn);


    void update(NotExistSn notExistSn);


    NotExistSn queryByOther(String batteryName,Integer electricityCabinetId,Integer cellNo);

    NotExistSn queryByBatteryName(String batteryName);


    R queryList(NotExistSnQuery notExistSnQuery);

    R queryCount(NotExistSnQuery notExistSnQuery);

	NotExistSn queryByIdFromDB(Long id);

    void delete(Long id);
}

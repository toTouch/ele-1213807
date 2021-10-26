package com.xiliulou.electricity.service;
import com.xiliulou.electricity.entity.SplitAccountFailRecord;

import java.util.List;

/**
 * (SplitAccountFailRecord)表服务接口
 *
 * @author makejava
 * @since 2021-05-07 08:10:06
 */
public interface SplitAccountFailRecordService {

    /**
     * 新增数据
     *
     * @param splitAccountFailRecord 实例对象
     * @return 实例对象
     */
    SplitAccountFailRecord insert(SplitAccountFailRecord splitAccountFailRecord);


}

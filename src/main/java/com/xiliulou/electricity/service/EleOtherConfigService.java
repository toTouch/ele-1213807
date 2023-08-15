package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleOtherConfig;


/**
 * (CupboardOtherConfig)表服务接口
 *
 * @author Hardy
 * @since 2021-07-21 15:22:56
 */
public interface EleOtherConfigService {


    /**
     * 新增数据
     *
     * @param eleOtherConfig 实例对象
     * @return 实例对象
     */
    Integer insert(EleOtherConfig eleOtherConfig);

    /**
     * 修改数据
     *
     * @param eleOtherConfig 实例对象
     * @return 实例对象
     */
    Integer update(EleOtherConfig eleOtherConfig);


    EleOtherConfig queryByEidFromCache(Integer eid);

    R updateEleOtherConfig(EleOtherConfig eleOtherConfig);

    Integer updateByEid(EleOtherConfig eleOtherConfig);
}

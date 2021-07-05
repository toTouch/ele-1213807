package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.utils.SecurityUtils;

import java.util.List;

/**
 * 实名认证资料项(TEleAuthEntry)表服务接口
 *
 * @author makejava
 * @since 2021-02-20 13:37:11
 */
public interface EleAuthEntryService {

      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleAuthEntry queryByIdFromCache(Integer id);


    R updateEleAuthEntries(List<EleAuthEntry> eleAuthEntryList);

    List<EleAuthEntry> getEleAuthEntriesList(Integer tenantId);

    List<EleAuthEntry> getUseEleAuthEntriesList(Integer tenantId);

    void insertByTenantId(Integer tenantId);

}

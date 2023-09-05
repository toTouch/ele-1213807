package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleUserAuth;

import java.util.List;

/**
 * 实名认证信息(TEleUserAuth)表服务接口
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
public interface EleUserAuthService {


    /**
     * 新增数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    EleUserAuth insert(EleUserAuth eleUserAuth);

    /**
     * 修改数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    Integer update(EleUserAuth eleUserAuth);


    R webAuth(List<EleUserAuth> eleUserAuthList);


    R getEleUserAuthSpecificStatus(Long uid);

    R selectUserAuthStatus(Long uid);

    R selectCurrentEleAuthEntriesList(Long uid);

    R getEleUserServiceStatus();

    void updateByUid(Long uid, Integer authStatus);

    EleUserAuth queryByUidAndEntryId(Long uid, Integer idIdCard);

    R acquireIdcardFileSign();

    R acquireselfieFileSign();

}

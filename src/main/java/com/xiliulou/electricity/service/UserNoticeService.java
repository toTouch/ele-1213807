package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.UserNoticeQuery;
import org.apache.commons.lang3.tuple.Triple;

/**
 * (Faq)表服务接口
 *
 * @author makejava
 * @since 2021-09-26 14:06:23
 */
public interface UserNoticeService {

    Object queryUserNotice();

    Triple<Boolean, String, Object> insert(UserNoticeQuery userNoticeQuery);

    Triple<Boolean, String, Object> update(UserNoticeQuery userNoticeQuery);

}

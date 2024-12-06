package com.xiliulou.electricity.service.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;

/**
 * @Description 供其他功能调用方法，分期相关service全部使用的是构造器注入，不兼容循环依赖
 * @Author: SongJP
 * @Date: 2024/12/5 11:31
 */
public interface InstallmentSearchApiService {
    
    /**
     * 根据用户查询使用中的最近一条签约记录
     *
     * @param uid 用户uid
     * @return 签约记录
     */
    InstallmentRecord queryUsingRecordForUser(Long uid);
}

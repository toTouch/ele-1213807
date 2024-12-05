package com.xiliulou.electricity.service.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;

import java.util.List;

/**
 * @Description 供其他功能调用方法，分期相关service全部使用的是构造器注入，不兼容循环依赖
 * @Author: SongJP
 * @Date: 2024/12/5 11:31
 */
public interface InstallmentSearchApiService {
    
    /**
     * 根据状态查询用户的最近一条签约记录
     *
     * @param uid    用户
     * @param status 签约记录状态
     * @return 签约记录
     */
    InstallmentRecord queryRecordWithStatusForUser(Long uid, List<Integer> status);
}

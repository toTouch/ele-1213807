package com.xiliulou.electricity.service.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:50
 */
public interface InstallmentRecordService {
    
    /**
     * 新增单条数据
     * @param installmentRecord 数据库表实体类对象
     * @return 保存的数据条数
     */
    int insert(InstallmentRecord installmentRecord);
    
    /**
     * 更新单条数据
     * @param installmentRecord 数据库表实体类对象
     * @return 更新操作影响的数据行数
     */
    int update(InstallmentRecord installmentRecord);
}

package com.xiliulou.electricity.service.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.vo.installment.InstallmentRecordVO;

import java.util.List;

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
    Integer insert(InstallmentRecord installmentRecord);
    
    /**
     * 更新单条数据
     * @param installmentRecord 数据库表实体类对象
     * @return 更新操作影响的数据行数
     */
    Integer update(InstallmentRecord installmentRecord);
    
    /**
     * 分页查询数据
     * @param installmentRecordQuery 分页查询请求参数
     * @return 分页数据VO结果
     */
    R<List<InstallmentRecordVO>> listForPage(InstallmentRecordQuery installmentRecordQuery);
    
    /**
     * 查询分页总数
     * @param installmentRecordQuery 分页查询请求参数
     * @return 分页总数
     */
    R<Integer> count(InstallmentRecordQuery installmentRecordQuery);
}

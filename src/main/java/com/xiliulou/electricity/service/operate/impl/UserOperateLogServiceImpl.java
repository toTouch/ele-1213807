package com.xiliulou.electricity.service.operate.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.operate.UserOperateLogEntity;
import com.xiliulou.electricity.mapper.operate.UserOperateLogMapper;
import com.xiliulou.electricity.query.operate.OperateQuery;
import com.xiliulou.electricity.service.operate.UserOperateLogService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * Description: This class is UserOperateLogServiceImpl!
 * </p>
 * <p>Project: xiliulou-ele-data-batch</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/27
 **/
@Service
public class UserOperateLogServiceImpl extends ServiceImpl<UserOperateLogMapper, UserOperateLogEntity> implements UserOperateLogService {
    
    @DS(value = "clickhouse")
    @Override
    public R<List<UserOperateLogEntity>> page(OperateQuery query) {
        
        return R.ok(this.baseMapper.selectByPage(query));
    }
    
    @DS(value = "clickhouse")
    @Override
    public R<Long> pageCount(OperateQuery query) {
        return R.ok(this.baseMapper.selectByCount(query));
    }
    
    
    public static String convertTime(Long time){
        try {
            return FORMAT.format(new Date(time));
        }catch (Throwable e){
            return null;
        }
    }
    
}

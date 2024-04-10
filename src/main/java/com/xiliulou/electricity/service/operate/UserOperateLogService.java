package com.xiliulou.electricity.service.operate;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.operate.UserOperateLogEntity;
import com.xiliulou.electricity.query.operate.OperateQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Description: This interface is UserOperateLogService!
 * </p>
 * <p>Project: xiliulou-ele-data-batch</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/27
 **/
public interface UserOperateLogService  {
    
    SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    R<List<UserOperateLogEntity>> page(OperateQuery query);
    
 
    
    R<Long> pageCount(OperateQuery query);
}

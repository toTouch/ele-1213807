package com.xiliulou.electricity.mapper.operate;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.operate.UserOperateLogEntity;
import com.xiliulou.electricity.query.operate.OperateQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * Description: This interface is UserOperateLogMapper!
 * </p>
 * <p>Project: xiliulou-ele-data-batch</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/27
 **/
@Mapper
public interface UserOperateLogMapper extends BaseMapper<UserOperateLogEntity> {
    
    List<UserOperateLogEntity> selectByPage(@Param("query") OperateQuery query);
    
    Long selectByCount(@Param("query") OperateQuery query);
}

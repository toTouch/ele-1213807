package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleUserOperateHistory;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.query.EleUserOperateHistoryQueryModel;
import com.xiliulou.electricity.vo.EleUserOperateHistoryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户操作记录(EleUserOperateHistory)表数据库访问层
 *
 * @author zhangyongbo
 * @since 2023-12-28 14:44:12
 */
public interface EleUserOperateHistoryMapper{
   
   List<EleUserOperateHistoryVO> selectListUserOperateHistory(EleUserOperateHistoryQueryModel eleUserOperateHistoryQueryModel);
   
   Integer insertOne(EleUserOperateHistory eleUserOperateHistory);
}

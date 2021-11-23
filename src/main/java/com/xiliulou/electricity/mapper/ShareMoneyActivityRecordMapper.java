package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ShareMoneyActivityRecord;
import com.xiliulou.electricity.query.ShareMoneyActivityRecordQuery;
import com.xiliulou.electricity.vo.ShareMoneyActivityRecordVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 发起邀请活动记录(ShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
public interface ShareMoneyActivityRecordMapper extends BaseMapper<ShareMoneyActivityRecord>{


    @Update("update t_share_money_activity_record set count=count+1 where uid =#{uid}")
	void addCountByUid(@Param("uid") Long uid);


	List<ShareMoneyActivityRecordVO> queryList(ShareMoneyActivityRecordQuery shareMoneyActivityRecordQuery);

	Integer queryCount(ShareMoneyActivityRecordQuery shareMoneyActivityRecordQuery);
}

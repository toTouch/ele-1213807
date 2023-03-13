package com.xiliulou.electricity.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.JoinShareActivityHistoryMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderExcelQuery;
import com.xiliulou.electricity.query.JsonShareActivityHistoryQuery;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FinalJoinShareActivityHistoryVo;
import com.xiliulou.electricity.vo.JoinShareActivityHistoryExcelVo;
import com.xiliulou.electricity.vo.JoinShareActivityHistoryVO;
import com.xiliulou.electricity.vo.UserInfoExcelVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.beans.SimpleBeanInfo;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 参与邀请活动记录(JoinShareActivityHistory)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@Service("joinShareActivityHistoryService")
@Slf4j
public class JoinShareActivityHistoryServiceImpl implements JoinShareActivityHistoryService {
	@Resource
	private JoinShareActivityHistoryMapper joinShareActivityHistoryMapper;

	@Autowired
	UserService userService;
	
	@Autowired
	ShareActivityRecordService shareActivityRecordService;
	
	@Autowired
	UserInfoService userInfoService;


	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public JoinShareActivityHistory queryByIdFromDB(Long id) {
		return this.joinShareActivityHistoryMapper.selectById(id);
	}

	/**
	 * 新增数据
	 *
	 * @param joinShareActivityHistory 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public JoinShareActivityHistory insert(JoinShareActivityHistory joinShareActivityHistory) {
		this.joinShareActivityHistoryMapper.insert(joinShareActivityHistory);
		return joinShareActivityHistory;
	}

	/**
	 * 修改数据
	 *
	 * @param joinShareActivityHistory 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(JoinShareActivityHistory joinShareActivityHistory) {
		return this.joinShareActivityHistoryMapper.updateById(joinShareActivityHistory);

	}

	@Override
	public JoinShareActivityHistory queryByRecordIdAndStatus(Long id) {
		return joinShareActivityHistoryMapper.selectOne(new LambdaQueryWrapper<JoinShareActivityHistory>()
				.eq(JoinShareActivityHistory::getRecordId,id).eq(JoinShareActivityHistory::getStatus,JoinShareActivityHistory.STATUS_INIT));
	}

	@Override
	public R userList(Integer activityId) {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("joinActivity  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery=new JsonShareActivityHistoryQuery();
		jsonShareActivityHistoryQuery.setActivityId(activityId);
		jsonShareActivityHistoryQuery.setUid(user.getUid());
        
        List<JoinShareActivityHistoryVO> joinShareActivityHistoryVOList = joinShareActivityHistoryMapper
                .queryList(jsonShareActivityHistoryQuery);
        return R.ok(Optional.ofNullable(joinShareActivityHistoryVOList).orElse(new ArrayList<>()));
	}

	@Override
	public R queryList(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery) {
		ShareActivityRecord shareActivityRecord = shareActivityRecordService
				.queryByIdFromDB(jsonShareActivityHistoryQuery.getId());
		if (Objects.isNull(shareActivityRecord)) {
			return R.failMsg("未查询到相关邀请记录");
		}
		
		jsonShareActivityHistoryQuery.setActivityId(shareActivityRecord.getActivityId());
		jsonShareActivityHistoryQuery.setUid(shareActivityRecord.getUid());
		
		List<JoinShareActivityHistoryVO> joinShareActivityHistoryVOList = joinShareActivityHistoryMapper
				.queryList(jsonShareActivityHistoryQuery);
        return R.ok(Optional.ofNullable(joinShareActivityHistoryVOList).orElse(new ArrayList<>()));
	}

	@Override
	public void updateByActivityId(JoinShareActivityHistory joinShareActivityHistory) {
		joinShareActivityHistoryMapper.updateByActivityId(joinShareActivityHistory);
	}

	@Override
	public void updateExpired(JoinShareActivityHistory joinShareActivityHistory) {
		joinShareActivityHistoryMapper.updateExpired(joinShareActivityHistory);
	}
	
	@Override
	public FinalJoinShareActivityHistoryVo queryFinalHistoryByJoinUid(Long uid, Integer tenantId) {
		return joinShareActivityHistoryMapper.queryFinalHistoryByJoinUid(uid, tenantId);
	}
	
	@Override
	public R queryCount(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery) {
		ShareActivityRecord shareActivityRecord = shareActivityRecordService
				.queryByIdFromDB(jsonShareActivityHistoryQuery.getId());
		if (Objects.isNull(shareActivityRecord)) {
			return R.failMsg("未查询到相关邀请记录");
		}
		
		jsonShareActivityHistoryQuery.setActivityId(shareActivityRecord.getActivityId());
		jsonShareActivityHistoryQuery.setUid(shareActivityRecord.getUid());
		
		Long count = joinShareActivityHistoryMapper.queryCount(jsonShareActivityHistoryQuery);
		return R.ok(count);
	}
	
	@Override
	public void queryExportExcel(JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery,
			HttpServletResponse response) {
		ShareActivityRecord shareActivityRecord = shareActivityRecordService
				.queryByIdFromDB(jsonShareActivityHistoryQuery.getId());
		if (Objects.isNull(shareActivityRecord)) {
			throw new CustomBusinessException("查询不到记录");
		}
		
		jsonShareActivityHistoryQuery.setActivityId(shareActivityRecord.getActivityId());
		jsonShareActivityHistoryQuery.setUid(shareActivityRecord.getUid());
		jsonShareActivityHistoryQuery.setOffset(0L);
		jsonShareActivityHistoryQuery.setSize(2000L);
		
		List<JoinShareActivityHistoryExcelVo> voList = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		
		List<ElectricityCabinetOrderExcelQuery> query = joinShareActivityHistoryMapper
				.queryExportExcel(jsonShareActivityHistoryQuery);
		Optional.ofNullable(query).orElse(new ArrayList<>()).forEach(item -> {
			JoinShareActivityHistoryExcelVo vo = new JoinShareActivityHistoryExcelVo();
			vo.setJoinName(item.getJoinName());
			vo.setJoinPhone(item.getJoinPhone());
			vo.setStatus(queryStatus(item.getStatus()));
			
			date.setTime(item.getStartTime());
			vo.setStartTime(sdf.format(date));
			
			date.setTime(item.getExpiredTime());
			vo.setExpiredTime(sdf.format(date));
			
			UserInfo userInfo = userInfoService.queryByIdFromDB(item.getUid());
			if (Objects.nonNull(userInfo)) {
				vo.setName(userInfo.getName());
				vo.setPhone(userInfo.getPhone());
			}
			voList.add(vo);
		});
        
        String fileName = "邀请活动记录.xlsx";
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			// 告诉浏览器用什么软件可以打开此文件
			response.setHeader("content-Type", "application/vnd.ms-excel");
			// 下载文件的默认名称
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
			EasyExcel.write(outputStream, JoinShareActivityHistoryExcelVo.class).sheet("sheet").doWrite(voList);
			return;
		} catch (IOException e) {
			log.error("导出报表失败！", e);
		}
	}
	
	private String queryStatus(Integer status) {
		//参与状态 1--初始化，2--已参与，3--已过期，4--被替换
		String result = "";
		switch (status) {
			case 1:
				result = "初始化";
				break;
			case 2:
				result = "已参与";
				break;
			case 3:
				result = "已过期";
				break;
			case 4:
				result = "被替换";
				break;
			default:
				result = "";
		}
		return result;
	}
}

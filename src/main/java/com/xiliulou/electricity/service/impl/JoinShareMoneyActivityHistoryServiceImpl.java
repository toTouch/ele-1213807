package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityHistory;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivityRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.JoinShareMoneyActivityHistoryMapper;
import com.xiliulou.electricity.query.JoinShareMoneyActivityHistoryExcelQuery;
import com.xiliulou.electricity.query.JsonShareMoneyActivityHistoryQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FinalJoinShareMoneyActivityHistoryVo;
import com.xiliulou.electricity.vo.JoinShareMoneyActivityHistoryExcelVo;
import com.xiliulou.electricity.vo.JoinShareMoneyActivityHistoryVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 参与邀请活动记录(JoinShareActivityHistory)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@Service("joinShareMoneyActivityHistoryService")
@Slf4j
public class JoinShareMoneyActivityHistoryServiceImpl implements JoinShareMoneyActivityHistoryService {
	@Resource
	private JoinShareMoneyActivityHistoryMapper joinShareMoneyActivityHistoryMapper;

	@Autowired
	UserService userService;

	@Autowired
	ShareMoneyActivityService shareMoneyActivityService;
    
    @Autowired
    ShareMoneyActivityRecordService shareMoneyActivityRecordService;
	
	@Autowired
	UserInfoService userInfoService;
	
	@Resource
	private FranchiseeService franchiseeService;


	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public JoinShareMoneyActivityHistory queryByIdFromDB(Long id) {
		return this.joinShareMoneyActivityHistoryMapper.selectById(id);
	}

	/**
	 * 新增数据
	 *
	 * @param joinShareMoneyActivityHistory 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public JoinShareMoneyActivityHistory insert(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory) {
		this.joinShareMoneyActivityHistoryMapper.insert(joinShareMoneyActivityHistory);
		return joinShareMoneyActivityHistory;
	}

	/**
	 * 修改数据
	 *
	 * @param joinShareMoneyActivityHistory 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory) {
		return this.joinShareMoneyActivityHistoryMapper.updateById(joinShareMoneyActivityHistory);

	}
// TODO 111
//	@Slave
//	@Override
//	public JoinShareMoneyActivityHistory queryByRecordIdAndStatus(Long id) {
//		return joinShareMoneyActivityHistoryMapper.selectOne(new LambdaQueryWrapper<JoinShareMoneyActivityHistory>()
//				.eq(JoinShareMoneyActivityHistory::getRecordId,id).eq(JoinShareMoneyActivityHistory::getStatus,JoinShareMoneyActivityHistory.STATUS_INIT));
//	}

	@Slave
	@Override
	public JoinShareMoneyActivityHistory queryByRecordIdAndJoinUid(Long rid, Long uid) {
		return joinShareMoneyActivityHistoryMapper.selectOne(new LambdaQueryWrapper<JoinShareMoneyActivityHistory>()
				.eq(JoinShareMoneyActivityHistory::getRecordId, rid).eq(JoinShareMoneyActivityHistory::getJoinUid, uid)
				.eq(JoinShareMoneyActivityHistory::getStatus, JoinShareMoneyActivityHistory.STATUS_INIT));
	}

	@Override
	public R userList(Integer activityId) {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("joinActivity  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery=new JsonShareMoneyActivityHistoryQuery();
		jsonShareMoneyActivityHistoryQuery.setActivityId(activityId);
		jsonShareMoneyActivityHistoryQuery.setUid(user.getUid());
        jsonShareMoneyActivityHistoryQuery.setTenantId(TenantContextHolder.getTenantId());
        
        List<JoinShareMoneyActivityHistoryVO> voList = joinShareMoneyActivityHistoryMapper
                .queryList(jsonShareMoneyActivityHistoryQuery);
        
        if (ObjectUtil.isEmpty(voList)) {
            return R.ok(voList);
        }
        
        for (JoinShareMoneyActivityHistoryVO vo : voList) {
            ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByIdFromCache(vo.getActivityId());

			if(Objects.nonNull(shareMoneyActivity)){
                vo.setMoney(shareMoneyActivity.getMoney());
			}
	
	        Long franchiseeId = vo.getFranchiseeId();
	        if (Objects.nonNull(franchiseeId)) {
		        vo.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
	        }
        }
        
        return R.ok(voList);
	}

	@Slave
	@Override
	public R queryList(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery) {
		ShareMoneyActivityRecord shareMoneyActivityRecord = shareMoneyActivityRecordService.queryByIdFromDB(jsonShareMoneyActivityHistoryQuery.getId());
		if (Objects.isNull(shareMoneyActivityRecord)) {
			return R.ok(new ArrayList<>());
		}
		
		jsonShareMoneyActivityHistoryQuery.setUid(shareMoneyActivityRecord.getUid());
		jsonShareMoneyActivityHistoryQuery.setActivityId(shareMoneyActivityRecord.getActivityId());
		
		List<JoinShareMoneyActivityHistoryVO> voList = joinShareMoneyActivityHistoryMapper.queryList(jsonShareMoneyActivityHistoryQuery);
		if (CollectionUtils.isEmpty(voList)) {
			return R.ok(Collections.emptyList());
		}
		
		List<JoinShareMoneyActivityHistoryVO> list = voList.stream().peek(vo -> {
			Long franchiseeId = vo.getFranchiseeId();
			if (Objects.nonNull(franchiseeId)) {
				vo.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
			}
			
		}).collect(Collectors.toList());
		
		return R.ok(list);
	}

	@Slave
	@Override
	public R queryCount(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery) {
        ShareMoneyActivityRecord shareMoneyActivityRecord = shareMoneyActivityRecordService
                .queryByIdFromDB(jsonShareMoneyActivityHistoryQuery.getId());
        if (Objects.isNull(shareMoneyActivityRecord)) {
            return R.ok(0);
        }
        
        jsonShareMoneyActivityHistoryQuery.setUid(shareMoneyActivityRecord.getUid());
        jsonShareMoneyActivityHistoryQuery.setActivityId(shareMoneyActivityRecord.getActivityId());
		Integer count=joinShareMoneyActivityHistoryMapper.queryCount(jsonShareMoneyActivityHistoryQuery);
		return R.ok(count);
	}

	@Override
	public void updateByActivityId(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory) {
		joinShareMoneyActivityHistoryMapper.updateByActivityId(joinShareMoneyActivityHistory);
	}

	@Override
	public void updateExpired(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory) {
		joinShareMoneyActivityHistoryMapper.updateExpired(joinShareMoneyActivityHistory);
	}
	
	@Override
	public FinalJoinShareMoneyActivityHistoryVo queryFinalHistoryByJoinUid(Long uid, Integer tenantId) {
		return joinShareMoneyActivityHistoryMapper.queryFinalHistoryByJoinUid(uid, tenantId);
	}
	
	@Override
	public void queryExportExcel(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery,
			HttpServletResponse response) {
		ShareMoneyActivityRecord shareMoneyActivityRecord = shareMoneyActivityRecordService
				.queryByIdFromDB(jsonShareMoneyActivityHistoryQuery.getId());
		if (Objects.isNull(shareMoneyActivityRecord)) {
			throw new CustomBusinessException("查询不到记录");
		}
		
		jsonShareMoneyActivityHistoryQuery.setUid(shareMoneyActivityRecord.getUid());
		jsonShareMoneyActivityHistoryQuery.setActivityId(shareMoneyActivityRecord.getActivityId());
		jsonShareMoneyActivityHistoryQuery.setOffset(0L);
		jsonShareMoneyActivityHistoryQuery.setSize(2000L);
		
		List<JoinShareMoneyActivityHistoryExcelVo> voList = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
        
        List<JoinShareMoneyActivityHistoryExcelQuery> queryList = joinShareMoneyActivityHistoryMapper
                .queryExportExcel(jsonShareMoneyActivityHistoryQuery);
        Optional.ofNullable(queryList).orElse(new ArrayList<>()).forEach(item -> {
            JoinShareMoneyActivityHistoryExcelVo vo = new JoinShareMoneyActivityHistoryExcelVo();
            vo.setJoinName(item.getJoinName());
	        vo.setJoinPhone(item.getJoinPhone());
	        date.setTime(item.getExpiredTime());
	        vo.setExpiredTime(sdf.format(date));
	        date.setTime(item.getStartTime());
	        vo.setStartTime(sdf.format(date));
            vo.setStatus(queryStatus(item.getStatus()));
	
	        UserInfo userInfo = userInfoService.queryByUidFromDb(item.getUid());
	        if (Objects.nonNull(userInfo)) {
		        vo.setName(userInfo.getName());
		        vo.setPhone(userInfo.getPhone());
	        }
            voList.add(vo);
        });
		
		String fileName = "邀请返现记录.xlsx";
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			// 告诉浏览器用什么软件可以打开此文件
			response.setHeader("content-Type", "application/vnd.ms-excel");
			// 下载文件的默认名称
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
			EasyExcel.write(outputStream, JoinShareMoneyActivityHistoryExcelVo.class).sheet("sheet").doWrite(voList);
			return;
		} catch (IOException e) {
			log.error("导出报表失败！", e);
		}
	}

	@Slave
	@Override
	public R queryParticipantsRecord(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery) {
		List<JoinShareMoneyActivityHistoryVO> joinShareMoneyActivityHistoryVOS = joinShareMoneyActivityHistoryMapper.queryParticipantsRecord(jsonShareMoneyActivityHistoryQuery);
		
		if (CollectionUtils.isEmpty(joinShareMoneyActivityHistoryVOS)) {
			return R.ok(Collections.emptyList());
		}
		
		for(JoinShareMoneyActivityHistoryVO joinShareMoneyActivityHistoryVO : joinShareMoneyActivityHistoryVOS){
			Long inviterUid = joinShareMoneyActivityHistoryVO.getInviterUid();
			UserInfo userInfo = userInfoService.queryByUidFromDb(inviterUid);
			if(Objects.nonNull(userInfo)){
				joinShareMoneyActivityHistoryVO.setInviterName(userInfo.getName());
				joinShareMoneyActivityHistoryVO.setInviterPhone(userInfo.getPhone());
			}
			
			Long franchiseeId = joinShareMoneyActivityHistoryVO.getFranchiseeId();
			if (Objects.nonNull(franchiseeId)) {
				joinShareMoneyActivityHistoryVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
			}
		}

		return R.ok(joinShareMoneyActivityHistoryVOS);
	}

	@Slave
	@Override
	public R queryParticipantsCount(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery) {
		Long count = joinShareMoneyActivityHistoryMapper.queryParticipantsRecordCount(jsonShareMoneyActivityHistoryQuery);
		return R.ok(count);
	}

	@Override
	public List<JoinShareMoneyActivityHistory> queryUserJoinedActivity(Long joinUid, Integer tenantId) {
		return joinShareMoneyActivityHistoryMapper.queryUserJoinedShareMoneyActivity(joinUid, tenantId);
	}

	@Slave
	@Override
	public Pair<Boolean, String> checkJoinedActivityFromSameInviter(Long joinUid, Long inviterUid, Long activityId) {
		List<JoinShareMoneyActivityHistory> joinShareMoneyActivityHistories = joinShareMoneyActivityHistoryMapper.queryJoinedActivityByJoinerAndInviter(joinUid, inviterUid, activityId);
		if(CollectionUtils.isNotEmpty(joinShareMoneyActivityHistories)){
			for(JoinShareMoneyActivityHistory joinShareMoneyActivityHistory : joinShareMoneyActivityHistories){
				if(JoinShareMoneyActivityHistory.STATUS_SUCCESS.equals(joinShareMoneyActivityHistory.getStatus())){
					return Pair.of(Boolean.TRUE, "已参与过邀请返现活动");
				}
			}
			//如果没有参与成功。但是属于二次扫同一个人的码，则直接返回。
			return Pair.of(Boolean.TRUE, StringUtils.EMPTY);
		}

		return Pair.of(Boolean.FALSE, null);
	}
	
	@Slave
	@Override
	public JoinShareMoneyActivityHistory querySuccessHistoryByJoinUid(Long uid, Integer tenantId) {
		return joinShareMoneyActivityHistoryMapper.selectSuccessHistoryByJoinUid(uid, tenantId);
	}
    
    @Override
    public Integer removeByJoinUid(Long joinUid, Long updateTime, Integer tenantId) {
	    return joinShareMoneyActivityHistoryMapper.removeByJoinUid(joinUid, updateTime, tenantId);
    }
    
    private String queryStatus(Integer status) {
        //参与状态 1--初始化，2--已参与，3--已过期，4--被替换
        String result = "";
        switch (status) {
            case 1:
                result = "已参与";
                break;
            case 2:
                result = "邀请成功";
                break;
            case 3:
                result = "已过期";
                break;
            case 4:
                result = "已失效";
                break;
            case 5:
                result = "活动已下架";
                break;
            default:
                result = "";
        }
        return result;
    }
}

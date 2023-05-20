package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityHistory;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivityRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.JoinShareMoneyActivityHistoryMapper;
import com.xiliulou.electricity.query.JoinShareMoneyActivityHistoryExcelQuery;
import com.xiliulou.electricity.query.JsonShareMoneyActivityHistoryQuery;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.ShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FinalJoinShareActivityHistoryVo;
import com.xiliulou.electricity.vo.FinalJoinShareMoneyActivityHistoryVo;
import com.xiliulou.electricity.vo.JoinShareActivityHistoryExcelVo;
import com.xiliulou.electricity.vo.JoinShareMoneyActivityHistoryExcelVo;
import com.xiliulou.electricity.vo.JoinShareMoneyActivityHistoryVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
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

	@Override
	public JoinShareMoneyActivityHistory queryByRecordIdAndStatus(Long id) {
		return joinShareMoneyActivityHistoryMapper.selectOne(new LambdaQueryWrapper<JoinShareMoneyActivityHistory>()
				.eq(JoinShareMoneyActivityHistory::getRecordId,id).eq(JoinShareMoneyActivityHistory::getStatus,JoinShareMoneyActivityHistory.STATUS_INIT));
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
        
        }
        
        return R.ok(voList);
	}

	@Slave
	@Override
	public R queryList(JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery) {
        ShareMoneyActivityRecord shareMoneyActivityRecord = shareMoneyActivityRecordService
                .queryByIdFromDB(jsonShareMoneyActivityHistoryQuery.getId());
        if (Objects.isNull(shareMoneyActivityRecord)) {
            return R.ok(new ArrayList<>());
        }
        
        jsonShareMoneyActivityHistoryQuery.setUid(shareMoneyActivityRecord.getUid());
        jsonShareMoneyActivityHistoryQuery.setActivityId(shareMoneyActivityRecord.getActivityId());
		List<JoinShareMoneyActivityHistoryVO> voList = joinShareMoneyActivityHistoryMapper
				.queryList(jsonShareMoneyActivityHistoryQuery);
        
        return R.ok(voList);
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

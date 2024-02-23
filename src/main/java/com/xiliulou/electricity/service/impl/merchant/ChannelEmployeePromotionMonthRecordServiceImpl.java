package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.date.DateUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.ChannelEmployeePromotionMonthRecord;
import com.xiliulou.electricity.mapper.merchant.ChannelEmployeePromotionMonthRecordMapper;
import com.xiliulou.electricity.query.merchant.ChannelEmployeePromotionQueryModel;
import com.xiliulou.electricity.request.merchant.ChannelEmployeePromotionRequest;
import com.xiliulou.electricity.service.merchant.ChannelEmployeePromotionMonthRecordService;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeePromotionVO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/2/21 14:03
 * @desc
 */
@Service
public class ChannelEmployeePromotionMonthRecordServiceImpl implements ChannelEmployeePromotionMonthRecordService {
    
    @Resource
    private ChannelEmployeePromotionMonthRecordMapper channelEmployeePromotionMonthRecordMapper;
    
    @Transactional
    @Override
    public void batchInsert(List<ChannelEmployeePromotionMonthRecord> list) {
        channelEmployeePromotionMonthRecordMapper.batchInsert(list);
    }
    
    @Slave
    @Override
    public List<ChannelEmployeePromotionVO> listByPage(ChannelEmployeePromotionRequest channelEmployeeRequest) {
        ChannelEmployeePromotionQueryModel channelEmployeePromotionQueryModel = new ChannelEmployeePromotionQueryModel();
        BeanUtils.copyProperties(channelEmployeeRequest, channelEmployeePromotionQueryModel);
        
        // 处理时间
        initParam(channelEmployeePromotionQueryModel);
        List<ChannelEmployeePromotionVO> list = channelEmployeePromotionMonthRecordMapper.selectListByPage(channelEmployeeRequest);
        if (ObjectUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        
        // 处理出账日期
        list.stream().forEach(item -> {
            if (Objects.nonNull(item.getFeeDate())) {
                String billingDate = DateUtil.format(new Date(item.getFeeDate()), "yyyy-MM");
                item.setBillingDate(billingDate);
            }
        });
        
        return list;
    }
    
    private void initParam(ChannelEmployeePromotionQueryModel channelEmployeePromotionQueryModel) {
        if (ObjectUtils.isNotEmpty(channelEmployeePromotionQueryModel.getTime())) {
            // 将出账日期转换为具体的日期
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(channelEmployeePromotionQueryModel.getTime());
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            
            long startTime = calendar.getTimeInMillis();
            
            Calendar calendarLast = Calendar.getInstance();
            calendarLast.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            
            calendarLast.set(Calendar.HOUR_OF_DAY, 23);
            calendarLast.set(Calendar.MINUTE, 59);
            calendarLast.set(Calendar.SECOND, 59);
            
            long endTime = calendar.getTimeInMillis();
            
            channelEmployeePromotionQueryModel.setStartTime(startTime);
            channelEmployeePromotionQueryModel.setEndTime(endTime);
        }
    }
    
    @Slave
    @Override
    public Integer countTotal(ChannelEmployeePromotionRequest channelEmployeeRequest) {
        ChannelEmployeePromotionQueryModel channelEmployeePromotionQueryModel = new ChannelEmployeePromotionQueryModel();
        BeanUtils.copyProperties(channelEmployeeRequest, channelEmployeePromotionQueryModel);
        // 处理时间
        initParam(channelEmployeePromotionQueryModel);
        
        return channelEmployeePromotionMonthRecordMapper.countTotal(channelEmployeePromotionQueryModel);
    }
}

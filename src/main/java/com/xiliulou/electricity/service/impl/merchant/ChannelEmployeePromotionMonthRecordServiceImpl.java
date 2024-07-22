package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.ChannelEmployeePromotionDayRecord;
import com.xiliulou.electricity.entity.merchant.ChannelEmployeePromotionMonthRecord;
import com.xiliulou.electricity.enums.merchant.RebateTypeEnum;
import com.xiliulou.electricity.mapper.merchant.ChannelEmployeePromotionMonthRecordMapper;
import com.xiliulou.electricity.query.merchant.ChannelEmployeePromotionQueryModel;
import com.xiliulou.electricity.request.merchant.ChannelEmployeePromotionRequest;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.excel.CommentWriteHandler;
import com.xiliulou.electricity.service.excel.HeadContentCellStyle;
import com.xiliulou.electricity.service.excel.MergeSameRowsStrategy;
import com.xiliulou.electricity.service.merchant.ChannelEmployeePromotionDayRecordService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeePromotionMonthRecordService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeePromotionMonthExportVO;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeePromotionVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/2/21 14:03
 * @desc
 */
@Service("channelEmployeePromotionMonthRecordService")
@Slf4j
public class ChannelEmployeePromotionMonthRecordServiceImpl implements ChannelEmployeePromotionMonthRecordService {
    
    @Resource
    private ChannelEmployeePromotionMonthRecordMapper channelEmployeePromotionMonthRecordMapper;
    
    @Resource
    private ChannelEmployeePromotionDayRecordService channelEmployeePromotionDayRecordService;
    
    @Resource
    private ChannelEmployeeService channelEmployeeService;
    
    @Resource
    private UserService userService;
    
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
        
        List<ChannelEmployeePromotionVO> list = channelEmployeePromotionMonthRecordMapper.selectListByPage(channelEmployeePromotionQueryModel);
        
        if (ObjectUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        
        return list;
    }
    
    @Slave
    @Override
    public Integer countTotal(ChannelEmployeePromotionRequest channelEmployeeRequest) {
        ChannelEmployeePromotionQueryModel channelEmployeePromotionQueryModel = new ChannelEmployeePromotionQueryModel();
        BeanUtils.copyProperties(channelEmployeeRequest, channelEmployeePromotionQueryModel);
        
        return channelEmployeePromotionMonthRecordMapper.countTotal(channelEmployeePromotionQueryModel);
    }
    
    /**
     * 创建表头
     */
    private static List<List<String>> getHeader() {
        List<List<String>> headers = new ArrayList<>();
        headers.add(Arrays.asList("出账年月", "出账年月"));
        headers.add(Arrays.asList("渠道员汇总", "渠道员"));
        headers.add(Arrays.asList("渠道员汇总", "月拉新返现汇总(元)"));
        headers.add(Arrays.asList("渠道员汇总", "月续费返现汇总(元)"));
        headers.add(Arrays.asList("返利明细", "类型"));
        headers.add(Arrays.asList("返利明细", "返现(元)"));
        headers.add(Arrays.asList("返利明细", "结算时间"));
        return headers;
    }
    
    private static List<Map<String, String>> getComments() {
        List<Map<String, String>> commentList = new ArrayList<>();
        commentList.add(CommentWriteHandler.createCommentMap("渠道员提成出账记录", 1, 4,
                "拉新收益：本月新增用户返利； \n" + "续费收益：本月续费用户返利； \n" + "差额：本月商户升级后额外返利补贴。"));
        return commentList;
    }
    
    /**
     * 获取某个月的数据的详情
     *
     * @param monthDate
     * @param franchiseeId
     * @return
     */
    @Slave
    private List<ChannelEmployeePromotionMonthExportVO> getData(String monthDate, Long franchiseeId) throws ParseException {
        List<ChannelEmployeePromotionMonthExportVO> resList = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date date = format.parse(monthDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        
        long endTime = calendar.getTimeInMillis();
        
        // 查询月度账单
        List<ChannelEmployeePromotionMonthRecord> channelEmployeePromotionMonthRecords = channelEmployeePromotionMonthRecordMapper.selectByFeeDate(TenantContextHolder.getTenantId(), monthDate, franchiseeId);
        
        // 查询日结账单
        List<ChannelEmployeePromotionDayRecord> channelEmployeePromotionDayRecords = channelEmployeePromotionDayRecordService.queryListByFeeDate(startTime, endTime,
                TenantContextHolder.getTenantId(), franchiseeId);
        
        if (ObjectUtils.isEmpty(channelEmployeePromotionMonthRecords)) {
            return resList;
        }
        
        Map<Long, List<ChannelEmployeePromotionDayRecord>> detailMap = channelEmployeePromotionDayRecords.stream()
                .collect(Collectors.groupingBy(ChannelEmployeePromotionDayRecord::getChannelEmployeesId));
        
        channelEmployeePromotionMonthRecords.stream().forEach(item -> {
            // 渠道员名称
            String userName = "";
            
            User user = userService.queryByUidFromCache(item.getChannelEmployeesId());
            if (Objects.nonNull(user)) {
                userName = user.getName();
            }
            
            // 判断详情数据是否为空
            List<ChannelEmployeePromotionDayRecord> detailList = detailMap.get(item.getChannelEmployeesId());
            
            if (ObjectUtils.isEmpty(detailList)) {
                ChannelEmployeePromotionMonthExportVO vo = new ChannelEmployeePromotionMonthExportVO();
                vo.setChannelEmployeeName(userName);
                vo.setMonth(monthDate);
                resList.add(vo);
                
                return;
            }
            
            // 按照时间进行排序
            detailList = detailList.stream().sorted(Comparator.comparing(ChannelEmployeePromotionDayRecord::getFeeDate)).collect(Collectors.toList());
            
            for (ChannelEmployeePromotionDayRecord promotionDayRecord : detailList) {
                // 拉新
                ChannelEmployeePromotionMonthExportVO first = new ChannelEmployeePromotionMonthExportVO();
                first.setMonth(monthDate);
                first.setChannelEmployeeName(userName);
                first.setMonthFirstSumFee(item.getMonthFirstMoney());
                first.setMonthRenewSumFee(item.getMonthRenewMoney());
                first.setType(RebateTypeEnum.FIRST.getDesc());
                first.setSettleDate(DateUtil.format(new Date(promotionDayRecord.getFeeDate()), "yyyy-MM-dd"));
                first.setReturnMoney(promotionDayRecord.getDayFirstMoney());
                
                resList.add(first);
    
                // 续费
                ChannelEmployeePromotionMonthExportVO renewVo = new ChannelEmployeePromotionMonthExportVO();
                renewVo.setMonth(monthDate);
                renewVo.setChannelEmployeeName(userName);
                renewVo.setMonthFirstSumFee(item.getMonthFirstMoney());
                renewVo.setMonthRenewSumFee(item.getMonthRenewMoney());
                renewVo.setType(RebateTypeEnum.RENEW.getDesc());
                renewVo.setSettleDate(DateUtil.format(new Date(promotionDayRecord.getFeeDate()), "yyyy-MM-dd"));
                renewVo.setReturnMoney(promotionDayRecord.getDayRenewMoney());
                
                resList.add(renewVo);
                
                // 差额
                ChannelEmployeePromotionMonthExportVO balanceVo = new ChannelEmployeePromotionMonthExportVO();
                balanceVo.setMonth(monthDate);
                balanceVo.setChannelEmployeeName(userName);
                balanceVo.setMonthFirstSumFee(item.getMonthFirstMoney());
                balanceVo.setMonthRenewSumFee(item.getMonthRenewMoney());
                balanceVo.setType(RebateTypeEnum.BALANCE.getDesc());
                BigDecimal returnMoney = BigDecimal.ZERO;
                
                if (ObjectUtils.isNotEmpty(promotionDayRecord.getDayBalanceMoney())) {
                    returnMoney = returnMoney.add(promotionDayRecord.getDayBalanceMoney());
                }
                
                if (ObjectUtils.isNotEmpty(promotionDayRecord.getDayRenewBalanceMoney())) {
                    returnMoney = returnMoney.add(promotionDayRecord.getDayRenewBalanceMoney());
                }
                
                balanceVo.setReturnMoney(returnMoney);
                balanceVo.setSettleDate(DateUtil.format(new Date(promotionDayRecord.getFeeDate()), "yyyy-MM-dd"));
    
                resList.add(balanceVo);
    
            }
            
        });
        
        log.info("channelEmployeePromotionMonthRecords={}", resList);
        
        return resList;
    }
    
    @Slave
    @Override
    public void export(String monthDate, HttpServletResponse response, Long franchiseeId) {
        String fileName = "渠道员提成出账记录.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            
            EasyExcel.write(outputStream, ChannelEmployeePromotionMonthExportVO.class).head(getHeader())
                    // 合并策略：合并相同数据的行。第一个参数表示从哪一行开始进行合并，由于表头占了两行，因此从第2行开始（索引从0开始）
                    // 第二个参数是指定哪些列要进行合并
                    .registerWriteHandler(new MergeSameRowsStrategy(2, new int[] {0, 1, 2, 3})).registerWriteHandler(HeadContentCellStyle.myHorizontalCellStyleStrategy())
                    .registerWriteHandler(new CommentWriteHandler(getComments(), "xlsx")).registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                    // 注意：需要先调用registerWriteHandler()再调用sheet()方法才能使合并策略生效！！！
                    .sheet("渠道员提成出账记录").doWrite(getData(monthDate, franchiseeId));
            
        } catch (Exception e) {
            log.error("channel employee promotion export error！", e);
        }
    }
}

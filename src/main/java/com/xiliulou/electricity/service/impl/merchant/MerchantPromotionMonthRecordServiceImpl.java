package com.xiliulou.electricity.service.impl.merchant;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.merchant.RebateRecordConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantPromotionDayRecord;
import com.xiliulou.electricity.entity.merchant.MerchantPromotionMonthRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantPromotionMonthRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDayRecordQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionMonthRecordQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantPromotionRequest;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.excel.CommentWriteHandler;
import com.xiliulou.electricity.service.excel.HeadContentCellStyle;
import com.xiliulou.electricity.service.excel.MergeSameRowsStrategy;
import com.xiliulou.electricity.service.merchant.MerchantPromotionDayRecordService;
import com.xiliulou.electricity.service.merchant.MerchantPromotionMonthRecordService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionDayRecordVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionMonthExcelVO;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionMonthRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 商户推广费月度统计
 * @date 2024/2/24 10:25:56
 */
@Service
@Slf4j
public class MerchantPromotionMonthRecordServiceImpl implements MerchantPromotionMonthRecordService {
    
    @Resource
    private MerchantPromotionMonthRecordMapper merchantPromotionMonthRecordMapper;
    
    @Resource
    private MerchantPromotionDayRecordService merchantPromotionDayRecordService;
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private UserService userService;
    
    @Slave
    @Override
    public List<MerchantPromotionMonthRecordVO> listByPage(MerchantPromotionRequest request) {
        String monthDate = request.getMonthDate();
        //年月格式校验，判断date是否yyyy-MM格式
        if (StringUtils.isNotBlank(monthDate) && monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            // 数据库存的是yyyy-MM-01
            request.setMonthDate(monthDate + "-01");
        }
        
        MerchantPromotionMonthRecordQueryModel queryModel = new MerchantPromotionMonthRecordQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        List<MerchantPromotionMonthRecord> list = merchantPromotionMonthRecordMapper.selectListByPage(queryModel);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        return list.stream().map(item -> {
            MerchantPromotionMonthRecordVO vo = new MerchantPromotionMonthRecordVO();
            BeanUtils.copyProperties(item, vo);
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantPromotionRequest request) {
        String monthDate = request.getMonthDate();
        //年月格式校验，判断date是否yyyy-MM格式
        if (StringUtils.isNotBlank(monthDate) && monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            // 数据库存的是yyyy-MM-01
            request.setMonthDate(monthDate + "-01");
        }
        
        MerchantPromotionMonthRecordQueryModel queryModel = new MerchantPromotionMonthRecordQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return merchantPromotionMonthRecordMapper.countTotal(queryModel);
    }
    
    @Slave
    @Override
    public void exportExcel(MerchantPromotionRequest request, HttpServletResponse response) {
        // 根据年月获取当月第一天和最后一天的日期
        String monthDate = request.getMonthDate();
        //年月格式校验，判断date是否yyyy-MM格式
        if (StringUtils.isBlank(monthDate) || !monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            return;
        }
        
        MerchantPromotionDayRecordQueryModel queryModel = new MerchantPromotionDayRecordQueryModel();
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        queryModel.setStartDate(DateUtils.getFirstDayByMonth(monthDate));
        queryModel.setEndDate(DateUtils.getLastDayByMonth(monthDate));
        
        List<MerchantPromotionDayRecordVO> detailList = merchantPromotionDayRecordService.listByTenantId(queryModel);
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        
        List<MerchantPromotionMonthExcelVO> excelVOList = new ArrayList<>();
        
        // excelVOList 按merchantId进行分组
        Map<Long, List<MerchantPromotionDayRecordVO>> detailMap = detailList.stream().collect(Collectors.groupingBy(MerchantPromotionDayRecordVO::getMerchantId));
        // todo 排序
        
        detailMap.forEach((merchantId, merchantDayRecordVoList) -> {
            
            if (CollectionUtils.isNotEmpty(merchantDayRecordVoList)) {
                AtomicReference<BigDecimal> firstAmount = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<BigDecimal> renewAmount = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<BigDecimal> balanceFirstAmount = new AtomicReference<>(BigDecimal.ZERO);
                AtomicReference<BigDecimal> balanceRenewAmount = new AtomicReference<>(BigDecimal.ZERO);
                
                merchantDayRecordVoList.stream().filter(record -> Objects.equals(record.getType(), RebateRecordConstant.LASHIN))
                        .forEach(record -> firstAmount.set(firstAmount.get().add(record.getMoney())));
                merchantDayRecordVoList.stream().filter(record -> Objects.equals(record.getType(), RebateRecordConstant.RENEW))
                        .forEach(record -> renewAmount.set(renewAmount.get().add(record.getMoney())));
                merchantDayRecordVoList.stream().filter(record -> Objects.equals(record.getType(), RebateRecordConstant.BALANCE)).forEach(record -> {
                    balanceFirstAmount.set(balanceFirstAmount.get().add(Objects.isNull(record.getBalanceFromFirst()) ? BigDecimal.ZERO : record.getBalanceFromFirst()));
                    balanceRenewAmount.set(balanceRenewAmount.get().add(Objects.isNull(record.getBalanceFromRenew()) ? BigDecimal.ZERO : record.getBalanceFromRenew()));
                });
                
                BigDecimal monthFirstMoney = firstAmount.get().add(balanceFirstAmount.get());
                BigDecimal monthRenewMoney = renewAmount.get().add(balanceRenewAmount.get());
                
                merchantDayRecordVoList.forEach(item -> {
                    
                    String typeName = "";
                    BigDecimal dayMoney = BigDecimal.ZERO;
                    
                    switch (item.getType()) {
                        case MerchantPromotionDayRecord.LASH:
                            typeName = RebateRecordConstant.LASH_NAME;
                            dayMoney = item.getMoney();
                            break;
                        case MerchantPromotionDayRecord.RENEW:
                            typeName = RebateRecordConstant.RENEW_NAME;
                            dayMoney = item.getMoney();
                            break;
                        case MerchantPromotionDayRecord.BALANCE:
                            typeName = RebateRecordConstant.BALANCE_NAME;
                            dayMoney = item.getMoney();
                            break;
                        default:
                            break;
                    }
                    
                    MerchantPromotionMonthExcelVO excelVO = MerchantPromotionMonthExcelVO.builder().monthDate(monthDate)
                            .merchantName(Optional.ofNullable(merchantService.queryByIdFromCache(item.getMerchantId())).orElse(new Merchant()).getName())
                            .monthFirstMoney(monthFirstMoney).monthRenewMoney(monthRenewMoney)
                            .inviterName(Optional.ofNullable(userService.queryByUidFromCache(item.getInviterUid())).orElse(new User()).getName()).typeName(typeName)
                            .dayMoney(dayMoney).date(item.getDate()).build();
                    
                    excelVOList.add(excelVO);
                });
            }
            
        });
        
        String fileName = "商户推广费出账记录.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            EasyExcel.write(outputStream, MerchantPromotionMonthExcelVO.class).head(getHeader())
                    // 合并策略：合并相同数据的行。第一个参数表示从哪一行开始进行合并，由于表头占了两行，因此从第2行开始（索引从0开始）
                    // 第二个参数是指定哪些列要进行合并
                    .registerWriteHandler(new MergeSameRowsStrategy(2, new int[] {0, 1, 2, 3})).registerWriteHandler(HeadContentCellStyle.myHorizontalCellStyleStrategy())
                    .registerWriteHandler(new CommentWriteHandler(getComments(), "xlsx")).registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                    // 注意：需要先调用registerWriteHandler()再调用sheet()方法才能使合并策略生效！！！
                    .sheet("商户推广费出账记录").doWrite(excelVOList);
        } catch (Exception e) {
            log.error("导出报表失败！", e);
        }
    }
    
    /**
     * 创建表头
     */
    private static List<List<String>> getHeader() {
        List<List<String>> headers = new ArrayList<>();
        headers.add(Arrays.asList("出账年月", "出账年月"));
        headers.add(Arrays.asList("商户汇总", "商户名称"));
        headers.add(Arrays.asList("商户汇总", "月拉新返现汇总(元)"));
        headers.add(Arrays.asList("商户汇总", "月续费返现汇总(元)"));
        headers.add(Arrays.asList("返利明细", "商户/员工姓名"));
        headers.add(Arrays.asList("返利明细", "类型"));
        headers.add(Arrays.asList("返利明细", "返现(元)"));
        headers.add(Arrays.asList("返利明细", "结算时间"));
        return headers;
    }
    
    private static List<Map<String, String>> getComments() {
        List<Map<String, String>> commentList = new ArrayList<>();
        commentList.add(CommentWriteHandler.createCommentMap("商户推广费出账记录", 1, 6, "拉新收益：本月新增用户返利；续费收益：本月续费用户返利；差额：本月商户升级后额外返利补贴。"));
        return commentList;
    }
}

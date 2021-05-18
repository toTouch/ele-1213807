package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderExcelVO;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.electricity.vo.ElectricityMemberCardOrderExcelVO;
import com.xiliulou.electricity.vo.ElectricityMemberCardOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:54
 **/
@Service
@Slf4j
public class ElectricityMemberCardOrderServiceImpl extends ServiceImpl<ElectricityMemberCardOrderMapper, ElectricityMemberCardOrder> implements ElectricityMemberCardOrderService {

    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    UserService userService;
    @Autowired
    UserOauthBindService UserOauthBindService;

    /**
     * 创建月卡订单
     *
     * @param uid
     * @param memberId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createOrder(Long uid, Integer memberId, HttpServletRequest request) {
        ElectricityPayParams electricityPayParams = electricityPayParamsService.getElectricityPayParams();
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return R.failMsg("未配置支付参数!");
        }
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND SYS_USER UID:{}", uid);
            return R.failMsg("未找到系统用户信息!");
        }
        UserOauthBind userOauthBind = UserOauthBindService.queryUserOauthBySysId(uid);

        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID:{}", uid);
            return R.failMsg("未找到用户的第三方授权信息!");
        }
        UserInfo userInfo = userInfoService.selectUserByUid(uid);

        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ",user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unusable! userInfo:{} ",userInfo);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("ELECTRICITY  ERROR! not auth! userInfo:{} ",userInfo);
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.getElectricityMemberCard(memberId);
        if (Objects.isNull(electricityMemberCard)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND MEMBER_CARD BY ID:{}", memberId);
            return R.failMsg("未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID:{}", memberId);
            return R.failMsg("月卡已禁用!");
        }

        if (Objects.nonNull(userInfo.getMemberCardExpireTime()) && Objects.nonNull(userInfo.getRemainingNumber()) &&
                userInfo.getMemberCardExpireTime() > System.currentTimeMillis() &&
                (ObjectUtil.equal(ElectricityMemberCard.UN_LIMITED_COUNT, userInfo.getRemainingNumber()) || userInfo.getRemainingNumber() > 0)) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS NOT EXPIRED USERINFO:{}", userInfo);
            return R.failMsg("您的月卡还未过期,无需再次购买!");
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(memberId);
        electricityMemberCardOrder.setUid(uid);
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(electricityMemberCard.getHolidayPrice());
        electricityMemberCardOrder.setUserName(userInfo.getUserName());
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        baseMapper.insert(electricityMemberCardOrder);
        //支付零元
        if(electricityMemberCardOrder.getPayAmount().compareTo(BigDecimal.valueOf(0.01))<0){
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setId(userInfo.getId());
            Long memberCardExpireTime = System.currentTimeMillis() +
                    electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
            userInfoUpdate.setMemberCardExpireTime(memberCardExpireTime);
            userInfoUpdate.setRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
            userInfoUpdate.setCardName(electricityMemberCardOrder.getCardName());
            userInfoUpdate.setCardType(electricityMemberCardOrder.getMemberCardType());
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateById(userInfoUpdate);
            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
            electricityMemberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            baseMapper.updateById(electricityMemberCardOrderUpdate);
            return R.ok();
        }
        Pair<Boolean, Object> getPayParamsPair =
                electricityTradeOrderService.createTradeOrderAndGetPayParams(electricityMemberCardOrder, electricityPayParams, userOauthBind.getThirdId(), request);
        if (!getPayParamsPair.getLeft()) {
            return R.failMsg(getPayParamsPair.getRight().toString());
        }
        return R.ok(getPayParamsPair.getRight());
    }

    @Override
    public BigDecimal homeOne(Long first, Long now,List<Integer> cardIdList) {
        return baseMapper.homeOne(first, now,cardIdList);
    }

    @Override
    public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay,List<Integer> cardIdList) {
        return baseMapper.homeThree(startTimeMilliDay, endTimeMilliDay,cardIdList);
    }


    @Override
    public R getMemberCardOrderPage(Long uid, Long offset, Long size, Long startTime, Long endTime) {
        return R.ok(baseMapper.getMemberCardOrderPage(uid, offset, size, startTime, endTime));
    }

    /**
     * 获取交易次数
     *
     * @param uid
     * @return
     */
    @Override
    public R getMemberCardOrderCount(Long uid, Long startTime, Long endTime) {
        return R.ok(baseMapper.getMemberCardOrderCount(uid, startTime, endTime));
    }

    /**
     * 获取最近的电池交易记录
     *
     * @param uid
     * @return
     */
    @Override
    public ElectricityMemberCardOrder getRecentOrder(Long uid) {
        return baseMapper.getRecentOrder(uid);
    }

    @Override
    @DS("slave_1")
    public R memberCardOrderPage(Long offset, Long size, MemberCardOrderQuery memberCardOrderQuery) {
        Page page = PageUtil.getPage(offset, size);

        return R.ok(baseMapper.memberCardOrderPage(page, memberCardOrderQuery));
    }

    @Override
    public void exportExcel(MemberCardOrderQuery memberCardOrderQuery, HttpServletResponse response) {
        Page page = PageUtil.getPage(0L,2000L);
        baseMapper.memberCardOrderPage(page,  memberCardOrderQuery);
        if (ObjectUtil.isEmpty(page.getRecords())) {
            return ;
        }
        List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOList = page.getRecords();
        if (!DataUtil.collectionIsUsable(electricityMemberCardOrderVOList)) {
            return;
        }

        List<ElectricityMemberCardOrderExcelVO> electricityMemberCardOrderExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (ElectricityMemberCardOrderVO electricityMemberCardOrderVO : electricityMemberCardOrderVOList) {
            index++;
            ElectricityMemberCardOrderExcelVO excelVo = new ElectricityMemberCardOrderExcelVO();
            excelVo.setId(index);
            excelVo.setOrderId(electricityMemberCardOrderVO.getOrderId());
            excelVo.setPhone(electricityMemberCardOrderVO.getPhone());
            if (Objects.nonNull(electricityMemberCardOrderVO.getUpdateTime())) {
                excelVo.setBeginningTime(simpleDateFormat.format(new Date(electricityMemberCardOrderVO.getUpdateTime())));
                if (Objects.nonNull(electricityMemberCardOrderVO.getValidDays())) {
                    excelVo.setEndTime(simpleDateFormat.format(new Date(electricityMemberCardOrderVO.getUpdateTime()+electricityMemberCardOrderVO.getValidDays()*24*60*60*1000)));
                }
            }

            if (Objects.isNull(electricityMemberCardOrderVO.getMemberCardType())) {
                excelVo.setMemberCardType("");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_MONTH_CARD)) {
                excelVo.setMemberCardType("月卡");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_SEASON_CARD)) {
                excelVo.setMemberCardType("季卡");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_YEAR_CARD)) {
                excelVo.setMemberCardType("年卡");
            }
            if (Objects.isNull(electricityMemberCardOrderVO.getStatus())) {
                excelVo.setStatus("");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_INIT)) {
                excelVo.setStatus("未支付");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_SUCCESS)) {
                excelVo.setStatus("支付成功");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_FAIL)) {
                excelVo.setStatus("支付失败");
            }

            electricityMemberCardOrderExcelVOS.add(excelVo);

            String fileName = "购卡订单报表.xlsx";
            try {
                ServletOutputStream outputStream = response.getOutputStream();
                // 告诉浏览器用什么软件可以打开此文件
                response.setHeader("content-Type", "application/vnd.ms-excel");
                // 下载文件的默认名称
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
                EasyExcel.write(outputStream, ElectricityCabinetOrderExcelVO.class).sheet("sheet").doWrite(electricityMemberCardOrderExcelVOS);
                return;
            } catch (IOException e) {
                log.error("导出报表失败！", e);
            }
        }
    }
}

package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.EleUserOperateRecordMapper;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.BorrowExpireBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 换电柜电池表(TEleUserOperateRecord)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Service
@Slf4j
public class EleUserOperateRecordServiceImpl extends ServiceImpl<EleUserOperateRecordMapper, EleUserOperateRecord> implements EleUserOperateRecordService {

    @Resource
    EleUserOperateRecordMapper eleUserOperateRecordMapper;

    @Override
    public void insert(EleUserOperateRecord eleUserOperateRecord) {
        eleUserOperateRecordMapper.insert(eleUserOperateRecord);
    }

    @Override
    public R queryList(Long uid,Long size,Long offset) {
        return R.ok(eleUserOperateRecordMapper.queryList(uid,size,offset));
    }

    @Override
    public R queryCount(Long uid) {
        return R.ok(eleUserOperateRecordMapper.selectCount(new LambdaQueryWrapper<EleUserOperateRecord>().eq(EleUserOperateRecord::getUid, uid)));
    }
}

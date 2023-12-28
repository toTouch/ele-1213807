package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.TenantNoteRecharge;
import com.xiliulou.electricity.mapper.TenantNoteRechargeMapper;
import com.xiliulou.electricity.queryModel.tenantNote.TenantNoteRechargeQueryModel;
import com.xiliulou.electricity.request.tenantNote.TenantRechargePageRequest;
import com.xiliulou.electricity.service.TenantNoteRechargeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.vo.TenantNoteRechargeVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author maxiaodong
 * @date 2023/12/28 10:57
 * @desc
 */

@Service
@Slf4j
public class TenantNoteRechargeServiceImpl implements TenantNoteRechargeService {
    @Resource
    private TenantNoteRechargeMapper noteRechargeMapper;
    
    @Resource
    private UserService userService;
    
    @Override
    public int insertOne(TenantNoteRecharge recharge) {
        return noteRechargeMapper.insertOne(recharge);
    }
    
    @Slave
    @Override
    public Integer countTotal(TenantRechargePageRequest request) {
        TenantNoteRechargeQueryModel queryModel = TenantNoteRechargeQueryModel.builder().tenantId(request.getTenantId()).build();
        return noteRechargeMapper.countTotal(queryModel);
    }
    
    @Slave
    @Override
    public List<TenantNoteRechargeVo> listByPage(TenantRechargePageRequest request) {
        TenantNoteRechargeQueryModel queryModel = TenantNoteRechargeQueryModel.builder().tenantId(request.getTenantId()).build();
        List<TenantNoteRecharge> tenantNotes = noteRechargeMapper.selectListByPage(queryModel);
        if (ObjectUtils.isEmpty(tenantNotes)) {
            return Collections.EMPTY_LIST;
        }
        
        List<TenantNoteRechargeVo> list = new ArrayList<>();
        tenantNotes.parallelStream().forEach(item -> {
            TenantNoteRechargeVo vo = new TenantNoteRechargeVo();
            BeanUtils.copyProperties(item, vo);
            
            Optional.ofNullable(userService.queryByUidFromCache(item.getUid())).ifPresent(user -> {
                vo.setUName(user.getName());
            });
            list.add(vo);
        });
        
        return list;
    }
}

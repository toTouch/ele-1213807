package com.xiliulou.electricity.mapper.lostuser;


import com.xiliulou.electricity.entity.lostuser.LostUserFirstOrderRecord;

/**
 * 流失用户记录(LostUserRecord)表数据库访问层
 *
 * @author maxiaodong
 * @since 2024-10-09 11:39:04
 */

public interface LostUserFirstOrderRecordMapper {
    
    int insert(LostUserFirstOrderRecord lostUserFirstOrderRecord);
}


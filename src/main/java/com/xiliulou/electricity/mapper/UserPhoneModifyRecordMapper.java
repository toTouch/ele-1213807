package com.xiliulou.electricity.mapper;


import com.xiliulou.electricity.entity.UserPhoneModifyRecord;
import org.springframework.stereotype.Repository;

/**
 * @author HeYafeng
 * @description 资产调拨详情Mapper
 * @date 2023/11/20 11:30:12
 */

@Repository
public interface UserPhoneModifyRecordMapper {
    Integer insertOne(UserPhoneModifyRecord userPhoneModifyRecord);
}

package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Province;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * (Province)表数据库访问层
 *
 * @author makejava
 * @since 2021-01-21 18:05:46
 */
public interface ProvinceMapper  extends BaseMapper<Province>{

    @Select(" select id, code, name from t_province")
	List<Province> queryAllCity();

}

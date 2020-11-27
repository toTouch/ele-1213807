package com.xiliulou.electricity.entity;





                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    
                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    

                                            import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 换电柜文件表(TElectricityCabinetFile)实体类
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_file")
public class ElectricityCabinetFile {
    
    private Long id;
    /**
    * 文件名称
    */
    private String name;
    /**
    * 换电柜id
    */
    private Integer electricityCabinetId;
    /**
    * 类型
    */
    private Object type;
    /**
    * 文件次序
    */
    private Object index;
    /**
    * 文件的url
    */
    private String url;
    /**
    * 保存路径
    */
    private String bucketName;
    /**
    * 是否oss 0--是 1--否
    */
    private Object isOss;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;
    /**
    * 是否删除（0-正常，1-删除）
    */
    private Object delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
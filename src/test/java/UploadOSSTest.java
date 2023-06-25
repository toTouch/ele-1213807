import com.aliyun.oss.OSSClient;
import com.xiliulou.electricity.utils.ImageUtil;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.impl.AliyunOssService;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-05-23-15:56
 */
public class UploadOSSTest {
    String accessId = "LTAI5tC4goxYbeX99GMYaHY4";
    String accessKey = "XQ8zD6nrHoJIiy6iodn843ZUlB9xS5";
    String endpoint = "oss-cn-beijing.aliyuncs.com";
    String bucket = "wm-xiliulou-test";
    String host = "https://img.wm.xiliulou.com";

    private static final String OCR_OSS_PATH = "saas/idcard/";

    private AliyunOssService aliyunOssService;

    private StorageConfig storageConfig;

    @Before
    public void init(){
        try {
            StorageConfig config = new StorageConfig();
            config.setAccessKey(accessKey);
            config.setAccessKeyId(accessId);
            config.setOssEndpoint(endpoint);
            config.setBucketName(bucket);

            this.storageConfig=config;

            OSSClient oSSClient=new OSSClient(endpoint, accessId, accessKey);

            aliyunOssService=new AliyunOssService(oSSClient, storageConfig);
        }catch (Exception e) {
            System.out.println(e);
        }

    }


    @Test
    public void testUploadPictureTime(){

        Long begin=System.currentTimeMillis();

        String localPath="D:\\20230523162210.jpg";

        //身份证正面照片
        String ocrFrontPath = OCR_OSS_PATH + "999999999999999" + "_front_" + ThreadLocalRandom.current().nextInt(9999) + ".png";
        System.out.println(ocrFrontPath);

        byte[] ocrFrontBytes = ImageUtil.base64ToImage(ImageUtil.imageToBase64(localPath));

        aliyunOssService.uploadFile(storageConfig.getBucketName(), ocrFrontPath, new ByteArrayInputStream(ocrFrontBytes));

        System.out.println(System.currentTimeMillis()-begin);

    }



}

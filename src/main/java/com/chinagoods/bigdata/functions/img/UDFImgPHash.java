package com.chinagoods.bigdata.functions.img;

import com.chinagoods.bigdata.functions.utils.img.PHash;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by xiaowei.song on 17/3/23.
 */
@Description(name = "phash"
        , value = "_FUNC_(string) - get phash code by given input img url."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFImgPHash extends UDF {

    public static final Logger logger = LoggerFactory.getLogger(UDFImgPHash.class);

    private Text result = new Text();
//    public static final String DOWNLOAD_IMG_DIR = "e:\\phash";
    public static final String DOWNLOAD_IMG_DIR = "/tmp/phash";

    public static final String DEFAULT_FEATURE_CODE = "0000";


    public UDFImgPHash() throws IOException {
        // 若目录不存在，则重新创建目录
        FileUtils.forceMkdir(new File(DOWNLOAD_IMG_DIR));
    }

    /**
     * md5 hash.
     *
     * @param text 字符串
     * @return md5 hash.
     */
    public Text evaluate(Text text) throws IOException {
        if (text == null) {
            return null;
        }
        String imageUrlStr = text.toString();
        URL imageUrl = new URL(imageUrlStr);
        Path imageFilePath = Paths.get(DOWNLOAD_IMG_DIR, RandomStringUtils.randomAlphanumeric(10) + ".jpg");
        File imageFile = imageFilePath.toFile();
        // 下载文件到本地
        FileUtils.copyURLToFile(imageUrl, imageFile);
        logger.debug("本地图片路径为: {}", imageFile.toString());
        String phashFeature;
        try {
            phashFeature = PHash.getFeatureValue(imageFile.toString());
        } finally {
            // 计算完成，清理文件
            FileUtils.forceDelete(imageFile);
        }
        if (StringUtils.isNotBlank(phashFeature)) {
            result.set(phashFeature);
        } else {
            result.set(DEFAULT_FEATURE_CODE);
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        UDFImgPHash imgPHash = new UDFImgPHash();
        System.out.println(imgPHash.evaluate(new Text("https://cdnimg.chinagoods.com/jpg/2020/05/14/e8a0c54311b5c8b967e7bbac0ef3c3ca.jpg")));
    }
}

package com.chinagoods.bigdata.functions.img;

import com.chinagoods.bigdata.functions.utils.JacksonBuilder;
import com.chinagoods.bigdata.functions.utils.img.PHash;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kong.unirest.HttpStatus;
import kong.unirest.Unirest;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by xiaowei.song on 17/3/23.
 */
@Description(name = "ocr"
        , value = "_FUNC_(string) - get ocr content by given input img url."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFImgOcr extends UDF {

    public static final Logger logger = LoggerFactory.getLogger(UDFImgOcr.class);

    private Text result = new Text();
//    public static final String DOWNLOAD_IMG_DIR = "e:\\phash";
    public static final String DOWNLOAD_IMG_DIR = "/tmp/ocr";

    public static final String DEFAULT_FEATURE_CODE = "0000";

    public UDFImgOcr() throws IOException {
        // 若目录不存在，则重新创建目录
        FileUtils.forceMkdir(new File(DOWNLOAD_IMG_DIR));
    }

    public static final String IMG_OCR_URL = "http://172.18.5.14:18501/ocr";

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
        File imgFile = imageFile;
        Map<String, Object> rspM = null;
        try {
            rspM = postAction(imgFile);
        } finally {
            // 计算完成，清理文件
            FileUtils.forceDelete(imageFile);
        }
        // 解析内容
        String content = parseContent(rspM);
        if (StringUtils.isNotBlank(content)) {
            result.set(content);
        } else {
            result.set(DEFAULT_FEATURE_CODE);
        }
        return result;
    }

    public static Map<String, Object> postAction(File f) throws JsonProcessingException {
        Map<String, Object> resultMap=new HashMap<>(2);
        kong.unirest.HttpResponse<String> response = Unirest
                .post(IMG_OCR_URL)
                .header("accept", "*/*")
                .field("image", f, "image")
                .asString();

        String statusText= response.getStatusText();
        int status = response.getStatus();

        resultMap.put("statusText", statusText);
        resultMap.put("status", status);
        String content = "{}";
        boolean isSucc = true;
        try {
            content = response.getBody();
            if(status != HttpStatus.OK) {
                isSucc = false;
            }
        } catch (Exception e) {
            isSucc = false;
            logger.error("请求错误，ocr解析图片内容失败，错误为: ", e);
        }
        resultMap.put("isSuc", isSucc);
        JsonNode rspJn = JacksonBuilder.mapper.readTree("{}");
        try {
            rspJn = JacksonBuilder.mapper.readTree(content);
        } catch (JsonProcessingException e) {
            logger.error("请求错误，ocr解析图片内容返回内容反序列化错误，错误为: ", e);
        }

        resultMap.put("responseJson", rspJn);
        return resultMap;
    }

    public String parseContent(Map<String, Object> rspM){
        StringBuffer sb = new StringBuffer("");
        JsonNode rspJn = (JsonNode) rspM.get("responseJson");
        int statusCode = rspJn.path("status_code").asInt();
        if (statusCode != HttpStatus.OK) {
            logger.error("解析返回内容失败，返回内容为: {}", rspJn);
            return sb.toString();
        }
        Iterator<JsonNode> resultsJnArr = rspJn.path("results").iterator();
        JsonNode textJn = null;
        String text;
        while (resultsJnArr.hasNext()) {
            textJn = resultsJnArr.next();
            text = textJn.path("text").asText("");
            if (StringUtils.isNotBlank(text)) {
                sb.append(text).append("\n");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        UDFImgOcr imgPHash = new UDFImgOcr();
        System.out.println(imgPHash.evaluate(new Text("https://pics0.baidu.com/feed/adaf2edda3cc7cd97b91527811cf2933b90e9129.jpeg?token=0a3291326ec23fc3e009814b41da9bb1")));
    }
}

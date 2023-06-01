package com.chinagoods.bigdata.functions.audio;

import com.chinagoods.bigdata.functions.utils.JacksonBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
 * Created by xiaowei.song on 20/4/23.
 */
@Description(name = "whisper"
        , value = "_FUNC_(string) - get audio content by given input img url."
        , extended = "Example:\n > select _FUNC_(string) from src;")
public class UDFWhisper extends UDF {

    public static final Logger logger = LoggerFactory.getLogger(UDFWhisper.class);

    private Text result = new Text();
//    public static final String DOWNLOAD_IMG_DIR = "e:\\phash";
    public static final String DOWNLOAD_IMG_DIR = "/tmp/audio";

    public static final String DEFAULT_FEATURE_CODE = "0000";

    public UDFWhisper() throws IOException {
        // 若目录不存在，则重新创建目录
        FileUtils.forceMkdir(new File(DOWNLOAD_IMG_DIR));
    }

    public static final String IMG_OCR_URL = "http://172.18.5.14:25000/whisper";

    static {
        Unirest.config()
                .socketTimeout(600000)
                .connectTimeout(600000)
                .concurrency(10, 5)
                .setDefaultHeader("Accept", "application/json")
                .followRedirects(false)
                .enableCookieManagement(false)
        ;
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
        String urlStr = text.toString();
        URL url = new URL(urlStr);
        Path filePath = Paths.get(DOWNLOAD_IMG_DIR, RandomStringUtils.randomAlphanumeric(10) + ".jpg");
        File file = filePath.toFile();
        // 下载文件到本地
        FileUtils.copyURLToFile(url, file);
        logger.debug("本地图片路径为: {}", file.toString());
        String phashFeature;
        File imgFile = file;
        Map<String, Object> rspM = null;
        try {
            rspM = postAction(imgFile);
        } finally {
            // 计算完成，清理文件
            FileUtils.forceDelete(file);
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

    public static Map<String, Object> postAction(File f) {
        Map<String, Object> resultMap=new HashMap<>(2);

        kong.unirest.HttpResponse<String> response = Unirest.post(IMG_OCR_URL)
                .header("accept", "*/*")
                .field("file", f, "file")
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
            logger.error("请求错误，whisper解析音频内容失败，错误为: ", e);
        }
        resultMap.put("isSuc", isSucc);
        JsonNode rspJn = null;
        try {
            rspJn = JacksonBuilder.mapper.readTree(content);
        } catch (JsonProcessingException e) {
            logger.error("请求错误，whisper解析音频内容返回内容反序列化错误，错误为: ", e);
        }

        resultMap.put("responseJson", rspJn);
        return resultMap;
    }

    public String parseContent(Map<String, Object> rspM){
        StringBuffer sb = new StringBuffer("");
        JsonNode rspJn = (JsonNode) rspM.get("responseJson");
        Iterator<JsonNode> resultsJnArr = rspJn.path("results").iterator();
        JsonNode textJn = null;
        String text;
        while (resultsJnArr.hasNext()) {
            textJn = resultsJnArr.next();
            text = textJn.path("transcript").asText("");
            if (StringUtils.isNotBlank(text)) {
                sb.append(text).append("\n");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        UDFWhisper imgPHash = new UDFWhisper();
        System.out.println(imgPHash.evaluate(new Text("https://pics0.baidu.com/feed/adaf2edda3cc7cd97b91527811cf2933b90e9129.jpeg?token=0a3291326ec23fc3e009814b41da9bb1")));
    }
}

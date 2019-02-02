package com.github.aaronshan.functions.geo;

import com.github.aaronshan.functions.utils.GeoUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 17:05
 */
@Description(name = "gcj_extract_wgs"
        , value = "_FUNC_(gcjLat, gcjLng) - Convert GCJ-02 to WGS-84."
        , extended = "Example:\n > select _FUNC_(gcjLat, gcjLng) from src;")
public class UDFGeoGcjExtractWgs {
    private Text result = new Text();

    public Text evaluate(double gcjLat, double gcjLng) {
        result.set(GeoUtils.GCJ02ExtractWGS84(gcjLat, gcjLng));
        return result;
    }
}

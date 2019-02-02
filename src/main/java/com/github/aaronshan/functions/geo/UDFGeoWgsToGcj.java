package com.github.aaronshan.functions.geo;

import com.github.aaronshan.functions.utils.GeoUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 17:03
 */
@Description(name = "wgs_to_gcj"
        , value = "_FUNC_(wgsLng) - Convert WGS-84 to GCJ-02."
        , extended = "Example:\n > select _FUNC_(wgsLng) from src;")
public class UDFGeoWgsToGcj {
    private Text result = new Text();

    public Text evaluate(double wgsLat, double wgsLng) {
        result.set(GeoUtils.WGS84ToGCJ02(wgsLat, wgsLng));
        return result;
    }
}

package com.github.aaronshan.functions.geo;

import com.github.aaronshan.functions.utils.GeoUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 17:02
 */
@Description(name = "gcj_to_bd"
        , value = "_FUNC_(gcjLat, gcjLng) - Convert GCJ-02 to BD-09."
        , extended = "Example:\n > select _FUNC_(gcjLat, gcjLng) from src;")
public class UDFGeoGcjToBd {
    private Text result = new Text();

    public Text evaluate(double gcjLat, double gcjLng) {
        result.set(GeoUtils.GCJ02ToBD09(gcjLat, gcjLng));
        return result;
    }
}

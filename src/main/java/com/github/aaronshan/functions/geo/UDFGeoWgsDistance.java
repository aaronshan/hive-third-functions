package com.github.aaronshan.functions.geo;

import com.github.aaronshan.functions.utils.GeoUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 17:01
 */
@Description(name = "wgs_distance"
        , value = "_FUNC_(lat1, lng1, lat2, lng2) - return WGS84 distance."
        , extended = "Example:\n > select _FUNC_(lat1, lng1, lat2, lng2) from src;")
public class UDFGeoWgsDistance {
    private DoubleWritable result = new DoubleWritable();

    public DoubleWritable evaluate(double lat1, double lng1, double lat2, double lng2) {
        result.set(GeoUtils.WGS84Distance(lat1, lng1, lat2, lng2));
        return result;
    }
}

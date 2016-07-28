package cc.shanruifeng.functions.geo;

import cc.shanruifeng.functions.utils.GeoUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-27
 * @time 17:02
 */
@Description(name = "bd_to_gcj"
        , value = "_FUNC_(bdLat, bdLng) - Convert BD-09 to GCJ-02."
        , extended = "Example:\n > select _FUNC_(bdLat, bdLng) from src;")
public class UDFGeoBdToGcj extends UDF {
    private Text result = new Text();

    public Text evaluate(double bdLat, double bdLng) {
        result.set(GeoUtils.BD09ToGCJ02(bdLat, bdLng));
        return result;
    }
}

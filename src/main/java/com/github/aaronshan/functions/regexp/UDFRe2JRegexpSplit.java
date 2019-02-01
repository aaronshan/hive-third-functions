package cc.shanruifeng.functions.regexp;

import io.airlift.slice.Slices;
import java.util.ArrayList;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

/**
 * @author ruifeng.shan
 * @date 2018-07-27
 * @time 22:38
 */
@Description(name = "regexp_split"
        , value = "_FUNC_(string, string) - returns array of strings split by pattern."
        , extended = "Example:\n > select _FUNC_(string, pattern) from src;")
public class UDFRe2JRegexpSplit extends GenericUDF {
    private static final int ARG_COUNT = 2;
    private transient ArrayList<Object> result = new ArrayList<Object>();
    private transient Re2JRegexp re2JRegexp;

    public UDFRe2JRegexpSplit() {

    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function regexp_split(string, pattern) takes exactly " + ARG_COUNT + " arguments.");
        }

        for (int i = 0; i < ARG_COUNT; i++) {
            if (!ObjectInspectorUtils.compareTypes(PrimitiveObjectInspectorFactory.javaStringObjectInspector, arguments[i])) {
                throw new UDFArgumentTypeException(i,
                        "\"" + PrimitiveObjectInspectorFactory.javaStringObjectInspector.getTypeName() + "\" "
                                + "expected at function regexp_split, but "
                                + "\"" + arguments[i].getTypeName() + "\" "
                                + "is found");
            }
        }

        ObjectInspector expectOI = PrimitiveObjectInspectorFactory.javaStringObjectInspector;

        return ObjectInspectorFactory.getStandardListObjectInspector(expectOI);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        String source = (String) arguments[0].get();
        String pattern = (String) arguments[1].get();

        if (source == null) {
            return null;
        }

        if (re2JRegexp == null) {
            re2JRegexp = new Re2JRegexp(Integer.MAX_VALUE, 5, Slices.utf8Slice(pattern));
        }

        result.clear();
        result.addAll(re2JRegexp.split(Slices.utf8Slice(source)));

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "regexp_split(" + strings[0] + ", "
                + strings[1] + ")";
    }
}
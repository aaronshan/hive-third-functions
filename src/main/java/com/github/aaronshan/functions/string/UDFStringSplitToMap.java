package cc.shanruifeng.functions.string;

import com.google.common.base.Splitter;
import java.util.HashMap;
import java.util.Map;
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

import static cc.shanruifeng.functions.utils.Failures.checkCondition;

/**
 * @author ruifeng.shan
 * @date 2018-07-27
 * @time 00:04
 */
@Description(name = "split_to_map"
        , value = "_FUNC_(string, string, string) - returns a map created using the given key/value arrays."
        , extended = "Example:\n > select _FUNC_('a=123,b=.4,c=,=d', ',', '=') from src;")
public class UDFStringSplitToMap extends GenericUDF {
    private static final int ARG_COUNT = 3; // Number of arguments to this UDF
    HashMap<String, String> result = new HashMap<String, String>();

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function split_to_map(string, string, string) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of string
        for (int i = 0; i < 3; i++) {
            if (!ObjectInspectorUtils.compareTypes(PrimitiveObjectInspectorFactory.javaStringObjectInspector, arguments[i])) {
                throw new UDFArgumentTypeException(i,
                        "\"" + PrimitiveObjectInspectorFactory.javaStringObjectInspector.getTypeName() + "\" "
                                + "expected at function split_to_map, but "
                                + "\"" + arguments[i].getTypeName() + "\" "
                                + "is found");
            }
        }

        ObjectInspector mapKeyOI = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector mapValueOI = PrimitiveObjectInspectorFactory.javaStringObjectInspector;

        return ObjectInspectorFactory.getStandardMapObjectInspector(mapKeyOI, mapValueOI);
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        String string = (String) arguments[0].get();
        String entryDelimiter = (String) arguments[1].get();
        String keyValueDelimiter = (String) arguments[2].get();

        checkCondition(entryDelimiter.length() > 0, "entryDelimiter is empty");
        checkCondition(keyValueDelimiter.length() > 0, "keyValueDelimiter is empty");
        checkCondition(!entryDelimiter.equals(keyValueDelimiter), "entryDelimiter and keyValueDelimiter must not be the same");

        if (string == null) {
            return null;
        }

        result.clear();
        Map<String, String> map = Splitter.on(entryDelimiter).withKeyValueSeparator(keyValueDelimiter).split(string);
        result.putAll(map);

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "split_to_map(" + strings[0] + ", "
                + strings[1] + ", " + strings[2] + ")";
    }
}

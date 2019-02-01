package cc.shanruifeng.functions.string;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
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
@Description(name = "split_to_multimap"
        , value = "_FUNC_(string, string, string) - creates a multimap by splitting a string into key/value pairs."
        , extended = "Example:\n > select _FUNC_('a=123,b=.4,c=,=d', ',', '=') from src;")
public class UDFStringSplitToMultimap extends GenericUDF {
    private static final int ARG_COUNT = 3; // Number of arguments to this UDF
    HashMap<String, List<String>> result = new HashMap<String, List<String>>();

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function split_to_multimap(string, string, string) takes exactly " + ARG_COUNT + " arguments.");
        }

        // Check if two argument is of string
        for (int i = 0; i < 3; i++) {
            if (!ObjectInspectorUtils.compareTypes(PrimitiveObjectInspectorFactory.javaStringObjectInspector, arguments[i])) {
                throw new UDFArgumentTypeException(i,
                        "\"" + PrimitiveObjectInspectorFactory.javaStringObjectInspector.getTypeName() + "\" "
                                + "expected at function split_to_multimap, but "
                                + "\"" + arguments[i].getTypeName() + "\" "
                                + "is found");
            }
        }

        ObjectInspector mapKeyOI = PrimitiveObjectInspectorFactory.javaStringObjectInspector;
        ObjectInspector mapValueOI = ObjectInspectorFactory.getStandardListObjectInspector(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

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

        Multimap<String, String> multimap = ArrayListMultimap.create();

        result.clear();
        List<String> list = Splitter.on(entryDelimiter).splitToList(string);
        for (String str : list) {
            String[] fields = str.split(keyValueDelimiter);
            if (fields.length != 2) {
                throw new HiveException("Key-value delimiter must appear exactly once in each entry. Bad input: " + string);
            }
            multimap.put(fields[0], fields[1]);

        }

        for (String key : multimap.keySet()) {
            result.put(key, Lists.newArrayList(multimap.get(key)));
        }

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == ARG_COUNT);
        return "split_to_multimap(" + strings[0] + ", "
                + strings[1] + ", " + strings[2] + ")";
    }
}
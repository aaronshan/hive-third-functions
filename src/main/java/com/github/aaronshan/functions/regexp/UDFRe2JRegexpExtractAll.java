package com.github.aaronshan.functions.regexp;

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
@Description(name = "regexp_extract_all"
        , value = "_FUNC_(string, string) - string(s) extracted using the given pattern\n" +
        "_FUNC_(string, string, long) - group(s) extracted using the given pattern."
        , extended = "Example:\n > select _FUNC_(string, pattern) from src;")
public class UDFRe2JRegexpExtractAll extends GenericUDF {
    private transient ArrayList<Object> result = new ArrayList<Object>();
    private transient Re2JRegexp re2JRegexp;

    public UDFRe2JRegexpExtractAll() {

    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length != 2 || arguments.length != 3) {
            throw new UDFArgumentLengthException(
                    "The function regexp_extract_all takes exactly 2 or 3 arguments.");
        }

        for (int i = 0; i < 2; i++) {
            if (!ObjectInspectorUtils.compareTypes(PrimitiveObjectInspectorFactory.javaStringObjectInspector, arguments[i])) {
                throw new UDFArgumentTypeException(i,
                        "\"" + PrimitiveObjectInspectorFactory.javaStringObjectInspector.getTypeName() + "\" "
                                + "expected at function regexp_extract_all, but "
                                + "\"" + arguments[i].getTypeName() + "\" "
                                + "is found");
            }
        }

        if (arguments.length == 3) {
            if (!ObjectInspectorUtils.compareTypes(PrimitiveObjectInspectorFactory.javaLongObjectInspector, arguments[2])) {
                throw new UDFArgumentTypeException(2,
                        "\"" + PrimitiveObjectInspectorFactory.javaLongObjectInspector.getTypeName() + "\" "
                                + "expected at function regexp_extract_all, but "
                                + "\"" + arguments[2].getTypeName() + "\" "
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
        Long groupIndex = 0L;
        if (arguments.length == 3) {
            groupIndex = (Long) arguments[2].get();
        }

        if (source == null) {
            return null;
        }

        if (re2JRegexp == null) {
            re2JRegexp = new Re2JRegexp(Integer.MAX_VALUE, 5, Slices.utf8Slice(pattern));
        }

        result.clear();
        result.addAll(re2JRegexp.extractAll(Slices.utf8Slice(source), groupIndex));

        return result;
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length == 2 || strings.length == 3);
        if (strings.length == 2) {
            return "regexp_extract_all(" + strings[0] + ", "
                    + strings[1] + ")";
        } else {
            return "regexp_extract_all(" + strings[0] + ", "
                    + strings[1] + ", " + strings[2] + ")";
        }
    }
}
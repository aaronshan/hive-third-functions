package com.github.aaronshan.functions.array;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters.Converter;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;

/**
 * @author ruifeng.shan
 * @date 2016-07-26
 * @time 17:31
 */
@Description(name = "array_join"
        , value = "_FUNC_(array<E>, delimiter, null_replacement) - concatenates the elements of the given array using the delimiter and an optional null_replacement to replace nulls."
        , extended = "Example:\n > select _FUNC_(array, delimiter) from src;\n> select _FUNC_(array, delimiter, null_replacement) from src;")
public class UDFArrayJoin extends GenericUDF {

    private static final int ARRAY_IDX = 0;
    private static final int DELIMITER_IDX = 1;
    private static final int NULL_REPLACE_IDX = 2;
    private static final int MIN_ARG_COUNT = 2; // min Number of arguments to this UDF
    private static final int MAX_ARG_COUNT = 3; // max Number of arguments to this UDF
    private transient ObjectInspector delimiterOI;
    private transient ObjectInspector nullReplaceOI;
    private transient ListObjectInspector arrayOI;
    private transient ObjectInspector arrayElementOI;
    private transient Converter delimiterConvert;
    private transient Converter nullReplaceConvert;
    private Text result;

    public UDFArrayJoin() {

    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        // Check if two arguments were passed
        if (arguments.length > MAX_ARG_COUNT || arguments.length < MIN_ARG_COUNT) {
            throw new UDFArgumentLengthException(
                    "The function array_join(array, delimiter) or array_join(array, delimiter, null_replacement) takes exactly "
                            + MIN_ARG_COUNT + " or " + MAX_ARG_COUNT + " arguments.");
        }

        // Check if ARRAY_IDX argument is of category LIST
        if (!arguments[ARRAY_IDX].getCategory().equals(ObjectInspector.Category.LIST)) {
            throw new UDFArgumentTypeException(ARRAY_IDX,
                    "\"" + org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME + "\" "
                            + "expected at function array_join, but "
                            + "\"" + arguments[ARRAY_IDX].getTypeName() + "\" "
                            + "is found");
        }

        arrayOI = (ListObjectInspector) arguments[ARRAY_IDX];
        arrayElementOI = arrayOI.getListElementObjectInspector();

        delimiterOI = arguments[DELIMITER_IDX];
        delimiterConvert = ObjectInspectorConverters.getConverter(delimiterOI, PrimitiveObjectInspectorFactory.writableStringObjectInspector);
        if (arguments.length == MAX_ARG_COUNT) {
            nullReplaceOI = arguments[NULL_REPLACE_IDX];
            nullReplaceConvert = ObjectInspectorConverters.getConverter(nullReplaceOI, PrimitiveObjectInspectorFactory.writableStringObjectInspector);
        }

        result = new Text();

        return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        result.set("");

        Object array = arguments[ARRAY_IDX].get();
        Object delimiter = arguments[DELIMITER_IDX].get();
        Object nullReplace = null;
        if (arguments.length == MAX_ARG_COUNT) {
            nullReplace = arguments[NULL_REPLACE_IDX].get();
        }

        int arrayLength = arrayOI.getListLength(array);

        // Check if array is null or empty or value is null
        if (array == null || arrayLength <= 0) {
            return result;
        }

        StringBuffer stringBuffer = new StringBuffer();

        Object listElement = arrayOI.getListElement(array, 0);
        appendElement(stringBuffer, listElement, nullReplace);
        for (int i = 1; i < arrayLength; ++i) {
            stringBuffer.append(delimiterConvert.convert(delimiter));
            listElement = arrayOI.getListElement(array, i);
            appendElement(stringBuffer, listElement, nullReplace);
        }
        result.set(stringBuffer.toString());

        return result;
    }

    private void appendElement(StringBuffer stringBuffer, Object listElement, Object nullReplace) {
        if (listElement == null) {
            if (nullReplace != null) {
                stringBuffer.append(nullReplaceConvert.convert(nullReplace));
            } else {
                stringBuffer.append(listElement);
            }
        } else {
            stringBuffer.append(listElement);
        }
    }

    @Override
    public String getDisplayString(String[] strings) {
        assert (strings.length <= MAX_ARG_COUNT || strings.length >= MIN_ARG_COUNT);
        if (strings.length == 2) {
            return "array_join(" + strings[ARRAY_IDX] + ", "
                    + strings[DELIMITER_IDX] + ")";
        } else {
            return "array_join(" + strings[ARRAY_IDX] + ", "
                    + strings[DELIMITER_IDX] + ", "
                    + strings[NULL_REPLACE_IDX] + ")";
        }

    }
}
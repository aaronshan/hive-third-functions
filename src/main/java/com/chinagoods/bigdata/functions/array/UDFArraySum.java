package com.chinagoods.bigdata.functions.array;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author ruifeng.shan
 * date: 2016-07-26
 * time: 17:32
 */
/**
 * Hive UDF to sum the elements of an array
 */
@Description(
        name = "array_sum",
        value = " sum an array of doubles"
)
public class UDFArraySum extends GenericUDF {
    private static final Logger LOG = LoggerFactory.getLogger(UDFArraySum.class);
    private ListObjectInspector listInspector;


    public Double evaluate(List<Object> strArray) {
        double total = 0.0;
        for (Object obj : strArray) {
            Object dblObj = ((PrimitiveObjectInspector) (listInspector.getListElementObjectInspector())).getPrimitiveJavaObject(obj);
            if (dblObj instanceof Number) {
                Number dblNum = (Number) dblObj;
                total += dblNum.doubleValue();
            } else {
                //// Try to coerce it otherwise
                String dblStr = (dblObj.toString());
                try {
                    Double dblCoerce = Double.parseDouble(dblStr);
                    total += dblCoerce;
                } catch (NumberFormatException formatExc) {
                    LOG.info(" Unable to interpret " + dblStr + " as a number");
                }
            }
        }
        return total;
    }

    @Override
    public Object evaluate(DeferredObject[] arg0) throws HiveException {
        List argList = listInspector.getList(arg0[0].get());
        if (argList != null)
            return evaluate(argList);
        else
            return null;
    }

    @Override
    public String getDisplayString(String[] arg0) {
        return "array_sum()";
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] arg0)
            throws UDFArgumentException {
        this.listInspector = (ListObjectInspector) arg0[0];
        LOG.info(" Sum Array input type is " + listInspector + " element = " + listInspector.getListElementObjectInspector());

        ObjectInspector returnType = PrimitiveObjectInspectorFactory.javaDoubleObjectInspector;
        return returnType;
    }
}
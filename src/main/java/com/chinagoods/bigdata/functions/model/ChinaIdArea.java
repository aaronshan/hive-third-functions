package com.chinagoods.bigdata.functions.model;

/**
 * @author ruifeng.shan
 * date: 2016-07-07
 * time: 18:20
 */
public class ChinaIdArea {
    private String province;
    private String city;
    private String area;

    public ChinaIdArea(String province, String city, String area) {
        this.province = province;
        this.city = city;
        this.area = area;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getArea() {
        return area;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ChinaIdArea{");
        sb.append("province='").append(province).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", area='").append(area).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

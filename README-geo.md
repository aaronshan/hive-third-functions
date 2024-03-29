## 当前互联网地图的坐标系现状
### 地球坐标 (WGS84)
- 国际标准，从 GPS 设备中取出的数据的坐标系
- 国际地图提供商使用的坐标系

### 火星坐标 (GCJ-02), 也叫国测局坐标系
- 中国标准，从国行移动设备中定位获取的坐标数据使用这个坐标系
- 国家规定： 国内出版的各种地图系统（包括电子形式），必须至少采用GCJ-02对地理位置进行首次加密。

### 百度坐标 (BD-09)
- 百度标准，百度 SDK，百度地图，Geocoding 使用
- (本来就乱了，百度又在火星坐标上来个二次加密)

## 开发过程需要注意的事
- 从设备获取经纬度（GPS）坐标
    * 如果使用的是百度sdk那么可以获得百度坐标（bd09）或者火星坐标（GCJ02),默认是bd09
    * 如果使用的是ios的原生定位库，那么获得的坐标是WGS84
    * 如果使用的是高德sdk,那么获取的坐标是GCJ02
- 互联网在线地图使用的坐标系
    * 火星坐标系：
        + iOS 地图（其实是高德）
    	+ Google 地图
    	+ 搜搜、阿里云、高德地图
	* 百度坐标系：
    	+ 当然只有百度地图
	* WGS84坐标系：
        + 国际标准，谷歌国外地图、osm地图等国外的地图一般都是这个

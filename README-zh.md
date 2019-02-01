# hive-third-functions 

[![Build Status](https://travis-ci.org/aaronshan/hive-third-functions.svg?branch=master)](https://travis-ci.org/aaronshan/hive-third-functions)
[![Documentation Status](https://img.shields.io/badge/docs-latest-brightgreen.svg?style=flat)](https://github.com/aaronshan/hive-third-functions/tree/master/README.md)
[![Documentation Status](https://img.shields.io/badge/中文文档-最新-brightgreen.svg)](https://github.com/aaronshan/hive-third-functions/tree/master/README-zh.md)
[![Release](https://img.shields.io/github/release/aaronshan/hive-third-functions.svg)](https://github.com/aaronshan/hive-third-functions/releases)

## 简介

hive-third-functions 包含了一些很有用的hive udf函数，特别是数组和json函数.

> 注意:
> 1. hive-third-functions支持hive-0.11.0或更高版本.
> 2. 运行`3.0.0`及以上版本需要Java8及以上

## 编译

### 1. 安装依赖

目前, jdo2-api-2.3-ec.jar 在maven中央仓库中已经不可用, 因此我们不得不自己下载并安装到本地的maven库中. 命令如下：

```
wget http://www.datanucleus.org/downloads/maven2/javax/jdo/jdo2-api/2.3-ec/jdo2-api-2.3-ec.jar -O ~/jdo2-api-2.3-ec.jar
mvn install:install-file -DgroupId=javax.jdo -DartifactId=jdo2-api -Dversion=2.3-ec -Dpackaging=jar -Dfile=~/jdo2-api-2.3-ec.jar
```

### 2. 用mvn打包 

```
cd ${project_home}
mvn clean package
```

如果你想跳过单元测试，可以这样运行：
```
cd ${project_home}
mvn clean package -DskipTests
```

命令执行完成后, 将会在target目录下生成hive-third-functions-${version}-shaded.jar文件.

你也可以直接在发布页下载打包好了最新版本 [发布页](https://github.com/aaronshan/hive-third-functions/releases).

> 当前最新的版本是 `3.0.0`

## 函数

### 1. 字符函数

| 函数| 描述 |
|:--|:--|
|pinyin(string) -> string | 将汉字转换为拼音|
|md5(string) -> string | md5 哈希|
|sha256(string) -> string |sha256 哈希|

### 2. 数组函数

| 函数| 描述 |
|:--|:--|
|array_contains(array&lt;E&gt;, E) -> boolean | 判断数组是否包含某个值.|
|array_equals(array&lt;E&gt;, array&lt;E&gt;) -> boolean | 判断两个数组是否相等.|
|array_intersect(array, array) -> array | 返回两个数组的交集.|
|array_max(array&lt;E&gt;) -> E | 返回数组中的最大值.|
|array_min(array&lt;E&gt;) -> E | 返回数组中的最小值.|
|array_join(array, delimiter, null_replacement) -> string | 使用给定的连接符来连接数组中的元素, `null_replacement`是一个可选项, 用来替代空值.|
|array_distinct(array) -> array | 移除数组中的重复元素.|
|array_position(array&lt;E&gt;, E) -> long | 返回给定元素在数组中第一次出现的位置 (如果没找到, 返回0).|
|array_remove(array&lt;E&gt;, E) -> array | 删除数组中的给定元素.|
|array_reverse(array) -> array | 反转一个数组.|
|array_sort(array) -> array | 对数组排序, 数组中的元素必需是可排序的.|
|array_concat(array, array) -> array | 连接两个数组.|
|array_value_count(array&lt;E&gt;, E) -> long | 统计数组中包含给定元素的个数.|
|array_slice(array, start, length) -> array | 对数组进行分片操作，start为正数从前开始分片, start为负数从后开始分片, 长度为指定的长度.|
|array_element_at(array&lt;E&gt;, index) -> E | 返回指定位置的数组元素. 如果索引位置 < 0, 则从尾部开始计数并返回.|
|array_shuffle(array) -> array | 对数组shuffle.|
|sequence(start, end) -> array<Long> | 生成数组序列.|
|sequence(start, end, step) -> array<Long> | 生成数组序列.|
|sequence(start_date_string, end_data_string, step) -> array<String> | 生成日期数组序列.|

### 3. map函数
| 函数| 描述 |
|:--|:--|
|map_build(x&lt;K&gt;, y&lt;V&gt;) -> map&lt;K, V&gt;| 根据指定的键/值对数组创建map.|
|map_concat(x&lt;K, V&gt;, y&lt;K, V&gt;) -> map&lt;K,V&gt; | 返回两个map的并集. 如果一个键在 `x` 和 `y`中同时出现, 那对应值来自`y`.| 
|map_element_at(map&lt;K, V&gt;, key) -> V | 如果指定的`key`存在，返回对应的值, 否则返回 `NULL` .|
|map_equals(x&lt;K, V&gt;, y&lt;K, V&gt;) -> boolean |  判断map x 和 map y是否相等.|

### 4. 日期函数

| 函数| 描述 |
|:--|:--|
|day_of_week(date_string \| date) -> int | 一周的第几天,周一返回 1, 周日返回 7, 出错返回null.|
|day_of_year(date_string \| date) -> int | 一年的第几天. 值的范围从 1 到 366.|
|zodiac_en(date_string \| date) -> string | 将日期转换为星座英文|
|zodiac_cn(date_string \| date) -> string | 将日期转换为星座中文 | 
|type_of_day(date_string \| date) -> string | 获取日期的类型(1: 法定节假日, 2: 正常周末, 3: 正常工作日 4:攒假的工作日),错误返回-1. |

### 5. json函数

| 函数| 描述 |
|:--|:--|
|json_array_get(json, jsonPath) -> array(varchar) |returns the element at the specified index into the `json_array`. The index is zero-based.|
|json_array_length(json, jsonPath) -> array(varchar) |returns the array length of `json` (a string containing a JSON array).|
|json_array_extract(json, jsonPath) -> array(varchar) |extract json array by given jsonPath.|
|json_array_extract_scalar(json, jsonPath) -> array(varchar) |like `json_array_extract`, but returns the result value as a string (as opposed to being encoded as JSON).|
|json_extract(json, jsonPath) -> array(varchar) |extract json by given jsonPath.|
|json_extract_scalar(json, jsonPath) -> array(varchar) |like `json_extract`, but returns the result value as a string (as opposed to being encoded as JSON).|
|json_size(json, jsonPath) -> array(varchar) |like `json_extract`, but returns the size of the value. For objects or arrays, the size is the number of members, and the size of a scalar value is zero.|

### 6. 位函数

| 函数| 描述 |
|:--|:--|
|bit_count(x, bits) -> bigint | count the number of bits set in `x` (treated as bits-bit signed integer) in 2’s complement representation |
|bitwise_and(x, y) -> bigint | returns the bitwise AND of `x` and `y` in 2’s complement arithmetic.|
|bitwise_not(x) -> bigint | returns the bitwise NOT of `x` in 2’s complement arithmetic. | 
|bitwise_or(x, y) -> bigint | returns the bitwise OR of `x` and `y` in 2’s complement arithmetic.|
|bitwise_xor(x, y) -> bigint | returns the bitwise XOR of `x` and `y` in 2’s complement arithmetic. | 

### 7. 中国身份证函数

| 函数| 描述 |
|:--|:--|
|id_card_province(string) -> string |从身份证号获取省份|
|id_card_city(string) -> string |从身份证号获取城市|
|id_card_area(string) -> string |从身份证号获取区/县|
|id_card_birthday(string) -> string |从身份证号获取生日|
|id_card_gender(string) -> string |从身份证号获取性别|
|is_valid_id_card(string) -> boolean |鉴定身份证号是否有效.|
|id_card_info(string) -> json |获取身份证号信息. 包活省份、城市、区县等.|

### 8. 坐标系函数 

| 函数| 描述 |
|:--|:--|
|wgs_distance(double lat1, double lng1, double lat2, double lng2) -> double | 计算 WGS84坐标距离, 单位米. |
|gcj_to_bd(double,double) -> json | GCJ-02(火星坐标系) 转为 BD-09(百度坐标系), 谷歌、高德——>百度|
|bd_to_gcj(double,double) -> json | BD-09(百度坐标系) 转为 GCJ-02(火星坐标系), 百度——>谷歌、高德|
|wgs_to_gcj(double,double) -> json | WGS84(地球坐标系) 转为 GCJ02(火星坐标系)|
|gcj_to_wgs(double,double) -> json | GCJ02(火星坐标系) 转为 GPS84(地球坐标系), 输出的坐标精度在1到2米.|
|gcj_extract_wgs(double,double) -> json | GCJ02(火星坐标系) 转为 GPS84, 输出的坐标精度在0.5米. 但是计算比`gcj_to_wgs`耗时长. |

> 关于互联网地图坐标系的说明见: [当前互联网地图的坐标系现状](https://github.com/aaronshan/hive-third-functions/tree/master/README-geo.md)


### 9. url函数

| 函数| 描述 |
|:--|:--|
|url_encode(value) -> string | escapes value by encoding it so that it can be safely included in URL query parameter names and values|
|url_decode(value) -> string | unescape the URL encoded value. This function is the inverse of `url_encode`. | 

### 10. 数学函数

| function| description |
|:--|:--|
|infinity() -> double | 获取正无穷常数|
|is_finite(x) -> boolean | 判断x是否为有限数值|
|is_infinite(x) -> boolean |判断x是否为无穷数值|
|is_nan(x) -> boolean | 判断x是否不是一个数值类型的变量|
|nan() -> double | 获取一个表示NAN（not-a-number）的常数 |
|from_base(string, radix) -> bigint | 获取字面量的值，该值的基数为radix|
|to_base(x, radix) -> varchar | 返回x以radix为基数的字面量|
|cosine_similarity(x, y) -> double | 返回两个稀疏向量的余弦相似度|


## 用法

将下面这些内容写入 `${HOME}/.hiverc` 文件, 或者也可以按需在hive命令行环境中执行.

```
add jar ${jar_location_dir}/hive-third-functions-${version}-shaded.jar
create temporary function array_contains as 'com.github.aaronshan.functions.array.UDFArrayContains';
create temporary function array_equals as 'com.github.aaronshan.functions.array.UDFArrayEquals';
create temporary function array_intersect as 'com.github.aaronshan.functions.array.UDFArrayIntersect';
create temporary function array_max as 'com.github.aaronshan.functions.array.UDFArrayMax';
create temporary function array_min as 'com.github.aaronshan.functions.array.UDFArrayMin';
create temporary function array_join as 'com.github.aaronshan.functions.array.UDFArrayJoin';
create temporary function array_distinct as 'com.github.aaronshan.functions.array.UDFArrayDistinct';
create temporary function array_position as 'com.github.aaronshan.functions.array.UDFArrayPosition';
create temporary function array_remove as 'com.github.aaronshan.functions.array.UDFArrayRemove';
create temporary function array_reverse as 'com.github.aaronshan.functions.array.UDFArrayReverse';
create temporary function array_sort as 'com.github.aaronshan.functions.array.UDFArraySort';
create temporary function array_concat as 'com.github.aaronshan.functions.array.UDFArrayConcat';
create temporary function array_value_count as 'com.github.aaronshan.functions.array.UDFArrayValueCount';
create temporary function array_slice as 'com.github.aaronshan.functions.array.UDFArraySlice';
create temporary function array_element_at as 'com.github.aaronshan.functions.array.UDFArrayElementAt';
create temporary function bit_count as 'com.github.aaronshan.functions.bitwise.UDFBitCount';
create temporary function bitwise_and as 'com.github.aaronshan.functions.bitwise.UDFBitwiseAnd';
create temporary function array_shuffle as 'com.github.aaronshan.functions.array.UDFArrayShuffle';
create temporary function sequence as 'com.github.aaronshan.functions.array.UDFSequence';
create temporary function bitwise_not as 'com.github.aaronshan.functions.bitwise.UDFBitwiseNot';
create temporary function bitwise_or as 'com.github.aaronshan.functions.bitwise.UDFBitwiseOr';
create temporary function bitwise_xor as 'com.github.aaronshan.functions.bitwise.UDFBitwiseXor';
create temporary function map_build as 'com.github.aaronshan.functions.map.UDFMapBuild';
create temporary function map_concat as 'com.github.aaronshan.functions.map.UDFMapConcat';
create temporary function map_element_at as 'com.github.aaronshan.functions.map.UDFMapElementAt';
create temporary function map_equals as 'com.github.aaronshan.functions.map.UDFMapEquals';
create temporary function day_of_week as 'com.github.aaronshan.functions.date.UDFDayOfWeek';
create temporary function day_of_year as 'com.github.aaronshan.functions.date.UDFDayOfYear';
create temporary function type_of_day as 'com.github.aaronshan.functions.date.UDFTypeOfDay'; 
create temporary function zodiac_cn as 'com.github.aaronshan.functions.date.UDFZodiacSignCn';
create temporary function zodiac_en as 'com.github.aaronshan.functions.date.UDFZodiacSignEn';
create temporary function pinyin as 'com.github.aaronshan.functions.string.UDFChineseToPinYin';
create temporary function md5 as 'com.github.aaronshan.functions.string.UDFMd5';
create temporary function sha256 as 'com.github.aaronshan.functions.string.UDFSha256';
create temporary function json_array_get as 'com.github.aaronshan.functions.json.UDFJsonArrayGet';
create temporary function json_array_length as 'com.github.aaronshan.functions.json.UDFJsonArrayLength';
create temporary function json_array_extract as 'com.github.aaronshan.functions.json.UDFJsonArrayExtract';
create temporary function json_array_extract_scalar as 'com.github.aaronshan.functions.json.UDFJsonArrayExtractScalar';
create temporary function json_extract as 'com.github.aaronshan.functions.json.UDFJsonExtract';
create temporary function json_extract_scalar as 'com.github.aaronshan.functions.json.UDFJsonExtractScalar';
create temporary function json_size as 'com.github.aaronshan.functions.json.UDFJsonSize';
create temporary function id_card_province as 'com.github.aaronshan.functions.card.UDFChinaIdCardProvince';
create temporary function id_card_city as 'com.github.aaronshan.functions.card.UDFChinaIdCardCity';
create temporary function id_card_area as 'com.github.aaronshan.functions.card.UDFChinaIdCardArea';
create temporary function id_card_birthday as 'com.github.aaronshan.functions.card.UDFChinaIdCardBirthday';
create temporary function id_card_gender as 'com.github.aaronshan.functions.card.UDFChinaIdCardGender';
create temporary function is_valid_id_card as 'com.github.aaronshan.functions.card.UDFChinaIdCardValid';
create temporary function id_card_info as 'com.github.aaronshan.functions.card.UDFChinaIdCardInfo';
create temporary function wgs_distance as 'com.github.aaronshan.functions.geo.UDFGeoWgsDistance';
create temporary function gcj_to_bd as 'com.github.aaronshan.functions.geo.UDFGeoGcjToBd';
create temporary function bd_to_gcj as 'com.github.aaronshan.functions.geo.UDFGeoBdToGcj';
create temporary function wgs_to_gcj as 'com.github.aaronshan.functions.geo.UDFGeoWgsToGcj';
create temporary function gcj_to_wgs as 'com.github.aaronshan.functions.geo.UDFGeoGcjToWgs';
create temporary function gcj_extract_wgs as 'com.github.aaronshan.functions.geo.UDFGeoGcjExtractWgs';
create temporary function url_encode as 'com.github.aaronshan.functions.url.UDFUrlEncode';
create temporary function url_decode as 'com.github.aaronshan.functions.url.UDFUrlDecode';
create temporary function infinity as 'com.github.aaronshan.functions.math.UDFMathInfinity';
create temporary function is_finite as 'com.github.aaronshan.functions.math.UDFMathIsFinite';
create temporary function is_infinite as 'com.github.aaronshan.functions.math.UDFMathIsInfinite';
create temporary function is_nan as 'com.github.aaronshan.functions.math.UDFMathIsNaN';
create temporary function nan as 'com.github.aaronshan.functions.math.UDFMathIsNaN';
create temporary function from_base as 'com.github.aaronshan.functions.math.UDFMathFromBase';
create temporary function to_base as 'com.github.aaronshan.functions.math.UDFMathToBase';
create temporary function cosine_similarity as 'com.github.aaronshan.functions.math.UDFMathCosineSimilarity';
```

你可以在hive的命令杭中使用下面的语句来查看函数的细节.
```
hive> describe function zodiac_cn;
zodiac_cn(date) - from the input date string or separate month and day arguments, returns the sing of the Zodiac.
```

或者

```
hive> describe function extended zodiac_cn;
zodiac_cn(date) - from the input date string or separate month and day arguments, returns the sing of the Zodiac.
Example:
 > select zodiac_cn(date_string) from src;
 > select zodiac_cn(month, day) from src;
```

### 示例
```
 select pinyin('中国') => zhongguo
 select md5('aaronshan') => 95686bc0483262afe170b550dd4544d1
 select sha256('aaronshan') => d16bb375433ad383169f911afdf45e209eabfcf047ba1faebdd8f6a0b39e0a32
```

```
select day_of_week('2016-07-12') => 2
select day_of_year('2016-01-01') => 1
select type_of_day('2016-10-01') => 1
select type_of_day('2016-07-16') => 2
select type_of_day('2016-07-15') => 3
select type_of_day('2016-09-18') => 4
select zodiac_cn('1989-01-08') => 魔羯座
select zodiac_en('1989-01-08') => Capricorn
```

```
select array_contains(array(16,12,18,9), 12) => true
select array_equals(array(16,12,18,9), array(16,12,18,9)) => true
select array_intersect(array(16,12,18,9,null), array(14,9,6,18,null)) => [null,9,18]
select array_max(array(16,13,12,13,18,16,9,18)) => 18
select array_min(array(16,12,18,9)) => 9
select array_join(array(16,12,18,9,null), '#','=') => 16#12#18#9#=
select array_distinct(array(16,13,12,13,18,16,9,18)) => [9,12,13,16,18]
select array_position(array(16,13,12,13,18,16,9,18), 13) => 2
select array_remove(array(16,13,12,13,18,16,9,18), 13) => [16,12,18,16,9,18]
select array_reverse(array(16,12,18,9)) => [9,18,12,16]
select array_sort(array(16,13,12,13,18,16,9,18)) => [9,12,13,13,16,16,18,18]
select array_concat(array(16,12,18,9,null), array(14,9,6,18,null)) => [16,12,18,9,null,14,9,6,18,null]
select array_value_count(array(16,13,12,13,18,16,9,18), 13) => 2
select array_slice(array(16,13,12,13,18,16,9,18), -2, 3) => [9,18]
select array_element_at(array(16,13,12,13,18,16,9,18), -1) => 18
select array_shuffle(array(16,12,18,9))
select sequence(1, 5) => [1, 2, 3, 4, 5]
select sequence(5, 1) => [5, 4, 3, 2, 1]
select sequence(1, 9, 4) => [1, 5, 9]
select sequence('2016-04-12 00:00:00', '2016-04-14 00:00:00', 24*3600*1000) => ['2016-04-12 00:00:00', '2016-04-13 00:00:00', '2016-04-14 00:00:00']
```

```
select map_build(array('key1','key2'), array(16,12)) => {"key1":16,"key2":12}
select map_concat(map_build(array('key1','key2'), array(16,12)), map_build(array('key1','key3'), array(17,18))) => {"key1":17,"key2":12,"key3":18}
select map_element_at(map_build(array('key1','key2'), array(16,12)), 'key1') => 16
select map_equals(map_build(array('key1','key2'), array(16,12)), map_build(array('key1','key2'), array(16,12))) => true
```

```
select id_card_info('110101198901084517') => {"valid":true,"area":"东城区","province":"北京市","gender":"男","city":"北京市"}
```

```
select json_array_get("[{\"a\":{\"b\":\"13\"}}, {\"a\":{\"b\":\"18\"}}, {\"a\":{\"b\":\"12\"}}]", 1); => {"a":{"b":"18"}}
select json_array_get('["a", "b", "c"]', 0); => a
select json_array_get('["a", "b", "c"]', 1); => b
select json_array_get('["c", "b", "a"]', -1); => a
select json_array_get('["c", "b", "a"]', -2); => b
select json_array_get('[]', 0); => null
select json_array_get('["a", "b", "c"]', 10); => null
select json_array_get('["c", "b", "a"]', -10); => null
select json_array_length("[{\"a\":{\"b\":\"13\"}}, {\"a\":{\"b\":\"18\"}}, {\"a\":{\"b\":\"12\"}}]"); => 3
select json_array_extract("[{\"a\":{\"b\":\"13\"}}, {\"a\":{\"b\":\"18\"}}, {\"a\":{\"b\":\"12\"}}]", "$.a.b"); => ["\"13\"","\"18\"","\"12\""]
select json_array_extract_scalar("[{\"a\":{\"b\":\"13\"}}, {\"a\":{\"b\":\"18\"}}, {\"a\":{\"b\":\"12\"}}]", "$.a.b") => ["13","18","12"]
select json_extract("{\"a\":{\"b\":\"12\"}}", "$.a.b"); => "12"
select json_extract_scalar("{\"a\":{\"b\":\"12\"}}", "$.a.b") => 12
select json_extract_scalar('[1, 2, 3]', '$[2]');
select json_extract_scalar(json, '$.store.book[0].author');
select json_size('{"x": {"a": 1, "b": 2}}', '$.x'); => 2
select json_size('{"x": [1, 2, 3]}', '$.x'); => 3
select json_size('{"x": {"a": 1, "b": 2}}', '$.x.a'); => 0
```

```
select gcj_to_bd(39.915, 116.404) => {"lng":116.41036949371029,"lat":39.92133699351022}
select bd_to_gcj(39.915, 116.404) => {"lng":116.39762729119315,"lat":39.90865673957631}
select wgs_to_gcj(39.915, 116.404) => {"lng":116.41024449916938,"lat":39.91640428150164}
select gcj_to_wgs(39.915, 116.404) => {"lng":116.39775550083061,"lat":39.91359571849836}
select gcj_extract_wgs(39.915, 116.404) => {"lng":116.39775549316407,"lat":39.913596801757805}
```

```
select url_encode('http://shanruifeng.cc/') => http%3A%2F%2Fshanruifeng.cc%2F
```

```
select cosine_similarity(map_build(array['a'], array[1.0]), map_build(array['a'], array[2.0])); => 1.0
```
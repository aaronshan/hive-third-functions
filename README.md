#hive-third-functions [![Build Status](https://travis-ci.org/aaronshan/hive-third-functions.svg?branch=master)](https://travis-ci.org/aaronshan/hive-third-functions)

## Introduction

Some useful custom hive udf functions, especial array and json functions.

> Note:
> hive-third-functions support hive-0.11.0 or higher.

## Build

### 1. install dependency

Now, jdo2-api-2.3-ec.jar not available in the maven central repository, so we have to manually install it into our local maven repository.

```
wget http://www.datanucleus.org/downloads/maven2/javax/jdo/jdo2-api/2.3-ec/jdo2-api-2.3-ec.jar -O ~/jdo2-api-2.3-ec.jar
mvn install:install-file -DgroupId=javax.jdo -DartifactId=jdo2-api -Dversion=2.3-ec -Dpackaging=jar -Dfile=~/jdo2-api-2.3-ec.jar
```

### 2. mvn package 

```
cd ${project_home}
mvn clean package
```

If you want to skip unit tests, please run:
```
cd ${project_home}
mvn clean package -DskipTests
```

It will generate hive-third-functions-${version}-shaded.jar in target directory.

You can also directly download file from [release page](https://github.com/aaronshan/hive-third-functions/releases).

> current latest version is `2.2.0`

## Functions

### 1. string functions

| function| description |
|:--|:--|
|pinyin(string) -> string | convert chinese to pinyin|
|md5(string) -> string | md5 hash|
|sha256(string) -> string |sha256 hash|

### 2. array functions

| function| description |
|:--|:--|
|array_contains(array&lt;E&gt;, E) -> boolean | whether array contains value or not.|
|array_intersect(array, array) -> array | returns the two array's intersection, without duplicates.|
|array_max(array&lt;E&gt;) -> E | returns the maximum value of input array.|
|array_min(array&lt;E&gt;) -> E | returns the minimum value of input array.|
|array_join(array, delimiter, null_replacement) -> string | concatenates the elements of the given array using the delimiter and an optional `null_replacement` to replace nulls.|
|array_distinct(array) -> array | remove duplicate values from the array.|
|array_position(array&lt;E&gt;, E) -> long | returns the position of the first occurrence of the element in array (or 0 if not found).|
|array_remove(array&lt;E&gt;, E) -> array | remove all elements that equal element from array.|
|array_reverse(array) -> array | reverse the array element.|
|array_sort(array) -> array | sorts and returns the array. The elements of array must be orderable.|
|array_concat(array, array) -> array | concatenates two arrays.|
|array_value_count(array&lt;E&gt;, E) -> long | count array's element number that element value equals given value.|
|array_slice(array, start, length) -> array | subsets array starting from index start (or starting from the end if start is negative) with a length of length.|
|array_element_at(array&lt;E&gt;, index) -> E | returns element of array at given index. If index < 0, element_at accesses elements from the last to the first.|

### 3. map functions
| function| description |
|:--|:--|
|map_build(x&lt;K&gt;, y&lt;V&gt;) -> map&lt;K, V&gt;| returns a map created using the given key/value arrays.|
|map_concat(x&lt;K, V&gt;, y&lt;K, V&gt;) -> map&lt;K,V&gt; | returns the union of two maps. If a key is found in both `x` and `y`, that key’s value in the resulting map comes from `y`.| 
|map_element_at(map&lt;K, V&gt;, key) -> V | returns value for given `key`, or `NULL` if the key is not contained in the map.|
|map_equals(x&lt;K, V&gt;, y&lt;K, V&gt;) -> boolean |  whether map x equals with map y or not.|

### 4. date functions

| function| description |
|:--|:--|
|day_of_week(date_string \| date) -> int | day of week,if monday,return 1, sunday return 7, error return null.|
|day_of_year(date_string \| date) -> int | day of year. The value ranges from 1 to 366.|
|zodiac_en(date_string \| date) -> string | convert date to zodiac|
|zodiac_cn(date_string \| date) -> string | convert date to zodiac chinese | 
|type_of_day(date_string \| date) -> string | for chinese. 获取日期的类型(1: 法定节假日, 2: 正常周末, 3: 正常工作日 4:攒假的工作日),错误返回-1. |

### 5. json functions

| function| description |
|:--|:--|
|json_array_get(json, jsonPath) -> array(varchar) |returns the element at the specified index into the `json_array`. The index is zero-based.|
|json_array_length(json, jsonPath) -> array(varchar) |returns the array length of `json` (a string containing a JSON array).|
|json_array_extract(json, jsonPath) -> array(varchar) |extract json array by given jsonPath.|
|json_array_extract_scalar(json, jsonPath) -> array(varchar) |like `json_array_extract`, but returns the result value as a string (as opposed to being encoded as JSON).|
|json_extract(json, jsonPath) -> array(varchar) |extract json by given jsonPath.|
|json_extract_scalar(json, jsonPath) -> array(varchar) |like `json_extract`, but returns the result value as a string (as opposed to being encoded as JSON).|
|json_size(json, jsonPath) -> array(varchar) |like `json_extract`, but returns the size of the value. For objects or arrays, the size is the number of members, and the size of a scalar value is zero.|

### 6. bitwise functions

| function| description |
|:--|:--|
|bit_count(x, bits) -> bigint | count the number of bits set in `x` (treated as bits-bit signed integer) in 2’s complement representation |
|bitwise_and(x, y) -> bigint | returns the bitwise AND of `x` and `y` in 2’s complement arithmetic.|
|bitwise_not(x) -> bigint | returns the bitwise NOT of `x` in 2’s complement arithmetic. | 
|bitwise_or(x, y) -> bigint | returns the bitwise OR of `x` and `y` in 2’s complement arithmetic.|
|bitwise_xor(x, y) -> bigint | returns the bitwise XOR of `x` and `y` in 2’s complement arithmetic. | 

### 7. china id card functions

| function| description |
|:--|:--|
|id_card_province(string) -> string |get user's province|
|id_card_city(string) -> string |get user's city|
|id_card_area(string) -> string |get user's area|
|id_card_birthday(string) -> string |get user's birthday|
|id_card_gender(string) -> string |get user's gender|
|is_valid_id_card(string) -> boolean |determine is valid china id card No.|
|id_card_info(string) -> json |get china id card info. include province, city, area etc.|

### 8. geographic functions 

| function| description |
|:--|:--|
|wgs_distance(double lat1, double lng1, double lat2, double lng2) -> double | calculate WGS84 coordinate distance, in meters. |
|gcj_to_bd(double,double) -> json | GCJ-02(火星坐标系) convert to BD-09(百度坐标系), 谷歌、高德——>百度|
|bd_to_gcj(double,double) -> json | BD-09(百度坐标系) convert to GCJ-02(火星坐标系), 百度——>谷歌、高德|
|wgs_to_gcj(double,double) -> json | WGS84(地球坐标系) convert to GCJ02(火星坐标系)|
|gcj_to_wgs(double,double) -> json | GCJ02(火星坐标系) convert to GPS84(地球坐标系), output coordinate WGS-84 accuracy within 1 to 2 meters.|
|gcj_extract_wgs(double,double) -> json | GCJ02(火星坐标系) convert to GPS84, output coordinate WGS-84 accuracy within 0.5 meters. but compute cost more time than `gcj_to_wgs`. |

> 关于互联网地图坐标系的说明见: [当前互联网地图的坐标系现状](https://github.com/aaronshan/hive-third-functions/tree/master/README-geo.md)


### 9. url functions

| function| description |
|:--|:--|
|url_encode(value) -> string | escapes value by encoding it so that it can be safely included in URL query parameter names and values|
|url_decode(value) -> string | unescape the URL encoded value. This function is the inverse of `url_encode`. | 

## Use

Put these statements into `${HOME}/.hiverc` or exec its on hive cli env.

```
add jar ${jar_location_dir}/hive-third-functions-${version}-shaded.jar
create temporary function array_contains as 'cc.shanruifeng.functions.array.UDFArrayContains';
create temporary function array_intersect as 'cc.shanruifeng.functions.array.UDFArrayIntersect';
create temporary function array_max as 'cc.shanruifeng.functions.array.UDFArrayMax';
create temporary function array_min as 'cc.shanruifeng.functions.array.UDFArrayMin';
create temporary function array_join as 'cc.shanruifeng.functions.array.UDFArrayJoin';
create temporary function array_distinct as 'cc.shanruifeng.functions.array.UDFArrayDistinct';
create temporary function array_position as 'cc.shanruifeng.functions.array.UDFArrayPosition';
create temporary function array_remove as 'cc.shanruifeng.functions.array.UDFArrayRemove';
create temporary function array_reverse as 'cc.shanruifeng.functions.array.UDFArrayReverse';
create temporary function array_sort as 'cc.shanruifeng.functions.array.UDFArraySort';
create temporary function array_concat as 'cc.shanruifeng.functions.array.UDFArrayConcat';
create temporary function array_value_count as 'cc.shanruifeng.functions.array.UDFArrayValueCount';
create temporary function array_slice as 'cc.shanruifeng.functions.array.UDFArraySlice';
create temporary function array_element_at as 'cc.shanruifeng.functions.array.UDFArrayElementAt';
create temporary function bit_count as 'cc.shanruifeng.functions.bitwise.UDFBitCount';
create temporary function bitwise_and as 'cc.shanruifeng.functions.bitwise.UDFBitwiseAnd';
create temporary function bitwise_not as 'cc.shanruifeng.functions.bitwise.UDFBitwiseNot';
create temporary function bitwise_or as 'cc.shanruifeng.functions.bitwise.UDFBitwiseOr';
create temporary function bitwise_xor as 'cc.shanruifeng.functions.bitwise.UDFBitwiseXor';
create temporary function map_build as 'cc.shanruifeng.functions.map.UDFMapBuild';
create temporary function map_concat as 'cc.shanruifeng.functions.map.UDFMapConcat';
create temporary function map_element_at as 'cc.shanruifeng.functions.map.UDFMapElementAt';
create temporary function map_equals as 'cc.shanruifeng.functions.map.UDFMapEquals';
create temporary function day_of_week as 'cc.shanruifeng.functions.date.UDFDayOfWeek';
create temporary function day_of_year as 'cc.shanruifeng.functions.date.UDFDayOfYear';
create temporary function type_of_day as 'cc.shanruifeng.functions.date.UDFTypeOfDay'; 
create temporary function zodiac_cn as 'cc.shanruifeng.functions.date.UDFZodiacSignCn';
create temporary function zodiac_en as 'cc.shanruifeng.functions.date.UDFZodiacSignEn';
create temporary function pinyin as 'cc.shanruifeng.functions.string.UDFChineseToPinYin';
create temporary function md5 as 'cc.shanruifeng.functions.string.UDFMd5';
create temporary function sha256 as 'cc.shanruifeng.functions.string.UDFSha256';
create temporary function json_array_get as 'cc.shanruifeng.functions.json.UDFJsonArrayGet';
create temporary function json_array_length as 'cc.shanruifeng.functions.json.UDFJsonArrayLength';
create temporary function json_array_extract as 'cc.shanruifeng.functions.json.UDFJsonArrayExtract';
create temporary function json_array_extract_scalar as 'cc.shanruifeng.functions.json.UDFJsonArrayExtractScalar';
create temporary function json_extract as 'cc.shanruifeng.functions.json.UDFJsonExtract';
create temporary function json_extract_scalar as 'cc.shanruifeng.functions.json.UDFJsonExtractScalar';
create temporary function json_size as 'cc.shanruifeng.functions.json.UDFJsonSize';
create temporary function id_card_province as 'cc.shanruifeng.functions.card.UDFChinaIdCardProvince';
create temporary function id_card_city as 'cc.shanruifeng.functions.card.UDFChinaIdCardCity';
create temporary function id_card_area as 'cc.shanruifeng.functions.card.UDFChinaIdCardArea';
create temporary function id_card_birthday as 'cc.shanruifeng.functions.card.UDFChinaIdCardBirthday';
create temporary function id_card_gender as 'cc.shanruifeng.functions.card.UDFChinaIdCardGender';
create temporary function is_valid_id_card as 'cc.shanruifeng.functions.card.UDFChinaIdCardValid';
create temporary function id_card_info as 'cc.shanruifeng.functions.card.UDFChinaIdCardInfo';
create temporary function wgs_distance as 'cc.shanruifeng.functions.geo.UDFGeoWgsDistance';
create temporary function gcj_to_bd as 'cc.shanruifeng.functions.geo.UDFGeoGcjToBd';
create temporary function bd_to_gcj as 'cc.shanruifeng.functions.geo.UDFGeoBdToGcj';
create temporary function wgs_to_gcj as 'cc.shanruifeng.functions.geo.UDFGeoWgsToGcj';
create temporary function gcj_to_wgs as 'cc.shanruifeng.functions.geo.UDFGeoGcjToWgs';
create temporary function gcj_extract_wgs as 'cc.shanruifeng.functions.geo.UDFGeoGcjExtractWgs';
create temporary function url_encode as 'cc.shanruifeng.functions.url.UDFUrlEncode';
create temporary function url_decode as 'cc.shanruifeng.functions.url.UDFUrlDecode';
```

You can use these statements on hive cli env get detail of function.
```
hive> describe function zodiac_cn;
zodiac_cn(date) - from the input date string or separate month and day arguments, returns the sing of the Zodiac.
```

or

```
hive> describe function extended zodiac_cn;
zodiac_cn(date) - from the input date string or separate month and day arguments, returns the sing of the Zodiac.
Example:
 > select zodiac_cn(date_string) from src;
 > select zodiac_cn(month, day) from src;
```

### example
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
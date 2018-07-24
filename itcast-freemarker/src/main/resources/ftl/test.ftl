<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>FreeMarker示例</title>
</head>
<body>
name=${name}；message=${message}<br>

<hr>
<#-- assign指令，这是注释，不会输出到具体的输出文件中 -->
assign指令：<br>
<#assign str="黑马"/>
${str}<br>

<#assign info={"mobile":"13488888888","address":"吉山村"} />
手机号：${info.mobile}----地址：${info.address}

<br>
<hr>
<br>
include指令：<br>
<#include "header.ftl">

<br>
<hr>
<br>
if条件控制语句：<br>
<#assign bool=false>

<#if bool>
    bool的值为true。
<#else>
    bool的值为false。
</#if>
<br>
<hr>
<br>
list循环控制语句：<br>
<#list goodsList as goods>
    索引号为：${goods_index}；名称为：${goods.name}；价格为：${goods.price}<br>
</#list>
总共有：${goodsList?size}条记录。
<br>
<hr>
<br>
内建函数使用：<br>
<#assign str='{"name":"itcast","age":12}'>
<#assign jsonObj=str?eval>
${jsonObj.name}---${jsonObj.age}<br>

日期：${today?date}<br>
时间：${today?time}<br>
日期时间：${today?datetime}<br>
格式化显示日期时间:${today?string("yyyy-MM-dd HH:mm:ss SSSS")}<br>
<br>
数值直接显示：${number}；格式化显示为字符串：${number?c}
<br>
<hr>
<br>
空值的处理<br>
在freemarker中，不可以直接显示空值，需要使用!处理.<br>
直接使用!的话则什么都不显示：${emp!}；如果为空的时候要显示内容的话，可以使用!"默认显示的值"：${emp!"emp的内容为空。"}<br>

<hr>
<br>
bool???string 前面两个??表示一个变量是否存在；如果存在则返回true，否则返回false，后面一个?表示函数调用<br>

<#assign bool2=false>

${bool2???string}<br>

<#if str3??>
    str3存在。
<#else>
    str3不存在。
</#if>







<br>
<br>
<br>
</body>
</html>
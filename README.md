TinyTools
===
Tiny tools implemented by Java Swing


### TinyTool内置支持的模板变量：
    {columnName} 获取列头对应的行字段值
    {#sheetName} 获取激活表单的名称, 
    {#tableRange(X:Y)} 将Excel当前 **单行** X-Y 列的内容显示为表格。其中XY=COLUMN()-1
    {#expand(columnName)} 表示将某列的值展开变为一个二维的表格。
                          该列的值必须是个范围，写法以excel格式为准, sheet1不能缺失。如 'sheet1'!A1:B2

### 示例员工考核Excel表格样式如下：

sn|name|receiver|copyto|month|result|memo|itemsTable
-- |-- |--|-- |--|-- |--|-- 
1 | 曲健1|nicholas.qu@mi-me.com|nicholas.qu@mi-me.com|08|A|不温不火，继续加油|'Sheet1'!A1:K11
2 | 曲健2|nicholas.qu@mi-me.com|nicholas.qu@mi-me.com|08|B|不温不火，继续加油|'Sheet2'!A1:K11
3 | 曲健3|nicholas.qu@mi-me.com|nicholas.qu@mi-me.com|08|C|不温不火，继续加油|'Sheet3'!A1:K11

说明1: 列头尽量用英文，这样方便在工具内引用，当然是支持中文的，但在某些OS上会出现乱码就比较麻烦些，启动脚本设置一下即可。

说明2: 需要expand展开的范围引用，请采用固定的格式 'sheet名字'!A1:K2 。

说明3: expand的范围，会根据列宽、背景色设置预览和邮件正文保持相同，所以若觉得展开的二维表格格式不太妥当，可以自行调整列宽和背景色。

### 示例如下：
1. 发件人用统一的邮箱、密码和显示名称不变即可。
2. 收件人，抄送人和标题都采用模板变量的方式，用示例excel的话填入的就是 {receiver} {copyto}
3. “内容模板”是邮件的正文主体，除了对列名的直接引用，可以引用单行的范围

> 如：{#tableRange(1:2)} 会引用

name|receiver
-- |-- 
曲健1|nicholas.qu@mi-me.com

4. "内容模板"展开另一个sheet里面的二维表格
> 对于 曲健1 使用 {#expand(itemsTabl)} 将会把 'Sheet1'!A1:K11 这个范围展开，比如显示为：

kpi|score
-- |-- 
服务器稳定度|100
项目质量|120
技术创新|90


### 部分截图参考：
![screenshot1](https://github.com/NicholasQu/TinyTools/blob/master/raw/screenshot1.png)

Files used
![screenshot2](https://github.com/NicholasQu/TinyTools/blob/master/raw/screenshot2.png)

new fomula support: {#expand(columnName)}
![screenshot3](https://github.com/NicholasQu/TinyTools/blob/master/raw/screenshot3.png)

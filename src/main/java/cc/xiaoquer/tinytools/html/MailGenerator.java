package cc.xiaoquer.tinytools.html;

import cc.xiaoquer.tinytools.storage.PropertiesCache;
import cc.xiaoquer.tinytools.utils.ToolUtils;
import j2html.tags.ContainerTag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetBuilder;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static j2html.TagCreator.*;

/**
 * Created by Nicholas on 2017/9/12.
 */
@Slf4j
public class MailGenerator {
    public static final String PREVIEW_FILE         = PropertiesCache.CONFIG_PATH + "/preview.html";
    public static final String PREVIEW_FILE_NEW     = PropertiesCache.CONFIG_PATH + "/preview-{date}.html";

    public static final String TAG_NEWLINE          = "\\n";
    public static final String TAG_NEWLINE_HTML     = "<br/>";

    public static final String TAG_SEMICOLON        = ";";
    public static final String TAG_SEMICOLON_ESCAPE = "&amp;";
    public static final String TAG_LEFT             = "<";
    public static final String TAG_LEFT_ESCAPE      = "&lt;";
//    public static final String TAG_LEFT_ESCAPE_AMP  = "&amp;lt;";
    public static final String TAG_RIGHT            = ">";
    public static final String TAG_RIGHT_ESCAPE     = "&gt;";
//    public static final String TAG_RIGHT_ESCAPE_AMP = "&amp;gt;";
    public static final String TAG_QUOT             = "\"";
    public static final String TAG_QUOT_ESCAPE      = "&quot;";
//    public static final String TAG_QUOT_ESCAPE_AMP  = "&amp;quot;";

    /**
     * 模板默认支持的表达式( 默认支持的是带上井号，区分自定义的字符串 ）
     *      {#sheetName}           表格的名字
     *      {#tableRange(10:12)    将Excel某行的哪些列显示成Html的横向列表
     *      {#expand(columnName)}  表示将某列的值展开变为一个二维的表格。该列的值必须是个范围，写法以excel格式为准。如 'sheet1'!A1:B2
     */
    public static final String  TPL_INNER_SHEETNAME_EXP     = "\\{#sheetName\\}"; //用来替换，无需正则

    public static final String  TPL_INNER_TABRANGE_EXP      = "\\{#tableRange\\((\\d+)\\:(\\d+)\\)\\}";
    public static final Pattern TPL_INNER_TABRANGE_PATTERN  = Pattern.compile(TPL_INNER_TABRANGE_EXP);

    public static final String  TPL_INNER_EXPAND_EXP      = "\\{#expand\\(([^#^}]+)\\)\\}"; //columnName使用 非#非} 来匹配
    public static final Pattern TPL_INNER_EXPAND_PATTERN  = Pattern.compile(TPL_INNER_EXPAND_EXP);

    public static final String TPL_INNER_SHEET_RNG_EXP = "'([^']+)'!([^:]+):([\\w]+)"; //'sheetname'!A1:B2
    public static final Pattern TPL_INNER_SHEET_RNG_PATTERN = Pattern.compile(TPL_INNER_SHEET_RNG_EXP);

    public static final String TPL_INNER_CELL_EXP = "([A-Za-z]+)([0-9]+)"; //'sheetname'!A1:B2
    public static final Pattern TPL_INNER_CELL_PATTERN = Pattern.compile(TPL_INNER_CELL_EXP);

    /**
     *
     */
    public static final String  TPL_CUSTOM_EXP              = "\\{([^#^}]+)\\}";    //{后面第一个字符必须是非井号，后面是字母数字
    public static final Pattern TPL_CUSTOM_PATTERN          = Pattern.compile(TPL_CUSTOM_EXP);

    /**
     * 产生html
     * @param text
     * @return
     */
    public static String generate(String text,
                                  Map<String, Object> excelColumnNames,
                                  Map<String, Object> dataMap,
                                  Map<String, Sheet> sheetMap,
                                  String... innerVariables) {

//        text = style().withText(HtmlCss.COMMON).withText(HtmlCss.CARD).render()
//        text = replaceTags(text).replaceAll(TAG_NEWLINE, TAG_NEWLINE_HTML);

        String htmlCode = replaceVariables(true, text, excelColumnNames, dataMap, sheetMap, innerVariables);
        htmlCode = htmlCode.replaceAll(TAG_NEWLINE, TAG_NEWLINE_HTML);

        htmlCode =
                html(
                        head(
                                meta().attr("http-equiv", "Content-Type").attr("content", "text/html;charset=utf-8"),
                                style().withText(HtmlCss.COMMON).withText(HtmlCss.CARD)
                        ),
                        body(htmlCode)
                ).render();

        htmlCode = replaceTags(htmlCode);

        return  htmlCode;
    }

    /**
     *
     * @param receiver
     * @param copy
     * @param titleText
     * @param bodyText
     * @param excelColumnNames
     * @param dataMap
     * @param sheetMap
     * @param innerVariables
     * @return
     */
    public static String generatePreview(String receiver, String copy,
                                         String titleText, String bodyText,
                                         Map<String, Object> excelColumnNames,
                                         Map<String, Object> dataMap,
                                         Map<String, Sheet> sheetMap,
                                         String... innerVariables) {


        String htmlCode = "标题：" + replaceVariables(false, titleText, excelColumnNames, dataMap, sheetMap, innerVariables);
        htmlCode += "\n收件人：" + replaceVariables(false, receiver, excelColumnNames, dataMap, sheetMap, innerVariables);
        htmlCode += "\n抄送人：" + replaceVariables(false, copy, excelColumnNames, dataMap, sheetMap, innerVariables);

        htmlCode += hr().withStyle("height:2px;border:none;border-top:2px dotted #185598;");

        htmlCode += replaceVariables(true, bodyText, excelColumnNames, dataMap, sheetMap, innerVariables);
        htmlCode = htmlCode.replaceAll(TAG_NEWLINE, TAG_NEWLINE_HTML);

        htmlCode =
            html(
                    head(
                            title("预览邮件格式"),
                            meta().attr("http-equiv", "Content-Type").attr("content", "text/html;charset=utf-8"),
                            style().withText(HtmlCss.COMMON).withText(HtmlCss.CARD)
                    ),
                    body(htmlCode)
            ).render();

        htmlCode = replaceTags(htmlCode);

        //为了让editorPanel可以自动刷新，将文件名每次改个新的值，这样可以重新载入。
        ToolUtils.deleteFiles(PropertiesCache.CONFIG_PATH, "preview-");

        File htmlFile = new File(PREVIEW_FILE_NEW.replace("{date}", System.currentTimeMillis() + ""));

        if (!htmlFile.exists()) {
            try {
                htmlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        PrintStream printStream = null;
        try{
            //打开文件
            printStream = new PrintStream(new FileOutputStream(htmlFile));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        //将HTML文件内容写入文件中
        printStream.println(htmlCode);

//        File renameFile = new File(PREVIEW_FILE_NEW.replace("{date}", System.currentTimeMillis() + ""));
//        htmlFile.renameTo(renameFile);
        System.out.println("生成成功! 路径：" + htmlFile.getAbsolutePath());

        try {
            return htmlFile.toURI().toURL().toString();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public static String replaceVariables(boolean isHtml, String templateTxt,
                                          Map<String, Object> excelColumnNames,
                                          Map<String, Object> dataMap,
                                          Map<String, Sheet> sheetMap,
                                          String... innerVariables) {
        String realtxt = templateTxt;
        String replacement;

        //过滤excel列头对应的变量值
        Matcher matcher = TPL_CUSTOM_PATTERN.matcher(realtxt);
        while (matcher.find()) {
            replacement = matcher.group(0);
            String colHeader   = matcher.group(1);
            String dataValue   = adaptCellValue((String)dataMap.get(colHeader));
            toConsole("##### Replacing {0} with ColumnHeader {1} by {2}", replacement, colHeader, dataValue);
            realtxt = realtxt.replaceAll(escapse(replacement), _highLight(dataValue, isHtml));

            matcher = TPL_CUSTOM_PATTERN.matcher(realtxt);
        }

        //过滤内部变量
        //sheetName
        realtxt = realtxt.replaceAll(TPL_INNER_SHEETNAME_EXP, _highLight(innerVariables[0], isHtml));

        //Table Range
        matcher = TPL_INNER_TABRANGE_PATTERN.matcher(realtxt);
        while (matcher.find()) {
            replacement         = matcher.group(0);
            int colStart        = Integer.parseInt(matcher.group(1));
            int colEnd          = Integer.parseInt(matcher.group(2));

            toConsole("##### Replacing {0} from colStart {1} to colEnd {2}", replacement, colStart, colEnd);

            int colCnt = 0;
            for (int col = colStart; col <= colEnd; col++) {
                String colHeader = ToolUtils.trim(excelColumnNames.get(String.valueOf(col)));
                String dataValue = adaptCellValue(ToolUtils.trim(dataMap.get(colHeader)));

                if (!ToolUtils.isBlank(dataValue)) {
                    colCnt++;
                }
            }

            ContainerTag thead_tr = tr();
            ContainerTag tbody_tr = tr();
            ContainerTag thead = thead(thead_tr);
            ContainerTag tbody = tbody(tbody_tr);
            ContainerTag table = table(thead, tbody).withId("mail_tab").withStyle("font-size: 14px;border-collapse: collapse;border: 2px solid black;");

            for (int col = colStart; col <= colEnd; col++) {
                String colHeader = ToolUtils.trim(excelColumnNames.get(String.valueOf(col)));
                String dataValue = ToolUtils.trim(dataMap.get(colHeader));

                if (!ToolUtils.isBlank(dataValue)) {
                    thead_tr.with(
                            th(colHeader).attr("width", "" + (int) (100 / colCnt) + "%")
                                    .withStyle("font-size: 14px;border-collapse: collapse;border: 2px solid black;background: yellow;")
                    );

                    tbody_tr.with(
                            td(dataValue).withStyle("font-size: 14px;border-collapse: collapse;border: 2px solid black;text-align: center;")
                    );
                }
            }
            realtxt = realtxt.replaceAll(escapse(replacement), table.render());
        }

        /*
            expand to a table
            将特殊表达式展开为一个表格进行展示
         */
        matcher = TPL_INNER_EXPAND_PATTERN.matcher(realtxt);
        while (matcher.find()) {
            replacement = matcher.group(0);
            String colHeader   = matcher.group(1);
            String dataValue   = (String)dataMap.get(colHeader);
            toConsole("##### Replacing {0} with ColumnHeader {1} by {2}", replacement, colHeader, dataValue);

            Matcher excelMatcher = TPL_INNER_SHEET_RNG_PATTERN.matcher(dataValue);
            String sheetName = null;
            String rangeStart = null;
            String rangeEnd = null;
            while (excelMatcher.find()) {
                sheetName  = excelMatcher.group(1);
                rangeStart = excelMatcher.group(2);
                rangeEnd   = excelMatcher.group(3);
            }

            if (StringUtils.isAnyBlank(sheetName, rangeStart, rangeEnd)) {
                toConsole("##### The Formula for expanding in Excel must be wrong!!!!!");
                break;
            }

            //检查sheet存在
            Sheet sheet = sheetMap.get(sheetName);
            if (sheet == null) {
                toConsole("##### The sheet [{0}] isn't existing!!!!!", sheetName);
                break;
            }

            //行列匹配器
            int colStart = 0;
            int rowStart = 0;
            int colEnd   = 0;
            int rowEnd   = 0;

            Matcher colRowMatcher = TPL_INNER_CELL_PATTERN.matcher(rangeStart);
            while (colRowMatcher.find()) {
                colStart  = ToolUtils.letterToNumber(colRowMatcher.group(1));
                rowStart = Integer.parseInt(colRowMatcher.group(2));
            }
            colRowMatcher = TPL_INNER_CELL_PATTERN.matcher(rangeEnd);
            while (colRowMatcher.find()) {
                colEnd  = ToolUtils.letterToNumber(colRowMatcher.group(1));
                rowEnd = Integer.parseInt(colRowMatcher.group(2));
            }
            toConsole("##### Parsed Column From {0} to {1}, Row from {2} to {3}", colStart, colEnd, rowStart, rowEnd);

            ContainerTag thead = thead();
            ContainerTag tbody = tbody();
            ContainerTag table = table(thead, tbody).withId("mail_tab").withStyle("font-size: 14px;border-collapse: collapse;border: 2px solid black;");

            Map<Cell, Object> mergedMap = getMergedCells(sheet);

            float totalWidth = 0;
            for (int i = rowStart - 1; i < rowEnd; i ++) {
                Row row = sheet.getRow(i);

                ContainerTag tbody_tr = tr();
                tbody.with(tbody_tr);

                for (int j = colStart - 1; j < colEnd; j++) {
                    Cell cell = row.getCell(j);

                    if (cell == null ) {
                        toConsole("????? Why Cell Is Null? Row:Col={0}:{1}", i, j);
                        continue;
                    }

                    if (mergedMap.containsKey(cell)) {
                        Object obj = mergedMap.get(cell);
                        if (obj instanceof Cell) {
                            //合并单元格的非第一单元格，不需要拼接td
                            continue;
                        }
                    }

                    float width = sheet.getColumnWidthInPixels(j);

                    //只统计一次即可
                    if (i == rowStart - 1) totalWidth += width;

                    XSSFCellStyle cellStyle = ((XSSFCell)cell).getCellStyle();

                    String align = cellStyle.getAlignment().name();
                    String valign = cellStyle.getVerticalAlignment().name();

                    XSSFColor color = cellStyle.getFillForegroundColorColor();
                    String backgroundColor = color!=null ? color.getARGBHex() : "ffffffff";

                    String backgroundHex = "#" + StringUtils.substring(backgroundColor, 2);

                    Long bgColorInt = Long.parseLong(backgroundColor,16);
                    //移位方式可参照Color的getAlpha getRed等方法
                    int color_alpha = (int)((bgColorInt >> 24) & 0xFF); //第一个是alpha值，指不透明度，ff 255表示不透明，反之0表示透明
                    int color_red   = (int)((bgColorInt >> 16) & 0xFF);
                    int color_green = (int)((bgColorInt >> 8) & 0xFF);
                    int color_blue  = (int)((bgColorInt >> 0) & 0xFF);

                    DecimalFormat fnum = new  DecimalFormat("##0.00");
                    String opacity =fnum.format(color_alpha / 255.0f);

                    //EscapeUtil默认会escape掉特殊字符，这里我们都提前转换一下
                    ContainerTag td = td(adaptCellValue(ToolUtils._getCellValue(cell)))
                            .attr("width", (int)width + "px")
                            .withStyle("font-size: 14px;border-collapse: collapse;border: 2px solid black;text-align: " + align +
                                    ";background-color:transparent; opacity:" + opacity +
                                    "; background:" + backgroundHex);


                    if (mergedMap.containsKey(cell)) {
                        Object obj = mergedMap.get(cell);
                        if (obj instanceof int[]) {
                            //合并单元格的第一行第一列
                            int[] mergedCount = (int[]) obj;
                            td.attr("rowspan", mergedCount[0]);
                            td.attr("colspan", mergedCount[1]);
                        }
                    }

                    tbody_tr.with(td);
                }
            }
            //设置表格总宽度
            table.attr("width", (int)totalWidth + "px");

            realtxt = realtxt.replaceAll(escapse(replacement), table.render());
        }

        return realtxt;
    }

    private static Map<Cell, Object> getMergedCells(Sheet sheet) {
        //情况 1：
        //  Key = 第一行第一列的单元格
        //  Value = int[]{合并了多少行，合并了多少列}
        //情况2：
        //  Key = 非第一行第一列的单元格
        //  Value = Cell 第一行第一列的单元格
        Map<Cell, Object> mergedMap = new LinkedHashMap<>();

        for (int index = 0; index < sheet.getNumMergedRegions(); index++) {
            CellRangeAddress range = sheet.getMergedRegion(index);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();

            int[] mergedCount = new int[2];
            mergedCount[0] = lastRow - firstRow + 1;
            mergedCount[1] = lastColumn - firstColumn + 1;

            Cell firstCell = sheet.getRow(firstRow).getCell(firstColumn);
            mergedMap.put(firstCell, mergedCount);

            for (int i = firstRow; i <= lastRow; i++) {
                Row row = sheet.getRow(i);

                for (int j = firstColumn; j<=lastColumn; j++) {

                    if (i == firstRow && j==firstColumn) continue;;

                    Cell otherCell = row.getCell(j);
                    mergedMap.put(otherCell, firstCell);
                }
            }
        }

        return mergedMap;
    }

    private static String _highLight(String s, boolean isHtml) {
        s = ToolUtils.trim(s);
        if (isHtml && s.length() < 6) { //大段的文字不加粗
            return b(s).render();
        } else {
            return s;
        }
    }

    //自定义的內建表达式有正则的保留字符，需要转义才能替换。
    private static String escapse(String s) {
        return s.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}")
                .replaceAll("\\:", "\\\\:")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)")
                ;
    }

    //将cell value中的html保留字符替换为全角字符，以防与html tag冲突
    private static String adaptCellValue(String origin) {
//        case '\"':
//        escapedText.append("&quot;");
//        break;
//        case '&':
//        escapedText.append("&amp;");
//        break;
//        case '\'':
//        escapedText.append("&#x27;");
//        break;
//        case '<':
//        escapedText.append("&lt;");
//        break;
//        case '>':
//        escapedText.append("&gt;");
//        break;

        String adaptiveValue = StringUtils.replace(origin, TAG_RIGHT, "＞");
        adaptiveValue = StringUtils.replace(adaptiveValue, TAG_LEFT,  "＜");
        adaptiveValue = StringUtils.replace(adaptiveValue, "\"",  "“");
        adaptiveValue = StringUtils.replace(adaptiveValue, "\'",  "‘");
        adaptiveValue = StringUtils.replace(adaptiveValue, "&",  "＆");

        return adaptiveValue;
    }



    private static String replaceTags(String s) {
         return s
                 .replaceAll(TAG_SEMICOLON_ESCAPE, TAG_SEMICOLON)
                 .replaceAll(TAG_LEFT_ESCAPE, TAG_LEFT)
//                .replaceAll(TAG_LEFT_ESCAPE_AMP, TAG_LEFT)
                .replaceAll(TAG_RIGHT_ESCAPE, TAG_RIGHT)
//                .replaceAll(TAG_RIGHT_ESCAPE_AMP, TAG_RIGHT)
                .replaceAll(TAG_QUOT_ESCAPE, TAG_QUOT)
//                .replaceAll(TAG_QUOT_ESCAPE_AMP, TAG_QUOT);
        ;
    }

    private static void toConsole(String message, Object... objs) {
        if (DEBUG) {
            System.out.println(MessageFormat.format(message, objs));
        }
    }

    private static boolean DEBUG = false;
    public static void main(String[] args) {
        DEBUG = true;
        String s = "{employeeName}，你好：\n" +
                "\n" +
                "你目前的技术级别为 {techRank}，{#sheetName} 考评结果为 {assessResult}，累计积分为 {totalPoints}’  \n" +
                "\n" +
                "{#tableRange(1:2)}" +
                "\n" +
                "{#expand(items)}";

//        System.out.println(s.replaceAll(TAG_NEWLINE, "<br/>"));

        Map<String, Object> excelColumnNames = new LinkedHashMap<String, Object>();
        excelColumnNames.put("0", "employeeName");
        excelColumnNames.put("1", "techRank");
        excelColumnNames.put("2", "assessResult");
        excelColumnNames.put("3", "totalPoints");
        excelColumnNames.put("4", "iteams");

        Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
        dataMap.put("employeeName", "张三");
        dataMap.put("techRank", "T2");
        dataMap.put("assessResult", "A");
        dataMap.put("totalPoints", "100");
        dataMap.put("items", "'summary'!A1:B10");

        Map<String, Sheet> sheetMap = new LinkedHashMap<String, Sheet>();
        sheetMap.put("summary", new XSSFWorkbook().createSheet());
        sheetMap.put("张三", new XSSFWorkbook().createSheet());

        String[] innerVariables = new String[]{"09"};
        String parsed = replaceVariables(true, s, excelColumnNames, dataMap, sheetMap, innerVariables);
        System.out.println(parsed);
    }
}

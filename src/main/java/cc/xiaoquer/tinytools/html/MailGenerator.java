package cc.xiaoquer.tinytools.html;

import cc.xiaoquer.tinytools.storage.PropertiesCache;
import cc.xiaoquer.tinytools.utils.ToolUtils;
import j2html.tags.ContainerTag;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static j2html.TagCreator.*;

/**
 * Created by Nicholas on 2017/9/12.
 */
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
     */
    public static final String  TPL_INNER_SHEETNAME_EXP     = "\\{#sheetName\\}";
    public static final String  TPL_INNER_TABRANGE_EXP      = "\\{#tableRange\\((\\d+)\\:(\\d+)\\)\\}";
    public static final Pattern TPL_INNER_TABRANGE_PATTERN  = Pattern.compile(TPL_INNER_TABRANGE_EXP);

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
    public static String generate(String text) {

//        text = style().withText(HtmlCss.COMMON).withText(HtmlCss.CARD).render()
        text = replaceTags(text).replaceAll(TAG_NEWLINE, TAG_NEWLINE_HTML);

        return  text;
    }

    /**
     *
     * @param receiver
     * @param copy
     * @param titleText
     * @param bodyText
     * @param excelColumnNames
     * @param dataMap
     * @param innerVariables
     * @return
     */
    public static String generatePreview(String receiver, String copy,
                                         String titleText, String bodyText,
                                         Map<String, Object> excelColumnNames,
                                         Map<String, Object> dataMap,
                                         String... innerVariables) {


        String htmlCode = "标题：" + replaceVariables(false, titleText, excelColumnNames, dataMap, innerVariables);
        htmlCode += "\n收件人：" + replaceVariables(false, receiver, excelColumnNames, dataMap, innerVariables);
        htmlCode += "\n抄送人：" + replaceVariables(false, copy, excelColumnNames, dataMap, innerVariables);

        htmlCode += hr().withStyle("height:2px;border:none;border-top:2px dotted #185598;");

        htmlCode += replaceVariables(true, bodyText, excelColumnNames, dataMap, innerVariables);
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
                                           Map<String, Object> dataMap, String... innerVariables) {
        String realtxt = templateTxt;
        String replacement;

        //过滤excel列头对应的变量值
        Matcher matcher = TPL_CUSTOM_PATTERN.matcher(realtxt);
        while (matcher.find()) {
            replacement = matcher.group(0);
            String colHeader   = matcher.group(1);
            String dataValue   = (String)dataMap.get(colHeader);
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

            int colCnt = 0;
            for (int col = colStart; col <= colEnd; col++) {
                String colHeader = ToolUtils.trim(excelColumnNames.get(String.valueOf(col)));
                String dataValue = ToolUtils.trim(dataMap.get(colHeader));

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

        return realtxt;
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

    public static void main(String[] args) {
        String s = "#employeeName#，你好：\n" +
                "\n" +
                "你目前的技术级别为 #techRank#，##sheetName## 考评结果为 #assessResult#，累计积分为 #totalPoints#’  \n" +
                "#templateBonus#\n" +
                "#templateMemo#\n" +
                "\n" +
                "##tableRange(10:11)##";

//        System.out.println(s.replaceAll(TAG_NEWLINE, "<br/>"));

        Matcher matcher = TPL_INNER_TABRANGE_PATTERN.matcher(s);
        if (matcher.find()) {
            System.out.println(matcher.group(0));
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
        }

        Matcher matcher1 = TPL_CUSTOM_PATTERN.matcher(s);
        while (matcher1.find()) {
            System.out.println(matcher1.group(1));
        }

    }
}

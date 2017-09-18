//package cc.xiaoquer.tinytools.html;
//
//import cc.xiaoquer.tinytools.api.JIRA;
//import cc.xiaoquer.tinytools.api.beans.JiraBoard;
//import cc.xiaoquer.tinytools.api.beans.JiraIssue;
//import cc.xiaoquer.tinytools.storage.PropertiesCache;
//import j2html.tags.ContainerTag;
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//import java.util.Set;
//
//import static j2html.TagCreator.*;
//
///**
// * Created by Nicholas on 2017/9/6.
// */
//public class HtmlGenerator {
////    public static final String HTML_FILE = PropertiesCache.CONFIG_PATH + "/JiraScrumCards-{sprintName}-{date}.html";
//    public static final String HTML_FILE = PropertiesCache.CONFIG_PATH + "/JiraScrumCards-{sprintName}.html";
//    public static final String CSS_CARD_ID = "cardview";
//
//    public static final String TAG_LEFT = "<";
//    public static final String TAG_LEFT_ESCAPE = "&lt;";
//    public static final String TAG_RIGHT = ">";
//    public static final String TAG_RIGHT_ESCAPE = "&gt;";
//    public static final String TAG_QUOT = "\"";
//    public static final String TAG_QUOT_ESCAPE = "&quot;";
//
//    private static final String PX_ONEROW_HEIGHT = "110px";
//    private static final String PX_THREEROWS_HEIGHT = "280px";
//
//    private static final int A4_CARD_COLS = 2; //A4纸多少列
//    private static final int A4_CARD_ROWS = 3; //A4纸多少行
//
//    public static final String TEMPLATE_CARD_TITLE = "<p><u><div class=\"kanban_name\">{boardName}</div></u></p>" +
//                                                     "<div class=\"parent_name\">[{parentType}][{parentKey}]: {parentName}</div>";
//    public static final String TEMPLATE_CARD_CONTENT = "<div class=\"issue_name\">[{issueType}][{issueKey}]: {issueName}</div>";
//
//    public static String generate(String sprintName, Set<JiraIssue> issueSet) {
//
//        sprintName = sprintName.replaceAll(" ", "");
//
//        String htmlPath = HTML_FILE.replace("{sprintName}", sprintName).replace("{date}", String.valueOf(System.currentTimeMillis()));
//
//        String htmlCode = renderHTML(issueSet);
//
//        PrintStream printStream = null;
//        try{
//            //打开文件
//            printStream = new PrintStream(new FileOutputStream(htmlPath));
//        }catch(FileNotFoundException e){
//            e.printStackTrace();
//        }
//
//        //将HTML文件内容写入文件中
//        printStream.println(htmlCode);
//        System.out.println("生成成功! 路径：" + htmlPath);
//        return htmlPath;
//    }
//
//    private static String renderHTML(Set<JiraIssue> issueIdSet) {
//        ContainerTag pageTable = table().withStyle("width:92%; border-spacing:20px");
//
//        int i = 1;
//
//        ContainerTag pageBreak = null;
//        ContainerTag pageNewLine = null;
//        for (JiraIssue jiraIssue : issueIdSet) {
//            //超过一页的卡片数量就是用TBody分页
//            if (i % (A4_CARD_COLS * A4_CARD_ROWS) == 1) {
//                pageBreak = tbody().withClass("sheet");
//                pageTable.with(pageBreak);
//            }
//
//            //启动新的TR
//            if (i % A4_CARD_COLS == 1) {
//                pageNewLine = tr();
//                pageBreak.with(pageNewLine);
////                pageTable.with(pageNewLine);
//            }
//
//            pageNewLine.with(
//                td().with(renderSingleCard(jiraIssue))
//            );
//
//            i++;
//        }
//
//        return
//        html(
//            head(
//                title("Jira看板打印 - v1.0"),
//                meta().attr("http-equiv", "Content-Type").attr("content", "text/html;charset=utf-8"),
//                style().withText(HtmlCss.COMMON).withText(HtmlCss.PAPER).withText(HtmlCss.CARD)
////                link().withRel("stylesheet").withHref("/css/main.css")
//            ),
//            body(pageTable).withClass("A4")
//        ).renderFormatted()
//                .replaceAll(TAG_LEFT_ESCAPE, TAG_LEFT)
//                .replaceAll(TAG_RIGHT_ESCAPE, TAG_RIGHT)
//                .replaceAll(TAG_QUOT_ESCAPE, TAG_QUOT)
//                ;
//        //EscapeUtil.class Text.class When rendering it will escape some tags.
//    }
//
//    private static ContainerTag renderSingleCard(JiraIssue jiraIssue) {
//
//        String card_bgcolor = "#88ca79"; //Story背景色
//
//        if (jiraIssue.isTask()){
//            card_bgcolor ="#d3b1e2";
//        } else if (jiraIssue.isSubTask()) {
//            card_bgcolor = "#e6a988";
//        }
//
//        JiraBoard jiraBoard = JIRA.getBoardCache(jiraIssue.getBoardId());
//        String boardName = (jiraBoard == null ? "" : jiraBoard.getBoardName());
//
//        ContainerTag cardTable = table().withId(CSS_CARD_ID).attr("bgcolor", card_bgcolor).with(
//                thead(
//                        tr(
//                                th().attr("scope","col").attr("width", "70%"),
//                                th().attr("scope","col").attr("width", "30%")
//                        )
//                ),
//                tr(
//                        td(b(TEMPLATE_CARD_TITLE
//                                .replace("{boardName}",     boardName)
//                                .replace("{parentType}",    jiraIssue.getParentType())
//                                .replace("{parentKey}",     jiraIssue.getParentKey())
//                                .replace("{parentName}",    jiraIssue.getParentName()))
//                        )
//                        .attr("colspan","2").attr("height", PX_ONEROW_HEIGHT).attr("align", "center")
//                        .attr("style", "border-top:0px none #000;")
//                )
//        );
//
//        if (jiraIssue.isStoryOrTask()) {
//            cardTable.with(
//                tr(
//                        td(TEMPLATE_CARD_CONTENT
//                                .replace("{issueType}",     jiraIssue.getIssueType())
//                                .replace("{issueKey}",      jiraIssue.getIssueKey())
//                                .replace("{issueName}",     jiraIssue.getIssueName())
//                        )
//                        .attr("rowspan","3").attr("colspan", "2").attr("height", PX_THREEROWS_HEIGHT)
//                        .attr("align", "left").attr("valign", "middle")
//                )
//            );
//        } else {
//            cardTable.with(
//                tr(
//                        td(TEMPLATE_CARD_CONTENT
//                                .replace("{issueType}",     jiraIssue.getIssueType())
//                                .replace("{issueKey}",      jiraIssue.getIssueKey())
//                                .replace("{issueName}",     jiraIssue.getIssueName())
//                        )
//                        .attr("rowspan","3").attr("height", PX_THREEROWS_HEIGHT)
//                        .attr("align", "left").attr("valign", "middle"),
//
//                        td("" + jiraIssue.getOwner()).attr("height", PX_ONEROW_HEIGHT)
//                ),
//                tr(
//                        td("估算:" + jiraIssue.getEstimate()).attr("rowspan", "2").attr("valign", "top")
//                )
//            );
//        }
//        return cardTable;
//    }
//
//    public static void main(String[] args) {
//        generate("0915", null);
//    }
//}

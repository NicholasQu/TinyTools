package cc.xiaoquer.tinytools.html;

/**
 * Created by Nicholas on 2017/9/6.
 */
public class HtmlCss {
    public static final String COMMON = "" +
            " body  { margin: 0; font-family: Microsoft YaHei UI, Arial, Helvetica, sans-serif; font-size:14px; } " +
            " ";

    public static final String CARD = "" +
            "#mail_tab  " +
            "{  " +
            "    font-family: Lucida Sans Unicode, Lucida Grande, Sans-Serif;  " +
            "    font-size: 14px;  " +
//            "    margin: 10px;  " +
//            "    width: 500px;  " +
//            "    text-align: left;  " +
            "    border-collapse: collapse;  " +
            "    border-color: black;" +
            "    border-style: solid;" +
            "    border-width: 2px;" +
            "    page-break-inside:avoid; " +
            "}  " +
            "#mail_tab thead tr  " +
            "{  " +
//            "    background: url('table-images/pattern-head.png');  " +
            "    font-family: Lucida Sans Unicode, Lucida Grande, Sans-Serif;  " +
            "    background: yellow;  " +
            "    border: 2px solid black;  " +
            "    border-collapse: collapse;  " +
            "    " +
            "}  " +
            "#mail_tab th  " +
            "{  " +
            "    font-family: Lucida Sans Unicode, Lucida Grande, Sans-Serif;  " +
            "    font-weight: bold;  " +
            "    background: yellow;  " +
            "    padding: 0px;  " +
            "    border: 2px solid black;  " +
            "    border-collapse: collapse;  " +
//            "    color: #039;  " +
//            "    border-color: black;" +
//            "    border-style: none;" +
//            "    border-width: 0px;" +
            "}  " +
            "#mail_tab td  " +
            "{  " +
//            "    font-size: 18px;  " +
//            "    padding: 0px;   " +
//            "    border-bottom: 1px solid #fff;  " +
//            "    color: ;  " +
            "    border: 2px solid black;  " +
            "    border-collapse: collapse;  " +
            "    text-align: center; " +
//            "    border-color: black;" +
//            "    border-style: solid;" +
//            "    border-width: 2px;" +
            "}  " +
//            "#cardview tbody tr:hover td  " +
//            "{  " +
//            "    color: #339;  " +
//            "    background: #fff;" +
//            "} " +
            "";

    /**
     * https://github.com/cognitom/paper-css/blob/master/examples/a4.html
     * https://cdnjs.cloudflare.com/ajax/libs/normalize/3.0.3/normalize.css
     * https://cdnjs.cloudflare.com/ajax/libs/paper-css/0.2.3/paper.css
     *
     **/
    public static final String PAPER =
            ".sheet {" +
            "    margin: 0;" +
            "    overflow: hidden;" +
            "    position: relative;" +
            "    box-sizing: border-box;" +
            "    -moz-box-sizing: border-box;" +      /* Firefox */
            "    -webkit-box-sizing: border-box;" +   /* Safari */
            "    display: inline-table;" +            //最最关键的配置！！！！！！！！！！！！！！不能用block
            "    page-break-inside: avoid;" +
            "    page-break-after: always;" +
            "    vertical-align: top;" +
            "}" +
            "" +
            "/** Paper sizes **/" +
            "body.A3           .sheet { width: 297mm; height: 419mm }" +
            "body.A3.landscape .sheet { width: 420mm; height: 296mm }" +
            "body.A4           .sheet { width: 210mm; height: 296mm }" +
            "body.A4.landscape .sheet { width: 297mm; height: 209mm }" +
            "body.A5           .sheet { width: 148mm; height: 209mm }" +
            "body.A5.landscape .sheet { width: 210mm; height: 147mm }" +
            "" +
            "/** Padding area **/" +
            ".sheet.padding-10mm { padding: 10mm }" +
            ".sheet.padding-15mm { padding: 15mm }" +
            ".sheet.padding-20mm { padding: 20mm }" +
            ".sheet.padding-25mm { padding: 25mm }" +
            "" +
            "/** For screen preview **/" +
            "@media screen {" +
            "    body { background: #e0e0e0 }" + //#e0e0e0 }" +
            "    .sheet {" +
            "       background: white;" +
            "       box-shadow: 0.5mm 2mm rgba(75, 74, 75, 1.000);" +
            "       margin: 5mm;" +
            "       vertical-align: top;" +
            "    }" +
            "}" +
            "" +
            "/** Fix for Chrome issue #273306 **/" +
            "@media print {" +
            "             body.A3.landscape { width: 420mm }" +
            "    body.A3, body.A4.landscape { width: 297mm }" +
            "    body.A4, body.A5.landscape { width: 210mm }" +
            "    body.A5                    { width: 148mm }" +
            "}";

}

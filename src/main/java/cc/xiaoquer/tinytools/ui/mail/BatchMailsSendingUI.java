/*
 * Created by JFormDesigner on Wed Sep 13 09:11:50 CST 2017
 */

package cc.xiaoquer.tinytools.ui.mail;

import cc.xiaoquer.tinytools.html.MailGenerator;
import cc.xiaoquer.tinytools.mail.MailForm;
import cc.xiaoquer.tinytools.mail.MailSender;
import cc.xiaoquer.tinytools.storage.PropertiesCache;
import cc.xiaoquer.tinytools.utils.ToolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @author Nicholas Qu
 */
@Slf4j
public class BatchMailsSendingUI extends JFrame {

    private static final String PREVIEW_DEFAULT = "生成预览中...";
    private static String ACTIVE_SHEET_NAME    = "";
    private static int CURRENT_PREVIEW_ROW     = -100;

    public BatchMailsSendingUI() {
        initComponents();
        txtExcelFile.setName("excelFile");
        txtMailSender.setName("mailSender");
        txtMailSenderName.setName("mailSenderName");
        txtMailPassword.setName("mailPassword");
        txtMailReceiver.setName("mailReceiver");
        txtMailCopy.setName("mailCopy");
        txtMailTitle.setName("mailTitle");
        txtMailBody.setName("mailBody");

        loadAllCache();
    }

    private void thisWindowActivated(WindowEvent e) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                loadAllCache();

                btnChoose.requestFocus();
            }
        });
    }

    private void loadAllCache() {
        PropertiesCache.loadCache(txtExcelFile, txtMailSender, txtMailSenderName, txtMailPassword,
                txtMailReceiver, txtMailCopy, txtMailTitle, txtMailBody);
    }

    private void saveAllCache(String backupName) {
        PropertiesCache.saveCache(txtExcelFile, txtMailSender, txtMailSenderName, txtMailPassword,
                txtMailReceiver, txtMailCopy, txtMailTitle, txtMailBody);
        //备份下文件
        PropertiesCache.backupCacheFile(backupName);
    }

    private List<Map>       DATA_MAP_LIST       = new ArrayList<>();
    private List<String>    SWING_COLUMN_NAMES  = new ArrayList();


    private int toSwingCol(int excelCol) {
        return excelCol + 2;
    }

    private int toExcelCol(int swingCol) {
        return swingCol - 2;
    }

    private boolean parseExcel() {
        String excelPath = txtExcelFile.getText();
        File excelFile = new File(excelPath);
        if (excelPath == null || excelPath.length() == 0 || !excelFile.exists()) {
            JOptionPane.showMessageDialog
                    (null, "请选择有效的考评Excel文件！", "警告", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        resetTable();

        POIFSFileSystem fileSystem = null;
        HSSFWorkbook workbook = null;
        SWING_COLUMN_NAMES.add("勾选");//第一列是checkbox
        SWING_COLUMN_NAMES.add("序号");//第二列是序号

        try {
            fileSystem = new POIFSFileSystem(new BufferedInputStream(new FileInputStream(excelFile)));
            workbook = new HSSFWorkbook(fileSystem);
            HSSFSheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());

            int    sn        = 0;

            ACTIVE_SHEET_NAME = sheet.getSheetName().trim();

            System.out.println("正在解析【" + ACTIVE_SHEET_NAME + "】的考核表格...");

            int lastRowIndex = sheet.getLastRowNum();
            for (int i = 0; i <= lastRowIndex; i++) {
                HSSFRow row = sheet.getRow(i);

                if (row == null || row.getCell(0) == null
                        || _isCellBlank(row.getCell(0))) {
                    break;
                }

                short lastCellNum = row.getLastCellNum();
                Map<String, Object> rowMap = null;
                rowMap = new LinkedHashMap<>();
                rowMap.put("check", false);
                rowMap.put("sn",    sn++);  //序号
                DATA_MAP_LIST.add(rowMap);

                for (int j = 0; j < lastCellNum; j++) {
                    HSSFCell cell = row.getCell(j);
                    String cellValue = _getCellValue(cell);

                    if (i > 0) {
                        int tmpSwingCol = toSwingCol(j);
                        if (tmpSwingCol < SWING_COLUMN_NAMES.size()) {
                            rowMap.put(SWING_COLUMN_NAMES.get(tmpSwingCol), cellValue);
                        } else {
                            //某些时候在数据行的行尾多写了点东西，这样会取不到列头
                            log.error("EXECL该行数据取不到对应的列头, row={}, col={}", i, j);
                        }
                    } else {
                        SWING_COLUMN_NAMES.add(cellValue);
                        rowMap.put("" + j, cellValue);
                    }
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
            }
            try {
                fileSystem.close();
            } catch (IOException e) {
            }
        }

        return false;
    }

    private void resetTable() {
        DATA_MAP_LIST = new ArrayList<>();
        SWING_COLUMN_NAMES = new ArrayList();
        CURRENT_PREVIEW_ROW = -100;

        DefaultTableModel model = (DefaultTableModel) tabExcel.getModel();
        model.getDataVector().clear();   //清除表格数据
        model.setRowCount(0);
        model.fireTableDataChanged();//通知模型更新
        tabExcel.updateUI();//刷新表格
    }

    private void renderTable() {
        Object [ ][ ] data = new Object [DATA_MAP_LIST.size() - 1][ ];  //动态创建第一维, 去除列头
        for (int i = 0; i < DATA_MAP_LIST.size() - 1 ; i++ ) {
            Map<String, Object> rowMap = DATA_MAP_LIST.get(i + 1);
            data [ i ] = new Object[rowMap.size() ];                    //动态创建第二维
            int j = 0;
            for( Object o : rowMap.values()) {
                data [ i ][ j ] = o;
                j++;
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, SWING_COLUMN_NAMES.toArray()) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Boolean.class;
                    case 1:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };

        tabExcel = new JTable() {

            private Border outside = new MatteBorder(2, 0, 2, 0, Color.RED);
            private Border inside = new EmptyBorder(0, 2, 0, 2);
            private Border highlight = new CompoundBorder(outside, inside);

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

//                Color top       = Color.WHITE;
                Color left      = column % 2 == 0 ? Color.ORANGE : Color.PINK;
//            Color bottom    = Color.WHITE;
//            Color right     = Color.WHITE;

                Border border = BorderFactory.createCompoundBorder();
//            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(2,0,0,0,top));
                border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,2,0,0,left));
//            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,0,2,0,bottom));
//            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,0,0,2,right));

                JComponent comp = (JComponent)super.prepareRenderer(renderer, row, column);


                comp.setBorder(border);

                int modelRow = convertRowIndexToModel(row);
                String rank = (String)getModel().getValueAt(modelRow, 5);

                Color background = row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE;
                Color fontColor = Color.BLUE;

                if ("A".equalsIgnoreCase(rank)) {
                    background = Color.GREEN;
                } else if ("C".equalsIgnoreCase(rank)) {
                    background = Color.DARK_GRAY;
                    fontColor = Color.WHITE;
                }

                comp.setForeground(fontColor);
                comp.setBackground(background);

                if (isRowSelected(row)) {
                    //使用背景色来展示选中
//                    comp.setForeground(Color.BLUE);
//                    comp.setBackground(Color.ORANGE);
                    //使用边框来显示选中
                    comp.setBorder(highlight);
                }

                return comp;
            }
        };

        tabExcel.setModel(model);

        tabExcel.setAutoCreateRowSorter(true);
        tabExcel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabExcel.setBackground(Color.white);
        tabExcel.setShowHorizontalLines(true);
        tabExcel.setShowVerticalLines(true);
        tabExcel.setOpaque(false);
        tabExcel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
        tabExcel.setSelectionBackground(new Color(102, 102, 102));

        tabExcel.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabExcel.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabExcel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tabExcel.setIntercellSpacing(new Dimension(0,0));

//        tabExcel.setDefaultRenderer(Integer.class,
//                new ColorfulRender());        //TMD, Integer 不是Object?? 只设置Object只识别String的列！！！
//        tabExcel.setDefaultRenderer(Object.class,
//                new ColorfulRender());

        ListSelectionModel selectionModel = tabExcel.getSelectionModel();

        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
//                if (!e.getValueIsAdjusting()) {
//                    getAndRenderSelectedRows();
//                    toggleRowCheck();
//                    previewMail();
//                }
            }
        });

        tabExcel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tabExcel.rowAtPoint(e.getPoint());
                int col = tabExcel.columnAtPoint(e.getPoint());
                __tableExcelClick(row, col);
            }
        });

        scrollPane2.setViewportView(tabExcel);
    }

    private void __tableExcelClick(int row, int col) {
        toggleRowCheck();
        getAndRenderSelectedRows();
        previewMail();
    }

    //切换点击行的check状态
    private void toggleRowCheck() {
        DefaultTableModel model = (DefaultTableModel)tabExcel.getModel();
        int row = tabExcel.getSelectedRow();
        int col = tabExcel.getSelectedColumn();

        boolean ischeck = (boolean)model.getValueAt(row, 0);

        if (col > 0 && !ischeck) {
            model.setValueAt(!ischeck, row, 0);
        }

    }
    private int getAndRenderSelectedRows() {
        DefaultTableModel model = (DefaultTableModel)tabExcel.getModel();
        int selectedRows = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean checked = (boolean) model.getValueAt(i, 0);
            if (checked) {
                selectedRows++;
            }
        }
        lblSelectedRows.setText("共选中 " + selectedRows + " 条记录");
        return selectedRows;
    }

    private static boolean _isCellBlank(HSSFCell cell) {
        String s = _getCellValue(cell);
        return s == null || s.trim().length() == 0;
    }

    private static String _getCellValue(HSSFCell cell) {
        if (cell==null) {
            return "";
        }
//        System.out.println("rowIdx:"+cell.getRowIndex()+",colIdx:"+cell.getColumnIndex());
        String cellValue = null;

        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        CellType type = cell.getCellTypeEnum();

        if (CellType.STRING == type) {
            cellValue = cell.getStringCellValue().trim();
        } else if (CellType.NUMERIC == type) {
//            cellValue = String.valueOf((int)cell.getNumericCellValue()).trim();
            cellValue = String.valueOf(cell.getNumericCellValue()).trim();
        } else if (CellType.BOOLEAN == type) {
            cellValue = String.valueOf(cell.getBooleanCellValue()).trim();
        } else if (CellType.FORMULA == type) {
            cellValue = evaluator.evaluateInCell(cell).toString().trim();
        } else {
            cellValue = "";
        }

        return ToolUtils.trim(cellValue.replace(".0", ""));
    }

    private void btnChooseMouseClicked(MouseEvent e) {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.getName().endsWith(".xls") || f.getName().endsWith(".xlsx")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Excel格式文件(.xls|.xlsx)";
            }
        });

        jfc.showDialog(new JLabel(), "Open");
        File file=jfc.getSelectedFile();
        if (file == null) return;
        txtExcelFile.setText(file.getAbsolutePath());
    }

    private void resetUI() {
        btnParse.setEnabled(false);
        btnParse.setText("解析Excel中...");
        lblSelectedRows.setText("共选中 条记录");
        btnSendMail.setEnabled(false);
        try {
            editorMailBodyPreview.setPage(new File(MailGenerator.PREVIEW_FILE).toURI().toURL());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void btnParseMouseClicked(MouseEvent e) {
        resetUI();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean parsed = parseExcel();
                if (parsed) renderTable();

                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                btnParse.setEnabled(true);
                btnParse.setText("▽▽ 解析Excel ▽▽");
                btnSendMail.setEnabled(true);

                PropertiesCache.saveCache(txtExcelFile);
            }
        });

    }

    private void radioAllMouseClicked(MouseEvent e) {
        checkTable(true);
    }

    private void radioNoneMouseClicked(MouseEvent e) {
        checkTable(false);
    }

    private void checkTable(boolean checked) {
        DefaultTableModel model = (DefaultTableModel)tabExcel.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(checked, i, 0);
        }

        getAndRenderSelectedRows();
    }

    private void btnSendMailMouseClicked(MouseEvent e) {
        //将当前的配置备份一哈
        saveAllCache(txtMailTitle.getText().replace(" ", ""));

        int selectedRows = getAndRenderSelectedRows();
        if (selectedRows == 0) {
            JOptionPane.showMessageDialog
                    (null, "请至少选中一行记录！", "警告", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnSendMail.setText("保存配置 & 批量发送中...");
        btnSendMail.setEnabled(false);

        int succSend = 0;
        try {
            succSend = sendMail();

            try {
                Thread.sleep(500L);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        } catch (Exception ex) {
            log.error("批量发送邮件失败", ex);
        } finally {
            btnSendMail.setText("批量发送邮件");
            btnSendMail.setEnabled(true);
        }

        StringBuffer message = new StringBuffer();
        message.append("已成功发送 ").append(succSend).append(" 封邮件.");
        if (selectedRows > succSend) {
            message.append("\n - 失败 ").append(selectedRows - succSend).append(" 封邮件.")
                    .append("\n 请至 ").append(PropertiesCache.CONFIG_PATH).append("/mailsending-fail.log查看失败明细!");
        }
        JOptionPane.showMessageDialog
                (null, message.toString(), "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private int sendMail() {
        DefaultTableModel model = (DefaultTableModel)tabExcel.getModel();
        int succSend = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean checked = (boolean)model.getValueAt(i, 0);
            if (checked) {
                String sender   = txtMailSender.getText();
                String sendername= txtMailSenderName.getText();
                String senderpwd = String.valueOf(txtMailPassword.getPassword());
                String receiver = replaceV(false, txtMailReceiver.getText(), i);
                String copy     = replaceV(false, txtMailCopy.getText(), i);
                String title    = replaceV(false, txtMailTitle.getText(), i);
                String body     = MailGenerator.generate(replaceV(true, txtMailBody.getText(), i));

                MailForm mailForm = new MailForm("smtp.exmail.qq.com",
                        sender, sendername, senderpwd, receiver, copy, title, body);

                boolean isSend = MailSender.send(mailForm);

                if (isSend) {
                    succSend ++;
                }
            }
        }

        return succSend;
    }

    private String replaceV(boolean isHtml, String replacementTxt, int selectedRow) {
        return MailGenerator.replaceVariables(isHtml, replacementTxt,
                DATA_MAP_LIST.get(0),
                DATA_MAP_LIST.get(selectedRow + 1),
                ACTIVE_SHEET_NAME);
    }

    private void txtExcelFileFocusLost(FocusEvent e) {
        // TODO add your code here
    }

    private void previewMail() {
        int _selectedRow = tabExcel.getSelectedRow();

        synchronized (this) {
            if (_selectedRow == CURRENT_PREVIEW_ROW) {
                return;
            }

            if (PREVIEW_DEFAULT.equalsIgnoreCase(editorMailBodyPreview.getToolTipText())) {
                System.out.println("正在生成预览...请稍候...");
                return;
            }
        }

        //异步preview
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                editorMailBodyPreview.setToolTipText(PREVIEW_DEFAULT);

                CURRENT_PREVIEW_ROW = _selectedRow;
                int selectedRowInMap = _selectedRow + 1;

                if (DATA_MAP_LIST.size() > 0
                        && DATA_MAP_LIST.get(selectedRowInMap) != null
                        && DATA_MAP_LIST.get(selectedRowInMap).size() > 0) {
                    try {
                        //生成preview网页
                        String previewPath = MailGenerator.generatePreview(
                                txtMailReceiver.getText(), txtMailCopy.getText(),
                                txtMailTitle.getText(), txtMailBody.getText(),
                                DATA_MAP_LIST.get(0),
                                DATA_MAP_LIST.get(selectedRowInMap),
                                ACTIVE_SHEET_NAME);

                        //展示preview网页
                        editorMailBodyPreview.setPage(previewPath);
                        editorMailBodyPreview.updateUI();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        editorMailBodyPreview.setToolTipText("预览完毕");
                    }
                }
            }
        });
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("resources");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        panel3 = new JPanel();
        txtExcelFile = new JTextField();
        btnChoose = new JButton();
        btnParse = new JButton();
        scrollPane2 = new JScrollPane();
        tabExcel = new JTable();
        panelCheck = new JPanel();
        radioAll = new JRadioButton();
        radioNone = new JRadioButton();
        lblSelectedRows = new JLabel();
        scrollPane4 = new JScrollPane();
        textArea1 = new JTextArea();
        label4 = new JLabel();
        panel4 = new JPanel();
        txtMailSender = new JTextField();
        label5 = new JLabel();
        txtMailPassword = new JPasswordField();
        txtMailSenderName = new JTextField();
        txtMailReceiver = new JTextField();
        label8 = new JLabel();
        txtMailCopy = new JTextField();
        label7 = new JLabel();
        panel5 = new JPanel();
        label3 = new JLabel();
        txtMailTitle = new JTextField();
        label2 = new JLabel();
        scrollPane1 = new JScrollPane();
        txtMailBody = new JTextArea();
        label6 = new JLabel();
        scrollPane3 = new JScrollPane();
        editorMailBodyPreview = new JEditorPane();
        panel2 = new JPanel();
        btnSendMail = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {122, 621, 0};
                ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 167, 0, 34, 0, 0, 124, 208, 32, 0};
                ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                //---- label1 ----
                label1.setText("\u9009\u62e9Excel ");
                label1.setHorizontalAlignment(SwingConstants.TRAILING);
                contentPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //======== panel3 ========
                {
                    panel3.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {326, 20, 102, 38, 142, 0};
                    ((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
                    ((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                    //---- txtExcelFile ----
                    txtExcelFile.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            txtExcelFileFocusLost(e);
                        }
                    });
                    panel3.add(txtExcelFile, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- btnChoose ----
                    btnChoose.setText("\u6253\u5f00");
                    btnChoose.setBorder(new LineBorder(Color.orange, 2, true));
                    btnChoose.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            btnChooseMouseClicked(e);
                        }
                    });
                    panel3.add(btnChoose, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- btnParse ----
                    btnParse.setText(bundle.getString("BatchMailsSendingUI.btnParse.text"));
                    btnParse.setMinimumSize(new Dimension(50, 27));
                    btnParse.setPreferredSize(new Dimension(50, 27));
                    btnParse.setBorder(new BevelBorder(BevelBorder.RAISED, Color.red, Color.red, Color.red, Color.red));
                    btnParse.setOpaque(true);
                    btnParse.setMultiClickThreshhold(1L);
                    btnParse.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            btnParseMouseClicked(e);
                        }
                    });
                    panel3.add(btnParse, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                contentPanel.add(panel3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //======== scrollPane2 ========
                {

                    //---- tabExcel ----
                    tabExcel.setAutoCreateRowSorter(true);
                    tabExcel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    tabExcel.setBackground(Color.white);
                    tabExcel.setShowHorizontalLines(true);
                    tabExcel.setShowVerticalLines(true);
                    tabExcel.setOpaque(false);
                    tabExcel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                    tabExcel.setSelectionBackground(new Color(102, 102, 102));
                    scrollPane2.setViewportView(tabExcel);
                }
                contentPanel.add(scrollPane2, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //======== panelCheck ========
                {
                    panelCheck.setLayout(new GridBagLayout());
                    ((GridBagLayout)panelCheck.getLayout()).columnWidths = new int[] {70, 84, 128, 465, 0};
                    ((GridBagLayout)panelCheck.getLayout()).rowHeights = new int[] {43, 0};
                    ((GridBagLayout)panelCheck.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panelCheck.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                    //---- radioAll ----
                    radioAll.setText(bundle.getString("BatchMailsSendingUI.radioAll.text"));
                    radioAll.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            radioAllMouseClicked(e);
                        }
                    });
                    panelCheck.add(radioAll, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- radioNone ----
                    radioNone.setText(bundle.getString("BatchMailsSendingUI.radioNone.text"));
                    radioNone.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            radioNoneMouseClicked(e);
                        }
                    });
                    panelCheck.add(radioNone, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- lblSelectedRows ----
                    lblSelectedRows.setFont(new Font("sansserif", Font.BOLD | Font.ITALIC, 12));
                    lblSelectedRows.setText(bundle.getString("BatchMailsSendingUI.lblSelectedRows.text"));
                    panelCheck.add(lblSelectedRows, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //======== scrollPane4 ========
                    {

                        //---- textArea1 ----
                        textArea1.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
                        textArea1.setOpaque(true);
                        textArea1.setBackground(Color.lightGray);
                        textArea1.setText(bundle.getString("BatchMailsSendingUI.textArea1.text"));
                        textArea1.setLineWrap(true);
                        textArea1.setFont(new Font("sansserif", Font.PLAIN, 9));
                        textArea1.setForeground(Color.black);
                        scrollPane4.setViewportView(textArea1);
                    }
                    panelCheck.add(scrollPane4, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                contentPanel.add(panelCheck, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- label4 ----
                label4.setText("\u53d1\u4ef6\u90ae\u7bb1 ");
                label4.setHorizontalAlignment(SwingConstants.TRAILING);
                contentPanel.add(label4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //======== panel4 ========
                {
                    panel4.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {198, 76, 134, 160, 0};
                    ((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0};
                    ((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                    //---- txtMailSender ----
                    txtMailSender.setToolTipText("\u53d1\u4ef6\u90ae\u7bb1");
                    panel4.add(txtMailSender, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- label5 ----
                    label5.setText("\u767b\u5f55\u5bc6\u7801 ");
                    label5.setHorizontalAlignment(SwingConstants.TRAILING);
                    panel4.add(label5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- txtMailPassword ----
                    txtMailPassword.setToolTipText("\u90ae\u7bb1\u5bc6\u7801");
                    panel4.add(txtMailPassword, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- txtMailSenderName ----
                    txtMailSenderName.setToolTipText("\u53d1\u4ef6\u4eba\u540d\u79f0");
                    panel4.add(txtMailSenderName, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    //---- txtMailReceiver ----
                    txtMailReceiver.setToolTipText("\u53d1\u4ef6\u90ae\u7bb1");
                    panel4.add(txtMailReceiver, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- label8 ----
                    label8.setText("\u6284\u9001\u4eba ");
                    label8.setHorizontalAlignment(SwingConstants.TRAILING);
                    panel4.add(label8, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- txtMailCopy ----
                    txtMailCopy.setToolTipText("\u53d1\u4ef6\u90ae\u7bb1");
                    panel4.add(txtMailCopy, new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                contentPanel.add(panel4, new GridBagConstraints(1, 3, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("\u6536\u4ef6\u4eba ");
                label7.setHorizontalAlignment(SwingConstants.TRAILING);
                contentPanel.add(label7, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //======== panel5 ========
                {
                    panel5.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {225, 108, 115, 160, 0};
                    ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
                    ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
                }
                contentPanel.add(panel5, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- label3 ----
                label3.setText("Email\u6807\u9898 ");
                label3.setHorizontalAlignment(SwingConstants.TRAILING);
                contentPanel.add(label3, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //---- txtMailTitle ----
                txtMailTitle.setToolTipText("\u90ae\u4ef6\u6807\u9898");
                contentPanel.add(txtMailTitle, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- label2 ----
                label2.setText("Email\u5185\u5bb9\u6a21\u677f  ");
                label2.setHorizontalAlignment(SwingConstants.TRAILING);
                contentPanel.add(label2, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //======== scrollPane1 ========
                {

                    //---- txtMailBody ----
                    txtMailBody.setLineWrap(true);
                    scrollPane1.setViewportView(txtMailBody);
                }
                contentPanel.add(scrollPane1, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- label6 ----
                label6.setText("Email\u5185\u5bb9\u9884\u89c8  ");
                label6.setHorizontalAlignment(SwingConstants.TRAILING);
                contentPanel.add(label6, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                //======== scrollPane3 ========
                {

                    //---- editorMailBodyPreview ----
                    editorMailBodyPreview.setText("\u9884\u89c8");
                    scrollPane3.setViewportView(editorMailBodyPreview);
                }
                contentPanel.add(scrollPane3, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //======== panel2 ========
                {
                    panel2.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {58, 78, 139, 32, 151, 136, 38, 0};
                    ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {37, 0};
                    ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                    //---- btnSendMail ----
                    btnSendMail.setText(bundle.getString("BatchMailsSendingUI.btnSendMail.text"));
                    btnSendMail.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.red, Color.red, Color.red, Color.red));
                    btnSendMail.setOpaque(true);
                    btnSendMail.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            btnSendMailMouseClicked(e);
                        }
                    });
                    panel2.add(btnSendMail, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
                }
                contentPanel.add(panel2, new GridBagConstraints(0, 8, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);
        }
        contentPane.add(dialogPane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        setSize(775, 765);
        setLocationRelativeTo(null);

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(radioAll);
        buttonGroup1.add(radioNone);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label1;
    private JPanel panel3;
    private JTextField txtExcelFile;
    private JButton btnChoose;
    private JButton btnParse;
    private JScrollPane scrollPane2;
    private JTable tabExcel;
    private JPanel panelCheck;
    private JRadioButton radioAll;
    private JRadioButton radioNone;
    private JLabel lblSelectedRows;
    private JScrollPane scrollPane4;
    private JTextArea textArea1;
    private JLabel label4;
    private JPanel panel4;
    private JTextField txtMailSender;
    private JLabel label5;
    private JPasswordField txtMailPassword;
    private JTextField txtMailSenderName;
    private JTextField txtMailReceiver;
    private JLabel label8;
    private JTextField txtMailCopy;
    private JLabel label7;
    private JPanel panel5;
    private JLabel label3;
    private JTextField txtMailTitle;
    private JLabel label2;
    private JScrollPane scrollPane1;
    private JTextArea txtMailBody;
    private JLabel label6;
    private JScrollPane scrollPane3;
    private JEditorPane editorMailBodyPreview;
    private JPanel panel2;
    private JButton btnSendMail;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}

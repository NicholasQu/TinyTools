///*
// * Created by JFormDesigner on Mon Sep 11 18:01:34 CST 2017
// */
//
//package cc.xiaoquer.tinytools.ui.monthlyassessment;
//
//import java.awt.event.*;
//import cc.xiaoquer.tinytools.storage.PropertiesCache;
//import lombok.Data;
//import lombok.ToString;
//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.poifs.filesystem.POIFSFileSystem;
//import org.apache.poi.ss.usermodel.CellType;
//import org.apache.poi.ss.usermodel.FormulaEvaluator;
//
//import javax.swing.*;
//import javax.swing.border.*;
//import javax.swing.filechooser.FileFilter;
//import javax.swing.table.DefaultTableModel;
//import javax.swing.table.TableCellRenderer;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.*;
//import java.util.List;
//
///**
// * @author Nicholas.qu
// */
//public class MonthlyAssessment extends JFrame {
//    public MonthlyAssessment() {
//        initComponents();
//    }
//
//    private void thisWindowActivated(WindowEvent e) {
//
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                populateCache();
//
//                btnChoose.requestFocus();
//            }
//        });
//    }
//
//    private void populateCache() {
//        if (txtExcelFile.getText().length() == 0) {
//            txtExcelFile.setText(PropertiesCache.getValue("excelFile"));
//        }
//        if (txtTemplateMailBody.getText().length() == 0) {
//            txtTemplateMailBody.setText(PropertiesCache.getValue("templateMailBody"));
//        }
//        if (txtTemplateBonus.getText().length() == 0) {
//            txtTemplateBonus.setText(PropertiesCache.getValue("templateBonus"));
//        }
//        if (txtTemplateMemo.getText().length() == 0) {
//            txtTemplateMemo.setText(PropertiesCache.getValue("templateMemo"));
//        }
//
//        if (txtMailAddress.getText().length() == 0) {
//            txtMailAddress.setText(PropertiesCache.getValue("mailAddress"));
//        }
//        if (txtMailPassword.getPassword().length == 0) {
//            txtMailPassword.setText(PropertiesCache.getPassword(""));
//        }
//        if (txtMailTitle.getText().length() == 0) {
//            txtMailTitle.setText(PropertiesCache.getValue("mailTitle"));
//        }
//    }
//
//    private void saveCache() {
////        PropertiesCache.setValue("excelFile", txtExcelFile.getText());
////        PropertiesCache.setValue("templateMailBody", txtTemplateMailBody.getText());
////        PropertiesCache.setValue("templateBonus", txtTemplateBonus.getText());
////        PropertiesCache.setValue("templateMemo", txtTemplateMemo.getText());
////        PropertiesCache.setValue("mailAddress", txtMailAddress.getText());
////        PropertiesCache.setPassword("", String.valueOf(txtMailPassword.getPassword()));
////        PropertiesCache.setValue("mailTitle", txtMailTitle.getText());
//    }
//
//    private List<List> DATA_LIST = new ArrayList<>();
//    private Map<Integer, Assessment> ASSESS_MAP = new HashMap<>();
//    private List<String> COLUMN_NAMES = new ArrayList();
//
//    private final static int COL_EMPNAME        = 0; //注意这个Col对应的是Excel的列号，与Table里面的列号要+2
//    private final static int COL_TECHRANK       = 1;
//    private final static int COL_TOTALPOINTS    = 2;
//    private final static int COL_ASSESS_RESULT  = 3;
//    private final static int COL_BONUS          = 4;
//    private final static int COL_ASSESS_MEMO    = 5;
//    private final static int COL_RECEIVER       = 6;
//    private final static int COL_COPY           = 7;
//    private final static int COL_DETAILS        = 8;
//
//
//    private boolean parseExcel() {
//        String excelPath = txtExcelFile.getText();
//        File excelFile = new File(excelPath);
//        if (excelPath == null || excelPath.length() == 0 || !excelFile.exists()) {
//            JOptionPane.showMessageDialog
//                    (null, "请选择有效的考评Excel文件！", "警告", JOptionPane.ERROR_MESSAGE);
//            return false;
//        }
//
//        resetTable();
//
//        POIFSFileSystem fileSystem = null;
//        HSSFWorkbook workbook = null;
//        COLUMN_NAMES.add("勾选");//第一列是checkbox
//        COLUMN_NAMES.add("序号");//第二列是序号
//
//        try {
//            fileSystem = new POIFSFileSystem(new BufferedInputStream(new FileInputStream(excelFile)));
//            workbook = new HSSFWorkbook(fileSystem);
//            HSSFSheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
//
//            int    sn        = 0;
//            String v_monthly = sheet.getSheetName().trim();
//
//            System.out.println("正在解析【" + v_monthly + "】的考核表格...");
//
//            int lastRowIndex = sheet.getLastRowNum();
//            for (int i = 0; i <= lastRowIndex; i++) {
//                HSSFRow row = sheet.getRow(i);
//
//                if (row == null || row.getCell(0) == null
//                        || _getCellValue(row.getCell(0)).equalsIgnoreCase("")) {
//                    break;
//                }
//
//                short lastCellNum = row.getLastCellNum();
//                List rowList = null;
//                Assessment assessment = null;
//                if ( i > 0) {  //列头行i=0不需要添加data数据
//                    rowList = new ArrayList();
//                    rowList.add(true); //row.getRowNum());  //add checkbox with default checked.
//                    rowList.add(++sn);  //序号
//                    DATA_LIST.add(rowList);
//
//                    assessment = new Assessment();
//                    ASSESS_MAP.put(sn, assessment);
//                }
//
//                for (int j = 0; j < lastCellNum; j++) {
//                    HSSFCell cell = row.getCell(j);
//                    String cellValue = _getCellValue(cell);
//
//                    if (i > 0) {
//                        rowList.add(cellValue);
//
//                        assessment.setValue(j, cellValue);
//                    } else {
//                        COLUMN_NAMES.add(cellValue);
//                    }
//
//                }
//            }
//
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                workbook.close();
//            } catch (IOException e) {
//            }
//            try {
//                fileSystem.close();
//            } catch (IOException e) {
//            }
//        }
//
//        return false;
//    }
//
//    private void resetTable() {
//        DATA_LIST = new ArrayList<>();
//        COLUMN_NAMES = new ArrayList();
//
//        DefaultTableModel model = (DefaultTableModel) tabExcel.getModel();
//        model.getDataVector().clear();   //清除表格数据
//        model.setRowCount(0);
////        for (int i=0; i < model.getRowCount(); i++) {
////            model.removeRow(i);
////        }
//        model.fireTableDataChanged();//通知模型更新
//        tabExcel.updateUI();//刷新表格
//    }
//
//    private void renderTable() {
//        Object [ ][ ] data = new Object [DATA_LIST.size()][ ];    //动态创建第一维
//        for (int i = 0; i < DATA_LIST.size() ; i++ ) {
//            List row = DATA_LIST.get(i);
//            data [ i ] = new Object[row.size()];    //动态创建第二维
//            for( int j=0 ; j < row.size() ; j++) {
//                data [ i ][ j ] = row.get(j);
//            }
//        }
//
//        DefaultTableModel model = new DefaultTableModel(data, COLUMN_NAMES.toArray()) {
//            @Override
//            public Class<?> getColumnClass(int columnIndex) {
//                switch (columnIndex) {
//                    case 0:
//                        return Boolean.class;
//                    case 1:
//                        return Integer.class;
//                    default:
//                        return String.class;
//                }
//            }
//        };
//
//        tabExcel = new JTable() {
//
//            private Border outside = new MatteBorder(2, 0, 2, 0, Color.RED);
//            private Border inside = new EmptyBorder(0, 2, 0, 2);
//            private Border highlight = new CompoundBorder(outside, inside);
//
//            @Override
//            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
//
////                Color top       = Color.WHITE;
//                Color left      = column % 2 == 0 ? Color.ORANGE : Color.PINK;
////            Color bottom    = Color.WHITE;
////            Color right     = Color.WHITE;
//
//                Border border = BorderFactory.createCompoundBorder();
////            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(2,0,0,0,top));
//                border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,2,0,0,left));
////            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,0,2,0,bottom));
////            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,0,0,2,right));
//
//                JComponent comp = (JComponent)super.prepareRenderer(renderer, row, column);
//
//
//                comp.setBorder(border);
//
//                int modelRow = convertRowIndexToModel(row);
//                String rank = (String)getModel().getValueAt(modelRow, 5);
//
//                Color background = row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE;
//                Color fontColor = Color.BLUE;
//
//                if ("A".equalsIgnoreCase(rank)) {
//                    background = Color.GREEN;
//                } else if ("C".equalsIgnoreCase(rank)) {
//                    background = Color.DARK_GRAY;
//                    fontColor = Color.WHITE;
//                }
//
//                comp.setForeground(fontColor);
//                comp.setBackground(background);
//
//                if (isRowSelected(row)) {
//                    //使用背景色来展示选中
////                    comp.setForeground(Color.BLUE);
////                    comp.setBackground(Color.ORANGE);
//                    //使用边框来显示选中
//                    comp.setBorder(highlight);
//                }
//
//                return comp;
//            }
//        };
//
//        tabExcel.setModel(model);
//
//        tabExcel.setAutoCreateRowSorter(true);
//        tabExcel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        tabExcel.setBackground(Color.white);
//        tabExcel.setShowHorizontalLines(true);
//        tabExcel.setShowVerticalLines(true);
//        tabExcel.setOpaque(false);
//        tabExcel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
//        tabExcel.setSelectionBackground(new Color(102, 102, 102));
//
//        tabExcel.getColumnModel().getColumn(0).setPreferredWidth(50);
//        tabExcel.getColumnModel().getColumn(1).setPreferredWidth(50);
//        tabExcel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//
//        tabExcel.setIntercellSpacing(new Dimension(0,0));
//
////        tabExcel.setDefaultRenderer(Integer.class,
////                new ColorfulRender());        //TMD, Integer 不是Object?? 只设置Object只识别String的列！！！
////        tabExcel.setDefaultRenderer(Object.class,
////                new ColorfulRender());
//
//        scrollPane2.setViewportView(tabExcel);
//    }
//
//    private static String _getCellValue(HSSFCell cell) {
//        if (cell==null) {
//            return "";
//        }
////        System.out.println("rowIdx:"+cell.getRowIndex()+",colIdx:"+cell.getColumnIndex());
//        String cellValue = null;
//
//        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
//
//        CellType type = cell.getCellTypeEnum();
//
//        if (CellType.STRING == type) {
//            cellValue = cell.getStringCellValue().trim();
//        } else if (CellType.NUMERIC == type) {
////            cellValue = String.valueOf((int)cell.getNumericCellValue()).trim();
//            cellValue = String.valueOf(cell.getNumericCellValue()).trim();
//        } else if (CellType.BOOLEAN == type) {
//            cellValue = String.valueOf(cell.getBooleanCellValue()).trim();
//        } else if (CellType.FORMULA == type) {
//            cellValue = evaluator.evaluateInCell(cell).toString().trim();
//        } else {
//            cellValue = "";
//        }
//
//        return cellValue.replace(".0", "");
//    }
//
//    private void btnChooseMouseClicked(MouseEvent e) {
//        JFileChooser jfc = new JFileChooser();
//        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        jfc.setFileFilter(new FileFilter() {
//            @Override
//            public boolean accept(File f) {
//                if (f.getName().endsWith(".xls") || f.getName().endsWith(".xlsx")) {
//                    return true;
//                }
//                return false;
//            }
//
//            @Override
//            public String getDescription() {
//                return "Excel格式文件(.xls|.xlsx)";
//            }
//        });
//
//        jfc.showDialog(new JLabel(), "Open");
//        File file=jfc.getSelectedFile();
//        if (file == null) return;
//        txtExcelFile.setText(file.getAbsolutePath());
//    }
//
//    private void btnParseMouseClicked(MouseEvent e) {
//
//        btnParse.setEnabled(false);
//
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                boolean parsed = parseExcel();
//                if (parsed) renderTable();
//
//                try {
//                    Thread.sleep(3L);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
//
//                btnParse.setEnabled(true);
//
//                saveCache();
//            }
//        });
//    }
//
//    private void radioAllMouseClicked(MouseEvent e) {
//        checkTable(true);
//    }
//
//    private void radioNoneMouseClicked(MouseEvent e) {
//        checkTable(false);
//    }
//
//    private void checkTable(boolean checked) {
//        DefaultTableModel model = (DefaultTableModel)tabExcel.getModel();
//
//        for (int i = 0; i < model.getRowCount(); i++) {
//            model.setValueAt(checked, i, 0);
//        }
//    }
//
//    private void btnSendMailMouseClicked(MouseEvent e) {
//        saveCache();
//
//        DefaultTableModel model = (DefaultTableModel)tabExcel.getModel();
//
//        for (int i = 0; i < model.getRowCount(); i++) {
//            boolean checked = (boolean)model.getValueAt(i, 0);
//            if (checked) {
//                //根据序号获取Assessments对象
//                Assessment assessment = ASSESS_MAP.get((int)model.getValueAt(i, 1));
//
//                System.out.println(assessment);
//            }
//        }
//    }
//
//    @Data
//    @ToString
//    private class Assessment {
//        private String employeeName;
//        private String techRank;
//        private String totalPoints;
//        private String assessResult;
//        private String bonus;
//        private String assessMemo;
//        private String receiver;
//        private String copy;
//        private Map<String, String> details = new LinkedHashMap<String, String>();
//
//        public void putDetails(String key, String value) {
//            this.details.put(key, value);
//        }
//
//        public void setValue(int col, String value) {
//            switch (col) {
//                case COL_EMPNAME:
//                    this.employeeName = value;
//                    break;
//                case COL_TECHRANK:
//                    this.techRank = value;
//                    break;
//                case COL_TOTALPOINTS:
//                    this.totalPoints = value;
//                    break;
//                case COL_ASSESS_RESULT:
//                    this.assessResult = value;
//                    break;
//                case COL_BONUS:
//                    this.bonus = value;
//                    break;
//                case COL_ASSESS_MEMO:
//                    this.assessMemo = value;
//                    break;
//                case COL_RECEIVER:
//                    this.receiver = value;
//                    break;
//                case COL_COPY:
//                    this.copy = value;
//                    break;
//                default:
//                    putDetails(COLUMN_NAMES.get(col + 2), value);
//            }
//
//        }
//    }
//
//    /**
//     * 该方法可以实现自定义的颜色边框和背景色设置
//     */
////    private class ColorfulRender extends DefaultTableCellRenderer {
////        public Component getTableCellRendererComponent(JTable table,
////                                                       Object value, boolean isSelected, boolean hasFocus,
////                                                       int row, int column) {
//////            Color top       = Color.WHITE;
////            Color left      = column % 2 == 0 ? Color.ORANGE : Color.PINK;
//////            Color bottom    = Color.WHITE;
//////            Color right     = Color.WHITE;
////
////            Border border = BorderFactory.createCompoundBorder();
//////            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(2,0,0,0,top));
////            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,2,0,0,left));
//////            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,0,2,0,bottom));
//////            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,0,0,2,right));
////
////            JComponent comp = (JComponent)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
////                            row, column);
////            comp.setBorder(border);
////            comp.setBackground(row % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY);
////            comp.setForeground(Color.blue);
////            return comp;
////        }
////    }
//
//    private void initComponents() {
//        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//        ResourceBundle bundle = ResourceBundle.getBundle("resources");
//        dialogPane = new JPanel();
//        contentPanel = new JPanel();
//        label1 = new JLabel();
//        txtExcelFile = new JTextField();
//        btnChoose = new JButton();
//        label2 = new JLabel();
//        scrollPane1 = new JScrollPane();
//        txtTemplateMailBody = new JTextArea();
//        label4 = new JLabel();
//        txtTemplateBonus = new JTextArea();
//        label5 = new JLabel();
//        txtTemplateMemo = new JTextArea();
//        panel1 = new JPanel();
//        btnParse = new JButton();
//        scrollPane2 = new JScrollPane();
//        tabExcel = new JTable();
//        panel2 = new JPanel();
//        radioAll = new JRadioButton();
//        radioNone = new JRadioButton();
//        txtMailAddress = new JTextField();
//        txtMailPassword = new JPasswordField();
//        txtMailTitle = new JTextField();
//        btnSendMail = new JButton();
//
//        //======== this ========
//        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowActivated(WindowEvent e) {
//                thisWindowActivated(e);
//            }
//        });
//        Container contentPane = getContentPane();
//        contentPane.setLayout(new BorderLayout());
//
//        //======== dialogPane ========
//        {
//            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
//            dialogPane.setLayout(new GridBagLayout());
//            ((GridBagLayout)dialogPane.getLayout()).columnWidths = new int[] {0, 0};
//            ((GridBagLayout)dialogPane.getLayout()).rowHeights = new int[] {0, 0};
//            ((GridBagLayout)dialogPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
//            ((GridBagLayout)dialogPane.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
//
//            //======== contentPanel ========
//            {
//                contentPanel.setLayout(new GridBagLayout());
//                ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {122, 616, 64, 0};
//                ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 96, 0, 0, 0, 281, 32, 0};
//                ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
//                ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
//
//                //---- label1 ----
//                label1.setText("\u8003\u6838Excel ");
//                label1.setHorizontalAlignment(SwingConstants.TRAILING);
//                contentPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 5), 0, 0));
//                contentPanel.add(txtExcelFile, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 5), 0, 0));
//
//                //---- btnChoose ----
//                btnChoose.setText("\u9009\u62e9\u6587\u4ef6");
//                btnChoose.setBorder(new LineBorder(Color.red, 2, true));
//                btnChoose.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mouseClicked(MouseEvent e) {
//                        btnChooseMouseClicked(e);
//                    }
//                });
//                contentPanel.add(btnChoose, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 0), 0, 0));
//
//                //---- label2 ----
//                label2.setText("\u90ae\u4ef6\u4e3b\u4f53\u6a21\u677f ");
//                label2.setHorizontalAlignment(SwingConstants.TRAILING);
//                contentPanel.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 5), 0, 0));
//
//                //======== scrollPane1 ========
//                {
//
//                    //---- txtTemplateMailBody ----
//                    txtTemplateMailBody.setText(bundle.getString("MonthlyAssessment.txtTemplateMailBody.text"));
//                    txtTemplateMailBody.setLineWrap(true);
//                    scrollPane1.setViewportView(txtTemplateMailBody);
//                }
//                contentPanel.add(scrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 5), 0, 0));
//
//                //---- label4 ----
//                label4.setText(bundle.getString("MonthlyAssessment.label4.text"));
//                label4.setHorizontalAlignment(SwingConstants.TRAILING);
//                contentPanel.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 5), 0, 0));
//
//                //---- txtTemplateBonus ----
//                txtTemplateBonus.setText(bundle.getString("MonthlyAssessment.txtTemplateBonus.text"));
//                txtTemplateBonus.setLineWrap(true);
//                contentPanel.add(txtTemplateBonus, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 5), 0, 0));
//
//                //---- label5 ----
//                label5.setText(bundle.getString("MonthlyAssessment.label5.text"));
//                label5.setHorizontalAlignment(SwingConstants.TRAILING);
//                contentPanel.add(label5, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 5), 0, 0));
//
//                //---- txtTemplateMemo ----
//                txtTemplateMemo.setText(bundle.getString("MonthlyAssessment.txtTemplateMemo.text"));
//                txtTemplateMemo.setLineWrap(true);
//                contentPanel.add(txtTemplateMemo, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 5), 0, 0));
//
//                //======== panel1 ========
//                {
//                    panel1.setLayout(new GridBagLayout());
//                    ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {194, 201, 0, 0};
//                    ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
//                    ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
//                    ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
//
//                    //---- btnParse ----
//                    btnParse.setText(bundle.getString("MonthlyAssessment.btnParse.text"));
//                    btnParse.setMinimumSize(new Dimension(50, 27));
//                    btnParse.setPreferredSize(new Dimension(50, 27));
//                    btnParse.setBorder(new BevelBorder(BevelBorder.RAISED, Color.red, Color.red, Color.red, Color.red));
//                    btnParse.setOpaque(true);
//                    btnParse.setMultiClickThreshhold(1L);
//                    btnParse.addMouseListener(new MouseAdapter() {
//                        @Override
//                        public void mouseClicked(MouseEvent e) {
//                            btnParseMouseClicked(e);
//                        }
//                    });
//                    panel1.add(btnParse, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 0, 5), 0, 0));
//                }
//                contentPanel.add(panel1, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 5), 0, 0));
//
//                //======== scrollPane2 ========
//                {
//
//                    //---- tabExcel ----
//                    tabExcel.setAutoCreateRowSorter(true);
//                    tabExcel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//                    tabExcel.setBackground(Color.white);
//                    tabExcel.setShowHorizontalLines(true);
//                    tabExcel.setShowVerticalLines(true);
//                    tabExcel.setOpaque(false);
//                    tabExcel.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
//                    tabExcel.setSelectionBackground(new Color(102, 102, 102));
//                    scrollPane2.setViewportView(tabExcel);
//                }
//                contentPanel.add(scrollPane2, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 5, 0), 0, 0));
//
//                //======== panel2 ========
//                {
//                    panel2.setLayout(new GridBagLayout());
//                    ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {58, 78, 144, 12, 124, 136, 0, 0, 0, 0};
//                    ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {31, 0};
//                    ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
//                    ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
//
//                    //---- radioAll ----
//                    radioAll.setText(bundle.getString("MonthlyAssessment.radioAll.text"));
//                    radioAll.setSelected(true);
//                    radioAll.addMouseListener(new MouseAdapter() {
//                        @Override
//                        public void mouseClicked(MouseEvent e) {
//                            radioAllMouseClicked(e);
//                        }
//                    });
//                    panel2.add(radioAll, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- radioNone ----
//                    radioNone.setText(bundle.getString("MonthlyAssessment.radioNone.text"));
//                    radioNone.addMouseListener(new MouseAdapter() {
//                        @Override
//                        public void mouseClicked(MouseEvent e) {
//                            radioNoneMouseClicked(e);
//                        }
//                    });
//                    panel2.add(radioNone, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- txtMailAddress ----
//                    txtMailAddress.setText(bundle.getString("MonthlyAssessment.txtMailAddress.text"));
//                    txtMailAddress.setToolTipText("\u53d1\u4ef6\u90ae\u7bb1");
//                    panel2.add(txtMailAddress, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- txtMailPassword ----
//                    txtMailPassword.setText(bundle.getString("MonthlyAssessment.txtMailPassword.text"));
//                    txtMailPassword.setToolTipText("\u90ae\u7bb1\u5bc6\u7801");
//                    panel2.add(txtMailPassword, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- txtMailTitle ----
//                    txtMailTitle.setText(bundle.getString("MonthlyAssessment.txtMailTitle.text"));
//                    txtMailTitle.setToolTipText("\u90ae\u4ef6\u6807\u9898");
//                    panel2.add(txtMailTitle, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- btnSendMail ----
//                    btnSendMail.setText(bundle.getString("MonthlyAssessment.btnSendMail.text"));
//                    btnSendMail.addMouseListener(new MouseAdapter() {
//                        @Override
//                        public void mouseClicked(MouseEvent e) {
//                            btnSendMailMouseClicked(e);
//                        }
//                    });
//                    panel2.add(btnSendMail, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 0, 0), 0, 0));
//                }
//                contentPanel.add(panel2, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 0, 0), 0, 0));
//            }
//            dialogPane.add(contentPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
//                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                new Insets(0, 0, 0, 0), 0, 0));
//        }
//        contentPane.add(dialogPane, BorderLayout.CENTER);
//        setSize(890, 610);
//        setLocationRelativeTo(null);
//
//        //---- buttonGroup1 ----
//        ButtonGroup buttonGroup1 = new ButtonGroup();
//        buttonGroup1.add(radioAll);
//        buttonGroup1.add(radioNone);
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//    }
//
//    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
//    private JPanel dialogPane;
//    private JPanel contentPanel;
//    private JLabel label1;
//    private JTextField txtExcelFile;
//    private JButton btnChoose;
//    private JLabel label2;
//    private JScrollPane scrollPane1;
//    private JTextArea txtTemplateMailBody;
//    private JLabel label4;
//    private JTextArea txtTemplateBonus;
//    private JLabel label5;
//    private JTextArea txtTemplateMemo;
//    private JPanel panel1;
//    private JButton btnParse;
//    private JScrollPane scrollPane2;
//    private JTable tabExcel;
//    private JPanel panel2;
//    private JRadioButton radioAll;
//    private JRadioButton radioNone;
//    private JTextField txtMailAddress;
//    private JPasswordField txtMailPassword;
//    private JTextField txtMailTitle;
//    private JButton btnSendMail;
//    // JFormDesigner - End of variables declaration  //GEN-END:variables
//}

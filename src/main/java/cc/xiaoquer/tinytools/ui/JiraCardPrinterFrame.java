///*
// * Created by JFormDesigner on Sat Sep 02 21:04:27 CST 2017
// */
//
//package cc.xiaoquer.tinytools.ui;
//
//import cc.xiaoquer.tinytools.html.HtmlGenerator;
//import cc.xiaoquer.tinytools.storage.PropertiesCache;
//
//import javax.swing.*;
//import javax.swing.border.LineBorder;
//import javax.swing.border.TitledBorder;
//import javax.swing.event.TreeModelEvent;
//import javax.swing.event.TreeModelListener;
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.DefaultTreeModel;
//import javax.swing.tree.TreeNode;
//import javax.swing.tree.TreePath;
//import java.awt.*;
//import java.awt.datatransfer.Clipboard;
//import java.awt.datatransfer.StringSelection;
//import java.awt.datatransfer.Transferable;
//import java.awt.event.*;
//import java.io.File;
//import java.io.UnsupportedEncodingException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.List;
//
///**
// * @author unknown
// */
//public class JiraCardPrinterFrame {
//    public JiraCardPrinterFrame() {
//        initComponents();
//    }
//
//    private final static String SPLIT                   = "___";
//    private final static int    PADDING_LEN             = 100;
//
//    private final static String PADDING_SPACE;  //空白填充
//    static {
//        char[] b = new char[PADDING_LEN];
//        Arrays.fill(b, ' ');
//        PADDING_SPACE = new String(b);
//    }
//
//    private final static String BOARD_FILTER_TIPS       = "模糊查询回车过滤";
//    private final static String SERV_STATUS_CONNECTING  = "连接中...";
//    private final static String STATUS_BOARD_COUNT      = "Boards:[{}]个";
//    private final static String STATUS_SPRINT_COUNT     = "Sprints:[{}]个";
//    private final static String STATUS_ISSUE_COUNT      = "选中Issues:[{}]个";
//
//    private DefaultListModel boardsModel = new DefaultListModel();
//    private JList jListBoards = new JList(boardsModel);
//
//    private DefaultListModel sprintsModel = new DefaultListModel();
//    private JList jlistSprints = new JList(sprintsModel);
//
//    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("Kanban");
//    private DefaultTreeModel treeModel = new DefaultTreeModel(root);
//    private JTree tree = new JTree(treeModel);
//
//    /**
//     * 启动设置password焦点
//     * @param e
//     */
//    private void jiraFrameWindowActivated(WindowEvent e) {
//
//        if (txtJiraUrl.getText().length() == 0) {
//            txtJiraUrl.setText(PropertiesCache.getHost());
//            txtJiraUrl.requestFocus();
//        }
//        if (txtUser.getText().length() == 0) {
//            txtUser.setText(PropertiesCache.getUserName());
//            txtUser.requestFocus();
//        }
//        if (txtPassword.getPassword().length == 0) {
//            txtPassword.setText(PropertiesCache.getPassword());
//            txtPassword.requestFocus();
//        }
//
//        if (!checkConnectInputsValid() || JIRA.connectStatus.length() > 0) return;
//
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                connect();
//            }
//        });
//    }
//
//    private void btnConnectMouseClicked(MouseEvent e) {
//
//    }
//
//    private boolean checkConnectInputsValid() {
//        String url = txtJiraUrl.getText();
//        String user = txtUser.getText();
//        String password = String.valueOf(txtPassword.getPassword());
//
//        if (url == null || url.trim().length() == 0
//                || !(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))
//                || user == null || user.trim().length() ==0
//                || password == null || password.trim().length() == 0) {
//            return false;
//        }
//
//        return true;
//    }
//
//    private void btnConnectMousePressed(MouseEvent e) {
//        if (!checkConnectInputsValid()) {
//            JOptionPane.showMessageDialog
//                    (null, "请输入有效的Jira地址和登录账号！", "警告", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        lblStatus.setText(SERV_STATUS_CONNECTING);
//        statusPanel.setBackground(Color.ORANGE);
//        lblStatus.updateUI();
//    }
//
//
//    private void btnConnectMouseReleased(MouseEvent e) {
//        connect();
//    }
//
//    private void txtPasswordKeyPressed(KeyEvent e) {
//        if (e.getKeyChar() == e.VK_ENTER) {
//            connect();
//        }
//    }
//
//    private void connect() {
//        String url = txtJiraUrl.getText();
//        String user = txtUser.getText();
//        String password = String.valueOf(txtPassword.getPassword());
//        boolean isConnected = JIRA.connect(url, user, password);
//
//        lblStatus.setText(JIRA.connectStatus);
//
//        if (isConnected) {
//            PropertiesCache.setHost(url);
//            PropertiesCache.setUserName(user);
//            PropertiesCache.setPassword(password);
//            PropertiesCache.flush();
//
//
//            boardPanel.setEnabled(true);
//
//            txtFilterBoard.setText(PropertiesCache.getBoardFilter());
//            txtFilterBoard.setEnabled(true);
//            txtFilterBoard.setBackground(Color.WHITE);
//            txtFilterBoard.requestFocus();
//
//            statusPanel.setBackground(new Color(136, 202, 121));
//
//            renderBoards(PropertiesCache.getBoardFilter());
//        } else {
//            JOptionPane.showMessageDialog
//                    (null, JIRA.connectStatus, "警告", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//
//    }
//    private void btnLogoutMouseClicked(MouseEvent e) {
//        //清理子数据集
//        boardsModel.clear();
//        sprintsModel.clear();
//
//        root.removeAllChildren();
//        treeModel.reload();
//        tree.setModel(null);
//
//        txtFilterBoard.setEnabled(false);
//        txtFilterBoard.setBackground(new Color(214, 217, 223));
//        boardPanel.setEnabled(false);
//        JIRA.disconnect();
//    }
//
//    private void jiraFrameWindowClosing(WindowEvent e) {
//        JIRA.disconnect();
//    }
//
////    private void txtFilterBoardKeyReleased(KeyEvent e) {
////        if (e.getKeyChar() == e.VK_ENTER) {
////            renderBoards(txtFilterBoard.getText().trim());
////        }
////
////        String prefix = txtFilterBoard.getText();
////        if (prefix.length() == 0) {
////            txtFilterBoard.setText(BOARD_FILTER_TIPS);
////            txtFilterBoard.setForeground(Color.LIGHT_GRAY);
////        }
////    }
//
//    private void txtFilterBoardFocusGained(FocusEvent e) {
//        if (BOARD_FILTER_TIPS.equals(txtFilterBoard.getText())) {
//            txtFilterBoard.setText("");
//            txtFilterBoard.setForeground(Color.BLACK);
//        }
//    }
//
//    private void txtFilterBoardKeyReleased(KeyEvent e) {
//        if (e.getKeyChar() == e.VK_ENTER) {
//            filterBoard();
//        }
//    }
//
//    private void txtFilterBoardFocusLost(FocusEvent e) {
//        String prefix = txtFilterBoard.getText();
//        if (prefix.length() == 0) {
//            txtFilterBoard.setText(BOARD_FILTER_TIPS);
//            txtFilterBoard.setForeground(Color.LIGHT_GRAY);
//        }
//
//        filterBoard();
//
//    }
//
//    private void filterBoard() {
//        String prefix = txtFilterBoard.getText();
//
//        if (BOARD_FILTER_TIPS.equalsIgnoreCase(prefix)) {
//            prefix = "";
//        }
//
//        PropertiesCache.setBoardFilter(prefix);
//        PropertiesCache.flush();
//
//        renderBoards(prefix.trim());
//    }
//
//    private void asyncQuerySprints() {
//        lblSelectedBoard.setText("查询中......");
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                renderSprints(getIdByDisplayName((String)jListBoards.getSelectedValue()));
//            }
//        });
//    }
//
//    private void asyncQueryTree() {
//        lblSelectedBoardSprint.setText("查询中......");
//        final String s1 = (String)jListBoards.getSelectedValue();
//        final String s2 = (String)jlistSprints.getSelectedValue();
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                renderIssueTree(s1, s2);
//            }
//        });
//    }
//
//    public void show() {
//        jiraFrame.setVisible(true);
//    }
//
//    private static String getByteStr(String str, int start, int end) {
//        byte[] b = new byte[0];
//        try {
//            b = str.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            System.out.println("getByteStr error:" + str);
//            e.printStackTrace();
//        }
//
//        return new String(b, start, end);
//    }
//
//    private String combineDisplayName(String id, String name) {
//        if (name.length() >= PADDING_LEN) {
//            return name + SPLIT + id;
//        } else {
//            return getByteStr(name + PADDING_SPACE, 0, PADDING_LEN) + SPLIT + id;
////            return (name + PADDING_SPACE).substring(0, PADDING_LEN) + SPLIT + id;
//        }
//    }
//
//    private String getIdByDisplayName(String displayName) {
//        if (displayName == null) return "";
//        return displayName.split(SPLIT)[1];
//    }
//
//    private String getNameByDisplayName(String displayName) {
//        if (displayName == null) return "";
//        return displayName.split(SPLIT)[0].trim();
//    }
//
//    private String getHHMMSS() {
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
//
//        return sdf.format(new Date());
//    }
//
//    private void renderBoards(String prefix) {
//        resetResultSetAndText();
//
//        Map<String, JiraBoard> map = JIRA.getBoardMap();
//
//        List<JiraBoard> sorted = new ArrayList<>();
//        sorted.addAll(map.values());
//        Collections.sort(sorted, new Comparator<JiraBoard>() {
//            @Override
//            public int compare(JiraBoard o1, JiraBoard o2) {
//                return o1.getBoardName().compareTo(o2.getBoardName());
//            }
//        });
//
//        for (JiraBoard jiraBoard : sorted) {
//
//            if (prefix != null && prefix.length() > 0 && !jiraBoard.getBoardName().toLowerCase().contains(prefix.toLowerCase())) {
//                continue;
//            }
//
//            boardsModel.addElement(combineDisplayName(jiraBoard.getBoardId(), jiraBoard.getBoardName()));
//        }
//
////        for (int i=0;i<80;i++) {
////            boardsModel.addElement(i + "PMO-团队-大数据ETL_________" + i);
////        }
//
//        scrollPaneBoard.setViewportView(jListBoards);
//        scrollPaneBoard.updateUI();
//
//        lblBoardCount.setText(STATUS_BOARD_COUNT.replace("{}", boardsModel.size() + ""));
//        lblQueryStatus.setText(getHHMMSS() + "查询Boards成功");
//    }
//
//    private void resetResultSetAndText() {
//        //清理子数据集
//        boardsModel.clear();
//        sprintsModel.clear();
//
//        root.removeAllChildren();
//        treeModel.reload();
//        tree.setModel(null);
//
//        lblBoardCount.setText(STATUS_BOARD_COUNT);
//        lblQueryStatus.setText("");
//
//        lblSelectedBoard.setText("");
//        lblSelectedBoardSprint.setText("");
//    }
//
//    private void renderSprints(String boardId) {
//        if (boardId == null || boardId.length() == 0){
//            return;
//        }
//
//        //清理子数据集
//        sprintsModel.clear();
//
//        root.removeAllChildren();
//        treeModel.reload();
//        tree.setModel(null);
//
//        Map<String, JiraSprint> map = JIRA.getSprintMap(boardId);
//        for (JiraSprint jiraSprint : map.values()) {
//
////            if (prefix != null && prefix.length() > 0 && !jiraBoard.getBoardName().startsWith(prefix)) {
////                continue;
////            }
//
//            sprintsModel.addElement(combineDisplayName(jiraSprint.getSprintId(), jiraSprint.getSprintName()));
//        }
//
//        scrollPaneSprint.setViewportView(jlistSprints);
//        scrollPaneSprint.updateUI();
//
//        lblSelectedBoard.setText("看板:" + getNameByDisplayName((String)jListBoards.getSelectedValue()));
//        lblSelectedBoardSprint.setText("");
//
//        lblSprintCount.setText(STATUS_SPRINT_COUNT.replace("{}", sprintsModel.size() + ""));
//        lblQueryStatus.setText(getHHMMSS() + "查询Sprints成功");
//
//    }
//
//    private void renderIssueTree(String boardDisplayName, String sprintDisplayName) {
//        String boardId      = getIdByDisplayName(boardDisplayName);
//        String sprintId     = getIdByDisplayName(sprintDisplayName);
//
//        if (boardId == null || boardId.length() == 0 || sprintId == null || sprintId.length() == 0){
//            return;
//        }
//
//        root.removeAllChildren();
//        treeModel.reload();
//
//        tree.setModel(null);
//
//        int issuesCnt = 0;
//
//        Map<String, JiraIssue> issueMap = JIRA.getIssueMap(boardId, sprintId);
//
//        for (JiraIssue issue : issueMap.values()) {
//            final DefaultMutableTreeNode storyOrTask = add(root, issue, true);
//
//            issuesCnt++;
//            Map<String, JiraIssue> subTaskMap = issue.getSubTaskMap();
//            if (subTaskMap != null && subTaskMap.size() > 0) {
//                for (JiraIssue subTask : subTaskMap.values()) {
//                    add(storyOrTask, subTask, true);
//                    issuesCnt++;
//                }
//            }
//            root.add(storyOrTask);
//        }
//
//        tree = new JTree(treeModel);
//        final CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
//        tree.setCellRenderer(renderer);
//
//        final CheckBoxNodeEditor editor = new CheckBoxNodeEditor(tree);
//        tree.setCellEditor(editor);
//        tree.setEditable(true);
//
//        // listen for changes in the selection
////        tree.addTreeSelectionListener(new TreeSelectionListener() {
////
////            @Override
////            public void valueChanged(final TreeSelectionEvent e) {
////                lblIssueCount.setText(STATUS_ISSUE_COUNT.replace("{}", countCheckbox() + ""));
////            }
////        });
//
//        // listen for changes in the model (including check box toggles)
//        treeModel.addTreeModelListener(new TreeModelListener() {
//
//            @Override
//            public void treeNodesChanged(final TreeModelEvent e) {
//                lblIssueCount.setText(STATUS_ISSUE_COUNT.replace("{}", countCheckbox() + ""));
//            }
//
//            @Override
//            public void treeNodesInserted(final TreeModelEvent e) {
////                System.out.println(System.currentTimeMillis() + ": nodes inserted");
//            }
//
//            @Override
//            public void treeNodesRemoved(final TreeModelEvent e) {
////                System.out.println(System.currentTimeMillis() + ": nodes removed");
//            }
//
//            @Override
//            public void treeStructureChanged(final TreeModelEvent e) {
//                lblIssueCount.setText(STATUS_ISSUE_COUNT.replace("{}", countCheckbox() + ""));
//            }
//        });
//
//
//        scrollPaneCard.setViewportView(tree);
//        scrollPaneCard.updateUI();
//        expandTree(tree);
//
//        String issueCountStr = STATUS_ISSUE_COUNT.replace("{}", issuesCnt + "");
//        lblIssueCount.setText(issueCountStr);
//        lblQueryStatus.setText(getHHMMSS() + "查询Issues成功");
//
//        lblSelectedBoardSprint.setText(
//                "看板:" + getNameByDisplayName((String)jListBoards.getSelectedValue())
//                        + " => 迭代:"
//                        + getNameByDisplayName((String)jlistSprints.getSelectedValue()));
//
//        if (root.getChildCount() > 0) {
//            btnGenerate.setEnabled(true);
//        } else {
//            btnGenerate.setEnabled(false);
//        }
//
//    }
//
//    private DefaultMutableTreeNode add(
//            final DefaultMutableTreeNode parent, JiraIssue jiraIssue,
//            final boolean checked) {
//        final CheckBoxNodeData data = new CheckBoxNodeData(checked,
//                JIRA.toIssueDisplayName(jiraIssue));
//
//        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
//        parent.add(node);
//        return node;
//    }
//
//    /**
//     * 展开一棵树
//     * @param tree
//     */
//    private void expandTree(JTree tree) {
//        // 根节点
//
//        TreeNode node = (TreeNode) tree.getModel().getRoot();
//        expandAll(tree, new TreePath(node), true);
//    }
//
//    /**
//     * 完全展开一棵树或关闭一棵树
//     * @param tree JTree
//     * @param parent 父节点
//     * @param expand true 表示展开，false 表示关闭
//     */
//    private void expandAll(JTree tree, TreePath parent, boolean expand) {
//        TreeNode node = (TreeNode) parent.getLastPathComponent();
//
//        if (node.getChildCount() > 0) {
//            for (Enumeration e = node.children(); e.hasMoreElements();) {
//                TreeNode n = (TreeNode) e.nextElement();
//                TreePath path = parent.pathByAddingChild(n);
//                expandAll(tree, path, expand);
//            }
//        }
//        if (expand) {
//            tree.expandPath(parent);
//        } else {
//            tree.collapsePath(parent);
//        }
//    }
//
//    private void optAllMouseClicked(MouseEvent e) {
//        TreeNode node = (TreeNode)tree.getModel().getRoot();
//        //递归
//        doCheckbox(node, true);
//        treeModel.reload(node);
//        tree.updateUI();
//        expandTree(tree);
//    }
//
//    private void optCancelMouseClicked(MouseEvent e) {
//        TreeNode node = (TreeNode)tree.getModel().getRoot();
//        //递归
//        doCheckbox(node, false);
//        treeModel.reload(node);
//        tree.updateUI();
//        expandTree(tree);
//    }
//
//    private void doCheckbox(TreeNode node, boolean check) {
//        if (node.getChildCount() > 0) {
//            for (Enumeration child = node.children(); child.hasMoreElements();) {
//                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child.nextElement();
//
//                CheckBoxNodeData data = (CheckBoxNodeData)childNode.getUserObject();
//
//                data.setChecked(check);
//
//                doCheckbox(childNode, check);
//            }
//        }
//    }
//
//    private int countCheckbox() {
//        int cnt = 0;
//
//        if (tree == null || tree.getModel() == null || tree.getModel().getRoot() == null) {
//            return 0;
//        }
//
//        TreeNode rootNode = (TreeNode)tree.getModel().getRoot();
//        if (rootNode.getChildCount() > 0) {
//            for (Enumeration child = rootNode.children(); child.hasMoreElements();) {
//                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child.nextElement();
//
//                CheckBoxNodeData data = (CheckBoxNodeData)childNode.getUserObject();
//                if (data.isChecked()) {
//                    cnt++;
//                }
//
//                for (int i = 0; i < childNode.getChildCount(); i++) {
//                    DefaultMutableTreeNode grandsonNode = (DefaultMutableTreeNode)childNode.getChildAt(i);
//                    CheckBoxNodeData grandson = (CheckBoxNodeData)grandsonNode.getUserObject();
//
//                    if (grandson.isChecked()) {
//                        cnt++;
//                    }
//                }
//            }
//        }
//        return cnt;
//    }
//    private void btnGenerateMouseClicked(MouseEvent e) {
//        printHTML();
//    }
//
//    private void printHTML() {
//        Set<JiraIssue> issueSet = new LinkedHashSet<>();
//
//        TreeNode rootNode = (TreeNode)tree.getModel().getRoot();
//        if (rootNode.getChildCount() > 0) {
//            for (Enumeration child = rootNode.children(); child.hasMoreElements();) {
//                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child.nextElement();
//
//                CheckBoxNodeData data = (CheckBoxNodeData)childNode.getUserObject();
//                if (data.isChecked()) {
//                    issueSet.add(JIRA.getObjectFromDisplayName(data.getItem()));
//                }
//
//                for (int i = 0; i < childNode.getChildCount(); i++) {
//                    DefaultMutableTreeNode grandsonNode = (DefaultMutableTreeNode)childNode.getChildAt(i);
//                    CheckBoxNodeData grandson = (CheckBoxNodeData)grandsonNode.getUserObject();
//
//                    if (grandson.isChecked()) {
//                        issueSet.add(JIRA.getObjectFromDisplayName(grandson.getItem()));
//                    }
//                }
//            }
//        }
//
//        String sprintName = getNameByDisplayName((String)jlistSprints.getSelectedValue());
//
//        if (issueSet.size() == 0 || sprintName == "") {
//            JOptionPane.showMessageDialog(null, "请选中至少一个可打印的卡片!", "提示", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        String htmlPath = HtmlGenerator.generate(sprintName, issueSet);
//
//        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
//        Transferable tText = new StringSelection(htmlPath);
//        clip.setContents(tText, null);
//
//        int ret = JOptionPane.showOptionDialog
//                (null, "HTML格式的Jira卡片生成成功, 路径已复制到粘贴板！\n\n【路径:" + htmlPath + "】\n\n是否直接打开？", "提示",
//                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
//
//        if (JOptionPane.OK_OPTION == ret) {
//            try {
////                Runtime.getRuntime().exec(new String[] { "open", htmlPath });
//                Desktop.getDesktop().open(new File(htmlPath));
////                ProcessBuilder pb = new ProcessBuilder("open", htmlPath);
////                Process p = pb.start();
////                int exitCode = p.waitFor();
//            } catch (Exception e) {
//                System.out.println("Opening html file throws exception");
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void initComponents() {
//        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//        jiraFrame = new JFrame();
//        dialogPane = new JPanel();
//        serverPanel = new JPanel();
//        label1 = new JLabel();
//        txtJiraUrl = new JTextField();
//        label2 = new JLabel();
//        txtUser = new JTextField();
//        label3 = new JLabel();
//        txtPassword = new JPasswordField();
//        btnConnect = new JButton();
//        btnLogout = new JButton();
//        statusPanel = new JPanel();
//        jiraconnection = new JLabel();
//        lblStatus = new JLabel();
//        lblStatus2 = new JLabel();
//        lblQueryStatus = new JLabel();
//        lblBoardCount = new JLabel();
//        lblSprintCount = new JLabel();
//        lblIssueCount = new JLabel();
//        boardPanel = new JPanel();
//        label4 = new JLabel();
//        label5 = new JLabel();
//        label6 = new JLabel();
//        scrollPane1 = new JScrollPane();
//        txtFilterBoard = new JTextField();
//        scrollPane2 = new JScrollPane();
//        lblSelectedBoard = new JLabel();
//        scrollPane3 = new JScrollPane();
//        lblSelectedBoardSprint = new JLabel();
//        scrollPaneBoard = new JScrollPane();
//        scrollPaneSprint = new JScrollPane();
//        scrollPaneCard = new JScrollPane();
//        panel1 = new JPanel();
//        optAll = new JRadioButton();
//        optCancel = new JRadioButton();
//        btnGenerate = new JButton();
//
//        //======== jiraFrame ========
//        {
//            jiraFrame.setForeground(SystemColor.textHighlight);
//            jiraFrame.setBackground(new Color(204, 204, 255));
//            jiraFrame.setTitle("Jira\u770b\u677f\u6253\u5370 - v0.1");
//            jiraFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//            jiraFrame.setFont(new Font("Lucida Grande", Font.BOLD, 13));
//            jiraFrame.setResizable(false);
//            jiraFrame.addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowActivated(WindowEvent e) {
//                    jiraFrameWindowActivated(e);
//                }
//                @Override
//                public void windowClosing(WindowEvent e) {
//                    jiraFrameWindowClosing(e);
//                }
//            });
//            Container jiraFrameContentPane = jiraFrame.getContentPane();
//            jiraFrameContentPane.setLayout(new GridBagLayout());
//            ((GridBagLayout)jiraFrameContentPane.getLayout()).columnWidths = new int[] {0, 0};
//            ((GridBagLayout)jiraFrameContentPane.getLayout()).rowHeights = new int[] {0, 0};
//            ((GridBagLayout)jiraFrameContentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
//            ((GridBagLayout)jiraFrameContentPane.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
//
//            //======== dialogPane ========
//            {
//                dialogPane.setMinimumSize(new Dimension(812, 90));
//                dialogPane.setPreferredSize(new Dimension(655, 90));
//                dialogPane.setLayout(new GridBagLayout());
//                ((GridBagLayout)dialogPane.getLayout()).columnWidths = new int[] {0, 0, 0};
//                ((GridBagLayout)dialogPane.getLayout()).rowHeights = new int[] {77, 544, 25, 0};
//                ((GridBagLayout)dialogPane.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
//                ((GridBagLayout)dialogPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
//
//                //======== serverPanel ========
//                {
//                    serverPanel.setBackground(Color.lightGray);
//                    serverPanel.setPreferredSize(new Dimension(655, 80));
//                    serverPanel.setMinimumSize(new Dimension(812, 80));
//                    serverPanel.setBorder(new TitledBorder(null, "Jira", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
//                        new Font("Arial Black", Font.PLAIN, 16), Color.red));
//                    serverPanel.setMaximumSize(new Dimension(900, 80));
//                    serverPanel.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//                    serverPanel.setLayout(new GridBagLayout());
//                    ((GridBagLayout)serverPanel.getLayout()).columnWidths = new int[] {15, 77, 233, 71, 127, 71, 127, 86, 75, 0};
//                    ((GridBagLayout)serverPanel.getLayout()).rowHeights = new int[] {27, 0};
//                    ((GridBagLayout)serverPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
//                    ((GridBagLayout)serverPanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
//
//                    //---- label1 ----
//                    label1.setText("Jira\u5730\u5740");
//                    label1.setHorizontalTextPosition(SwingConstants.CENTER);
//                    label1.setFont(new Font("Lucida Grande", Font.BOLD, 13));
//                    serverPanel.add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
//                        new Insets(0, 0, 0, 7), 0, 0));
//
//                    //---- txtJiraUrl ----
//                    txtJiraUrl.setColumns(2);
//                    txtJiraUrl.setMinimumSize(new Dimension(50, 26));
//                    serverPanel.add(txtJiraUrl, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 7), 0, 0));
//
//                    //---- label2 ----
//                    label2.setText("\u7528\u6237\u540d");
//                    label2.setFont(new Font("Lucida Grande", Font.BOLD, 13));
//                    serverPanel.add(label2, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
//                        new Insets(0, 0, 0, 7), 0, 0));
//                    serverPanel.add(txtUser, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 7), 0, 0));
//
//                    //---- label3 ----
//                    label3.setText("\u5bc6\u7801");
//                    label3.setFont(new Font("Lucida Grande", Font.BOLD, 13));
//                    serverPanel.add(label3, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
//                        new Insets(0, 0, 0, 7), 0, 0));
//
//                    //---- txtPassword ----
//                    txtPassword.addKeyListener(new KeyAdapter() {
//                        @Override
//                        public void keyPressed(KeyEvent e) {
//                            txtPasswordKeyPressed(e);
//                        }
//                    });
//                    serverPanel.add(txtPassword, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 7), 0, 0));
//
//                    //---- btnConnect ----
//                    btnConnect.setText("\u767b\u5f55");
//                    btnConnect.setBackground(Color.lightGray);
//                    btnConnect.setOpaque(true);
//                    btnConnect.setFont(new Font("Lucida Grande", Font.BOLD, 13));
//                    btnConnect.addMouseListener(new MouseAdapter() {
//                        @Override
//                        public void mouseClicked(MouseEvent e) {
//                            btnConnectMouseClicked(e);
//                        }
//                        @Override
//                        public void mousePressed(MouseEvent e) {
//                            btnConnectMousePressed(e);
//                        }
//                        @Override
//                        public void mouseReleased(MouseEvent e) {
//                            btnConnectMouseReleased(e);
//                        }
//                    });
//                    serverPanel.add(btnConnect, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 7), 0, 0));
//
//                    //---- btnLogout ----
//                    btnLogout.setText("\u65ad\u5f00");
//                    btnLogout.setBackground(Color.lightGray);
//                    btnLogout.setOpaque(true);
//                    btnLogout.setFont(new Font("Lucida Grande", Font.BOLD, 13));
//                    btnLogout.addMouseListener(new MouseAdapter() {
//                        @Override
//                        public void mouseClicked(MouseEvent e) {
//                            btnLogoutMouseClicked(e);
//                        }
//                    });
//                    serverPanel.add(btnLogout, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 0), 0, 0));
//                }
//                dialogPane.add(serverPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 0, 0), 0, 0));
//
//                //======== statusPanel ========
//                {
//                    statusPanel.setBackground(Color.lightGray);
//                    statusPanel.setPreferredSize(new Dimension(346, 30));
//                    statusPanel.setBorder(LineBorder.createBlackLineBorder());
//                    statusPanel.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//                    statusPanel.setLayout(new GridBagLayout());
//                    ((GridBagLayout)statusPanel.getLayout()).columnWidths = new int[] {90, 76, 78, 157, 107, 106, 97, 57, 63, 0};
//                    ((GridBagLayout)statusPanel.getLayout()).rowHeights = new int[] {30, 0};
//                    ((GridBagLayout)statusPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
//                    ((GridBagLayout)statusPanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
//
//                    //---- jiraconnection ----
//                    jiraconnection.setText("Jira\u8fde\u63a5\u72b6\u6001:");
//                    jiraconnection.setHorizontalAlignment(SwingConstants.RIGHT);
//                    jiraconnection.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//                    jiraconnection.setBackground(Color.white);
//                    statusPanel.add(jiraconnection, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- lblStatus ----
//                    lblStatus.setText("\u672a\u8fde\u63a5");
//                    lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
//                    lblStatus.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//                    lblStatus.setBackground(Color.white);
//                    statusPanel.add(lblStatus, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- lblStatus2 ----
//                    lblStatus2.setText("\u67e5\u8be2\u72b6\u6001:");
//                    lblStatus2.setHorizontalAlignment(SwingConstants.RIGHT);
//                    lblStatus2.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//                    lblStatus2.setBackground(Color.white);
//                    statusPanel.add(lblStatus2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- lblQueryStatus ----
//                    lblQueryStatus.setHorizontalAlignment(SwingConstants.LEFT);
//                    lblQueryStatus.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//                    lblQueryStatus.setBackground(Color.white);
//                    statusPanel.add(lblQueryStatus, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- lblBoardCount ----
//                    lblBoardCount.setText("Boards:[]\u4e2a");
//                    lblBoardCount.setHorizontalAlignment(SwingConstants.CENTER);
//                    lblBoardCount.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//                    statusPanel.add(lblBoardCount, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- lblSprintCount ----
//                    lblSprintCount.setText("Sprints:[]\u4e2a");
//                    lblSprintCount.setHorizontalAlignment(SwingConstants.CENTER);
//                    lblSprintCount.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//                    statusPanel.add(lblSprintCount, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 5), 0, 0));
//
//                    //---- lblIssueCount ----
//                    lblIssueCount.setText("\u9009\u4e2dIssues:[]\u4e2a");
//                    lblIssueCount.setHorizontalAlignment(SwingConstants.CENTER);
//                    lblIssueCount.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
//                    statusPanel.add(lblIssueCount, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//                        new Insets(0, 0, 0, 5), 0, 0));
//                }
//                dialogPane.add(statusPanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 0, 0), 0, 0));
//
//                //======== boardPanel ========
//                {
//                    boardPanel.setBackground(Color.white);
//                    boardPanel.setBorder(LineBorder.createBlackLineBorder());
//                    boardPanel.setLayout(new GridBagLayout());
//                    ((GridBagLayout)boardPanel.getLayout()).columnWidths = new int[] {10, 225, 224, 431, 0};
//                    ((GridBagLayout)boardPanel.getLayout()).rowHeights = new int[] {53, 41, 391, 59, 0};
//                    ((GridBagLayout)boardPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
//                    ((GridBagLayout)boardPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
//
//                    //---- label4 ----
//                    label4.setText("Boards:");
//                    label4.setHorizontalAlignment(SwingConstants.CENTER);
//                    label4.setFont(new Font("Tahoma", Font.BOLD, 13));
//                    label4.setIcon(new ImageIcon(getClass().getResource("/images/jira/board.png")));
//                    boardPanel.add(label4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 5, 5), 0, 0));
//
//                    //---- label5 ----
//                    label5.setText("Sprints");
//                    label5.setFont(new Font("Tahoma", Font.BOLD, 13));
//                    label5.setIcon(new ImageIcon(getClass().getResource("/images/jira/sprint.png")));
//                    boardPanel.add(label5, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
//                        new Insets(0, 0, 5, 5), 0, 0));
//
//                    //---- label6 ----
//                    label6.setText("Story / Task / Subtask");
//                    label6.setFont(new Font("Tahoma", Font.BOLD, 13));
//                    label6.setIcon(new ImageIcon(getClass().getResource("/images/jira/card.png")));
//                    boardPanel.add(label6, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
//                        new Insets(0, 0, 5, 0), 0, 0));
//
//                    //======== scrollPane1 ========
//                    {
//                        scrollPane1.setBorder(LineBorder.createBlackLineBorder());
//
//                        //---- txtFilterBoard ----
//                        txtFilterBoard.setToolTipText("\u6a21\u7cca\u67e5\u8be2\u56de\u8f66\u8fc7\u6ee4");
//                        txtFilterBoard.setBorder(LineBorder.createBlackLineBorder());
//                        txtFilterBoard.setText("\u6a21\u7cca\u67e5\u8be2\u56de\u8f66\u8fc7\u6ee4");
//                        txtFilterBoard.setForeground(Color.blue);
//                        txtFilterBoard.setPreferredSize(new Dimension(90, 25));
//                        txtFilterBoard.setEnabled(false);
//                        txtFilterBoard.setBackground(new Color(214, 217, 223));
//                        txtFilterBoard.setOpaque(true);
//                        txtFilterBoard.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
//                        txtFilterBoard.addKeyListener(new KeyAdapter() {
//                            @Override
//                            public void keyReleased(KeyEvent e) {
//                                txtFilterBoardKeyReleased(e);
//                            }
//                        });
//                        txtFilterBoard.addFocusListener(new FocusAdapter() {
//                            @Override
//                            public void focusGained(FocusEvent e) {
//                                txtFilterBoardFocusGained(e);
//                            }
//                            @Override
//                            public void focusLost(FocusEvent e) {
//                                txtFilterBoardFocusLost(e);
//                            }
//                        });
//                        scrollPane1.setViewportView(txtFilterBoard);
//                    }
//                    boardPanel.add(scrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 5, 5), 0, 0));
//
//                    //======== scrollPane2 ========
//                    {
//                        scrollPane2.setBorder(LineBorder.createBlackLineBorder());
//                        scrollPane2.setBackground(Color.white);
//
//                        //---- lblSelectedBoard ----
//                        lblSelectedBoard.setFont(new Font("Lucida Grande", Font.BOLD, 12));
//                        lblSelectedBoard.setBorder(LineBorder.createBlackLineBorder());
//                        lblSelectedBoard.setBackground(new Color(214, 217, 223));
//                        lblSelectedBoard.setOpaque(true);
//                        scrollPane2.setViewportView(lblSelectedBoard);
//                    }
//                    boardPanel.add(scrollPane2, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 5, 5), 0, 0));
//
//                    //======== scrollPane3 ========
//                    {
//                        scrollPane3.setBorder(LineBorder.createBlackLineBorder());
//
//                        //---- lblSelectedBoardSprint ----
//                        lblSelectedBoardSprint.setFont(new Font("Lucida Grande", Font.BOLD, 12));
//                        lblSelectedBoardSprint.setBorder(LineBorder.createBlackLineBorder());
//                        lblSelectedBoardSprint.setOpaque(true);
//                        lblSelectedBoardSprint.setBackground(new Color(214, 217, 223));
//                        scrollPane3.setViewportView(lblSelectedBoardSprint);
//                    }
//                    boardPanel.add(scrollPane3, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 5, 0), 0, 0));
//                    boardPanel.add(scrollPaneBoard, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 5, 5), 0, 0));
//                    boardPanel.add(scrollPaneSprint, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 5, 5), 0, 0));
//                    boardPanel.add(scrollPaneCard, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 5, 0), 0, 0));
//
//                    //======== panel1 ========
//                    {
//                        panel1.setBackground(Color.white);
//                        panel1.setLayout(new GridBagLayout());
//                        ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {52, 78, 0, 0, 54, 139, 0, 0};
//                        ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 45, 0};
//                        ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
//                        ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};
//
//                        //---- optAll ----
//                        optAll.setText("\u5168\u9009");
//                        optAll.setFont(new Font("Lucida Grande", Font.BOLD, 13));
//                        optAll.addMouseListener(new MouseAdapter() {
//                            @Override
//                            public void mouseClicked(MouseEvent e) {
//                                optAllMouseClicked(e);
//                            }
//                        });
//                        panel1.add(optAll, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
//                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                            new Insets(0, 0, 0, 5), 0, 0));
//
//                        //---- optCancel ----
//                        optCancel.setText("\u53d6\u6d88\u5168\u9009");
//                        optCancel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
//                        optCancel.addMouseListener(new MouseAdapter() {
//                            @Override
//                            public void mouseClicked(MouseEvent e) {
//                                optCancelMouseClicked(e);
//                            }
//                        });
//                        panel1.add(optCancel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
//                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                            new Insets(0, 0, 0, 5), 0, 0));
//
//                        //---- btnGenerate ----
//                        btnGenerate.setText("\u751f\u6210HTML");
//                        btnGenerate.setIcon(new ImageIcon(getClass().getResource("/images/printer.png")));
//                        btnGenerate.setPreferredSize(new Dimension(121, 10));
//                        btnGenerate.setMinimumSize(new Dimension(121, 30));
//                        btnGenerate.setMaximumSize(new Dimension(121, 30));
//                        btnGenerate.setBackground(SystemColor.textHighlight);
//                        btnGenerate.setFont(new Font("Lucida Grande", Font.BOLD, 13));
//                        btnGenerate.setEnabled(false);
//                        btnGenerate.addMouseListener(new MouseAdapter() {
//                            @Override
//                            public void mouseClicked(MouseEvent e) {
//                                btnGenerateMouseClicked(e);
//                            }
//                        });
//                        panel1.add(btnGenerate, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0,
//                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                            new Insets(0, 0, 0, 5), 0, 0));
//                    }
//                    boardPanel.add(panel1, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0,
//                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                        new Insets(0, 0, 0, 0), 0, 0));
//                }
//                dialogPane.add(boardPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
//                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                    new Insets(0, 0, 0, 0), 0, 0));
//            }
//            jiraFrameContentPane.add(dialogPane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
//                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                new Insets(0, 0, 0, 0), 0, 0));
//            jiraFrame.setSize(905, 705);
//            jiraFrame.setLocationRelativeTo(null);
//        }
//
//        //---- buttonGroup1 ----
//        ButtonGroup buttonGroup1 = new ButtonGroup();
//        buttonGroup1.add(optAll);
//        buttonGroup1.add(optCancel);
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//
//
//
//        // 888888888888888888888888888
//        jListBoards.setFont(new Font("宋体", Font.PLAIN, 12));
//        jListBoards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        jListBoards.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() >= 1 && jListBoards.getSelectedIndex() > -1) {
//                    asyncQuerySprints();
//                }
//            }
//        });
////        jListBoards.addListSelectionListener(new ListSelectionListener() {
////            @Override
////            public void valueChanged(ListSelectionEvent e) {
////                if (jListBoards.getSelectedIndex() > -1 && !e.getValueIsAdjusting()) {
////                    asyncQuerySprints();
////                }
////            }
////        });
//
//        jlistSprints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        jlistSprints.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() >= 1 && jlistSprints.getSelectedIndex() > -1) {
//                    asyncQueryTree();
//                }
//            }
//
//        });
//
//    }
//
//    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
//    private JFrame jiraFrame;
//    private JPanel dialogPane;
//    private JPanel serverPanel;
//    private JLabel label1;
//    private JTextField txtJiraUrl;
//    private JLabel label2;
//    private JTextField txtUser;
//    private JLabel label3;
//    private JPasswordField txtPassword;
//    private JButton btnConnect;
//    private JButton btnLogout;
//    private JPanel statusPanel;
//    private JLabel jiraconnection;
//    private JLabel lblStatus;
//    private JLabel lblStatus2;
//    private JLabel lblQueryStatus;
//    private JLabel lblBoardCount;
//    private JLabel lblSprintCount;
//    private JLabel lblIssueCount;
//    private JPanel boardPanel;
//    private JLabel label4;
//    private JLabel label5;
//    private JLabel label6;
//    private JScrollPane scrollPane1;
//    private JTextField txtFilterBoard;
//    private JScrollPane scrollPane2;
//    private JLabel lblSelectedBoard;
//    private JScrollPane scrollPane3;
//    private JLabel lblSelectedBoardSprint;
//    private JScrollPane scrollPaneBoard;
//    private JScrollPane scrollPaneSprint;
//    private JScrollPane scrollPaneCard;
//    private JPanel panel1;
//    private JRadioButton optAll;
//    private JRadioButton optCancel;
//    private JButton btnGenerate;
//    // JFormDesigner - End of variables declaration  //GEN-END:variables
//}

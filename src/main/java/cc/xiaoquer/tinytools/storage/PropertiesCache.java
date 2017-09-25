package cc.xiaoquer.tinytools.storage;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.io.*;
import java.util.Properties;

/**
 * Created by Nicholas on 2017/9/5.
 */
public class PropertiesCache {
    public static final String  CONFIG_PATH                 = System.getProperty("user.dir") + File.separator + "TT_FILES";
    private static final String CONFIG_FILE_NAME            = CONFIG_PATH + File.separator +"conf" + File.separator + "conf.properties";
    private static File         CONFIG_FILE                 = new File(CONFIG_FILE_NAME);
    private static long         CONFIG_FILE_LAST_MODIFIED   = 0L;

    private static final String CONFIG_BACKUP_FILE_NAME     = "conf-{}.properties";

    private static final String KEY = System.getProperty("java.version");

    private static final Properties configProp = new Properties();
    private static Properties baselineProp = new Properties();

    private PropertiesCache() {
    }

    static {
        baselineProp.put("OWNER","nicholas.qu");
        readFile();
    }

    public static void loadCache(JTextComponent... components) {
        if (components == null) return;

        readFile();
        for (JTextComponent component : components) {
            if (component.getText().length() == 0) {
                if (component instanceof JPasswordField) {
                    component.setText(PropertiesCache.getPassword(component.getName()));
                } else {
                    component.setText(PropertiesCache.getValue(component.getName()));
                }
            }
        }
    }

    public static void saveCache(JTextComponent... components) {
        if (components == null) return;

        for (JTextComponent component : components) {
            if (component instanceof JPasswordField) {
                PropertiesCache.setPassword(component.getName(), component.getText());
            } else {
                PropertiesCache.setValue(component.getName(), component.getText());
            }
        }

        flush();
    }

    public static void backupCacheFile(String backupKey) {
        //创建备份文件
        File backUpFile = new File(CONFIG_FILE_NAME, CONFIG_BACKUP_FILE_NAME.replace("{}", backupKey));
        try {
            if (!backUpFile.exists()) {
                backUpFile.createNewFile();
            }

            //创建备份文件成功，进行文件复制
            fileCopy(CONFIG_FILE, backUpFile);
            System.out.println("备份文件 " + backUpFile.getAbsolutePath() + " 成功");

        } catch (Exception e) {
            System.out.println("备份文件失败");
        }
    }

    public static void restoreCacheFile(String backupKey) {
        //创建备份文件
        File backUpFile = new File(CONFIG_FILE_NAME, CONFIG_BACKUP_FILE_NAME.replace("{}", backupKey));
        try {
            if (backUpFile.exists()) {
                //创建备份文件成功，进行文件复制
                fileCopy(backUpFile, CONFIG_FILE);
                System.out.println("恢复备份文件 " + backUpFile.getAbsolutePath() + " 成功");
            } else {
                System.out.println("可恢复的备份文件不存在");
            }
        } catch (Exception e) {
            System.out.println("恢复备份文件失败");
        }

        //还原备份文件后，需要重新load属性值。
    }

    public static void fileCopy(File srcFile, File destFile) throws Exception {
        InputStream src = new BufferedInputStream(new FileInputStream(srcFile));
        OutputStream dest = new BufferedOutputStream(new FileOutputStream(destFile));

        byte[] trans = new byte[1024];

        int count = -1;

        while ((count = src.read(trans)) != -1) {
            dest.write(trans, 0, count);
        }

        dest.flush();
        src.close();
        dest.close();
    }

    public static Properties readFile() {
        if (!CONFIG_FILE.exists()) {
            try {
                CONFIG_FILE.getParentFile().mkdirs();
                CONFIG_FILE.createNewFile();
            } catch (IOException e) {
            }
        }

        //文件没有变动过
        if (CONFIG_FILE_LAST_MODIFIED == CONFIG_FILE.lastModified()) {
            return configProp;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(CONFIG_FILE);
            configProp.load(reader);

            baselineProp.clear();
            baselineProp.putAll(configProp);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configProp;
    }

    public static void flush() {
        if (baselineProp.hashCode() == configProp.hashCode()) {
            return;
        }
        System.out.println("FLUSH CACHE.");


        if (!CONFIG_FILE.exists()) {
            try {
                CONFIG_FILE.getParentFile().mkdirs();
                CONFIG_FILE.createNewFile();
            } catch (IOException e) {
            }
            System.out.println("生成缓存文件..." + CONFIG_FILE.getAbsolutePath());
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(CONFIG_FILE);
            configProp.store(writer, "Tiny Tools Cache");
            baselineProp.putAll(configProp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void setValue(String key, String value) {
        configProp.put(key, value);
    }

    public static String getValue(String key) {
        return (String)configProp.get(key);
    }

    public static void setPassword(String key, String value) {
        try {
            configProp.put(key, encryption(value));
        } catch (UnsupportedEncodingException e) {
        }
    }

    public static String getPassword(String key) {
        String pwd = (String)configProp.get(key);

        try {
            return decipher(pwd);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private static String encryption(String content) throws UnsupportedEncodingException {
        if (content == null) {
            return "";
        }

        byte[] contentBytes = content.getBytes();
        byte[] keyBytes = KEY.getBytes();

        byte dkey = 0;
        for(byte b : keyBytes){
            dkey ^= b;
        }

        byte salt = 0;  //随机盐值
        byte[] result = new byte[contentBytes.length];
        for(int i = 0 ; i < contentBytes.length; i++){
            salt = (byte)(contentBytes[i] ^ dkey ^ salt);
            result[i] = salt;
        }
        return new String(result, "utf-8");
    }

    private static String decipher(String content) throws UnsupportedEncodingException {
        if (content == null) {
            return "";
        }
        byte[] contentBytes = content.getBytes();
        byte[] keyBytes = KEY.getBytes();

        byte dkey = 0;
        for(byte b : keyBytes){
            dkey ^= b;
        }

        byte salt = 0;  //随机盐值
        byte[] result = new byte[contentBytes.length];
        for(int i = contentBytes.length - 1 ; i >= 0 ; i--){
            if(i == 0){
                salt = 0;
            }else{
                salt = contentBytes[i - 1];
            }
            result[i] = (byte)(contentBytes[i] ^ dkey ^ salt);
        }
        return new String(result, "utf-8");
    }

    public static void main(String[] args) {
        backupCacheFile("aaaaa");
        restoreCacheFile("aaaaa");
    }

}
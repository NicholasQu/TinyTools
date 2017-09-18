package cc.xiaoquer.tinytools.utils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by Nicholas on 2017/9/14.
 */
public class ToolUtils {

    public static String trim(Object s) {
        return s == null ? "" : ((String)s).trim();
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static void deleteFiles(String dir, String fileNamePrefix) {
        File directory = new File(dir);
        if (!directory.exists()) {
            System.out.println("dir is not valid");
            return;
        }

        for (File file : directory.listFiles()) {
            if (file.getName().startsWith(fileNamePrefix)) {
                file.delete();
            }
        }
    }
}

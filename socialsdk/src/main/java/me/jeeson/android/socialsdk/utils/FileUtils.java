package me.jeeson.android.socialsdk.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import me.jeeson.android.socialsdk.SocialSDK;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


/**
 * 文件工具类
 * Created by Jeeson on 2018/6/20.
 */
public class FileUtils {

    public static final String TAG = FileUtils.class.getSimpleName();

    public static final String POINT_GIF = ".gif";
    public static final String POINT_JPG = ".jpg";
    public static final String POINT_JPEG = ".jpeg";
    public static final String POINT_PNG = ".png";


    /**
     * 从文件中获取输出流
     *
     * @param path 路径
     * @return 输出流
     */
    public static ByteArrayOutputStream getOutputStreamFromFile(String path) {
        if (!FileUtils.isExist(path))
            return null;
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(path);
            baos = new ByteArrayOutputStream();
            byte[] b = new byte[8 * 1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                baos.write(b, 0, n);
            }
            return baos;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtils.closeStream(fis, baos);
        }
        return baos;
    }

    /**
     * 文件后缀
     *
     * @param path 路径
     * @return 后缀名
     */
    public static String getSuffix(String path) {
        if (!TextUtils.isEmpty(path)) {
            int lineIndex = path.lastIndexOf("/");
            if (lineIndex != -1) {
                String fileName = path.substring(lineIndex, path.length());
                if (!TextUtils.isEmpty(fileName)) {
                    int pointIndex = fileName.lastIndexOf(".");
                    if (pointIndex != -1) {
                        String suffix = fileName.substring(pointIndex, fileName.length());
                        if (!TextUtils.isEmpty(suffix)) {
                            return suffix;
                        }
                    }

                }
            }
        }
        return "";
    }

    /**
     * @param path 路径
     * @return 是否是 gif 文件
     */
    public static boolean isGifFile(String path) {
        return isGIFImage(new File(path));
    }

    /**
     * @param path 路径
     * @return 是不是 jpg || png
     */
    public static boolean isJpgPngFile(String path) {
        return isJpgFile(path) || isPngFile(path);
    }

    /**
     * @param path 路径
     * @return 是不是 jpg 文件
     */
    public static boolean isJpgFile(String path) {
        return isJPGImage(new File(path));
    }

    /**
     * 通过图片头获取图片文件的扩展名
     *
     * @param saveFile
     * @return
     */
    public static String getImageFileEndName(File saveFile) {
        if (saveFile != null && saveFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(saveFile.getAbsolutePath(), options);
            String miniType = options.outMimeType;
            if (miniType != null && miniType.startsWith("image/")) {
                return miniType.replaceFirst("image/", "");
            }
        }
        return null;
    }

    /**
     * 通过文件头判断图片是否为GIF
     *
     * @param imagePath File类型传参
     * @return
     */
    public static boolean isGIFImage(File imagePath) {
        String endName = FileUtils.getImageFileEndName(imagePath);
        return !TextUtils.isEmpty(endName) && endName.toUpperCase().contains("GIF");
    }

    /**
     * 通过文件头判断图片是否为PNG
     *
     * @param imagePath File类型传参
     * @return
     */
    public static boolean isPNGImage(File imagePath) {
        String endName = FileUtils.getImageFileEndName(imagePath);
        return !TextUtils.isEmpty(endName) && endName.toUpperCase().contains("PNG");
    }

    /**
     * 通过文件头判断图片是否为JPG
     *
     * @param imagePath File类型传参
     * @return
     */
    public static boolean isJPGImage(File imagePath) {
        String endName = FileUtils.getImageFileEndName(imagePath);
        return !TextUtils.isEmpty(endName) && (endName.toUpperCase().contains("JPG") || endName.toUpperCase().contains("JPEG"));
    }


    /**
     * @param path 路径
     * @return 是不是 png 文件
     */
    public static boolean isPngFile(String path) {
        return isPNGImage(new File(path));
    }

    /**
     * @param path 路径
     * @return 是不是 图片 文件
     */
    public static boolean isPicFile(String path) {
        return isJpgPngFile(path) || isGifFile(path);
    }

    /**
     * @param path 路径
     * @return 文件是否存在
     */
    public static boolean isExist(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        File file = new File(path);
        return file.exists() && file.length() > 0;
    }

    /**
     * @param file 文件
     * @return 文件是否存在
     */
    public static boolean isExist(File file) {
        return file != null && isExist(file.getAbsolutePath());
    }

    /**
     * @param path 路径
     * @return 是不是 http 路径
     */
    public static boolean isHttpPath(String path) {
        return path.toLowerCase().startsWith("http");
    }

    /**
     * 网络路径映射本地路径
     *
     * @param url 网络路径
     * @return 映射的本地路径
     */
    public static String mapUrl2LocalPath(String url) {
        // 映射文件名
        String suffix = FileUtils.getSuffix(url);
        suffix = TextUtils.isEmpty(suffix) ? ".png" : suffix;
        String fileName = CommonUtils.getMD5(url) + suffix;
        File saveFile = new File(SocialSDK.getConfig().getShareCacheDirPath(), fileName);
        return saveFile.getAbsolutePath();
    }


    /**
     * 将资源图片映射到本地文件存储，同一张图片不必重复decode
     *
     * @param context ctx
     * @param resId   资源ID
     * @return 路径
     */
    public static String mapResId2LocalPath(Context context, int resId) {
        String fileName = CommonUtils.getMD5(resId + "") + FileUtils.POINT_PNG;
        File saveFile = new File(SocialSDK.getConfig().getShareCacheDirPath(), fileName);
        if (saveFile.exists())
            return saveFile.getAbsolutePath();
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(saveFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            BitmapUtils.recyclerBitmaps(bitmap);
        }
        return saveFile.getAbsolutePath();
    }

    public static boolean hasStoragePermission(Context context) {
        boolean hasPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasPermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return hasPermission;
    }


}



package com.example.stm32update.Ymodem;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Get InputStream from different source, files from sd card/assets supported.
 */

public class InputStreamSource {
    //文件容量大小改为 32*256
    private static final int BUFFER_SIZE = 32 * 256;
    //private static final String ERROR_UNSUPPORTED_SCHEME = "Unsupported file source";

    InputStream getStream(Context context, String imageUri) throws IOException {
        switch (SourceScheme.ofUri(imageUri)) {
            case FILE:
                return getStreamFromFile(imageUri);

            case ASSETS:
                return getStreamFromAssets(context, imageUri);

            case UNKNOWN:
            default:
                return getStreamFromOtherSource(imageUri);
        }
    }

    private InputStream getStreamFromFile(String fileUri) throws IOException {
        String filePath = SourceScheme.FILE.crop(fileUri);
        return new BufferedInputStream(new FileInputStream(filePath), BUFFER_SIZE);
    }

    private InputStream getStreamFromAssets(Context context, String fileUri) throws IOException {
        String filePath = SourceScheme.ASSETS.crop(fileUri);
        return context.getAssets().open(filePath);
    }

    /**
     * 从其它的地方获取数据
     */
    private InputStream getStreamFromOtherSource(String fileUri) throws IOException {
        return new BufferedInputStream(new FileInputStream(fileUri), BUFFER_SIZE);
        //throw new UnsupportedOperationException(String.format(ERROR_UNSUPPORTED_SCHEME, fileUri));
    }

}

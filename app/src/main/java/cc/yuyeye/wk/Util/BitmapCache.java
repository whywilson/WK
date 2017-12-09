package cc.yuyeye.wk.Util;

import android.graphics.*;
import android.util.*;
import com.android.volley.toolbox.ImageLoader.*;

public class BitmapCache implements ImageCache {
    private LruCache<String, Bitmap> mCache;

    public BitmapCache() {
        int maxSize = 15 * 1920 * 1920;
        mCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    @Override
    public Bitmap getBitmap(String url) {
        return mCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        if (mCache.get(url) == null) {
            Log.i("ImageUrl","putBitmap "+ url);
            mCache.put(url, bitmap);
        }
    }

}

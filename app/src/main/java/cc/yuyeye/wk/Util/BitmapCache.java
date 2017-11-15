package cc.yuyeye.wk.Util;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

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
            LogUtil.i("ImageUrl","putBitmap "+ url);
            mCache.put(url, bitmap);
        }
    }

}

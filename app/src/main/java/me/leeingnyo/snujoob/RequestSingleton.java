package me.leeingnyo.snujoob;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class RequestSingleton {
    private static RequestSingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    private static final String HOST = "http://leeingnyo.me:10010/";

    private RequestSingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized RequestSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public static String getLoginUrl(){
        return HOST + "login";
    }
    public static String getAutoLoginUrl(){
        return HOST + "auto_login";
    }
    public static String getJoinUrl(){
        return HOST + "join";
    }
    public static String getSearchUrl(){
        return HOST + "search";
    }
    public static String getRegisterUrl(String studentId){
        return HOST + "users/" + studentId + "/register";
    }
    public static String getWatchrUrl(String studentId){
        return HOST + "users/" + studentId + "/watch";
    }
    public static String getUserUrl(String studentId){
        return HOST + "users/" + studentId;
    }
    public static String getUpdateGcmUrl(String studentId){
        return HOST + "users/" + studentId + "/update_gcm";
    }
}

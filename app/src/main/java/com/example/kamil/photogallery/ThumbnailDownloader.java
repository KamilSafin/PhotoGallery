package com.example.kamil.photogallery;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {

    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestThumbnails = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private OnThumbnailDownloadedListener<T> mOnThumbnailDownloadedListener;
    private int maxCache = 2 * 1024 * 1024;
    private LruCache<String, Bitmap> mCache = new LruCache<>(maxCache);

    public ThumbnailDownloader(Handler responseHandler) {
        super("ThumbnailDownloader");
        mResponseHandler = responseHandler;
    }

    public interface OnThumbnailDownloadedListener<T> {
        void onThumbnailDownloaded(T obj, Bitmap bitmap);
    }

    public void setOnThumbnailDownloadedListener(OnThumbnailDownloadedListener<T> listener) {
        mOnThumbnailDownloadedListener = listener;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    handleBitmap((T) msg.obj);
                }
            }
        };
    }

    public void queueThumbnail(T object, List<GalleryItem> list, int position) {

        if (list.get(position).getmUrl() == null) {
            mRequestThumbnails.remove(object);
        }
        mRequestThumbnails.put(object, list.get(position).getmUrl());
        for (int i = position; i < (list.size() - position > 20 ? (position + 20) : (position + list.size() - position)); i++) {
                mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, object).sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    private void handleBitmap(final T object) {
        try {
            final String url = mRequestThumbnails.get(object);

            if (url == null) {
                return;
            }

            final Bitmap bitmap;
            if (mCache.get(url) == null) {
                byte[] drawableBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(drawableBytes, 0, drawableBytes.length);
            } else {
                bitmap = mCache.get(url);
            }

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestThumbnails.get(object) != url) {
                        return;
                    }

                    mRequestThumbnails.remove(object);
                    mCache.put(url, bitmap);
                    mOnThumbnailDownloadedListener.onThumbnailDownloaded(object, bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e("Kamil",ioe.toString());
        }
    }
}

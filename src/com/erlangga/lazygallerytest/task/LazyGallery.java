package com.erlangga.lazygallerytest.task;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.ImageView;

/**
 * @author Erlangga
 *
 */

public class LazyGallery {
	private ExecutorService mThreadPool;
	private static final int THREAD_POOL_SIZE = 3;
	private HashMap<String, SoftReference<Bitmap>> imageCache;
	
	public interface ImageCallback{
		public void imageLoaded(Bitmap bitmap);		
	}
	
	public LazyGallery(){
		mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}
	
	/**
	 * Clears all instance data and stops running threads
	 */
	private void reset() {
	    ExecutorService oldThreadPool = mThreadPool;
	    mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	    oldThreadPool.shutdownNow();	   
	}
	
	public Bitmap loadGallery(final Context context, final int id, final ImageView imageView, final ImageCallback imageCallback) {
		final String tmpStrId = String.valueOf(id);
		if(imageCache.containsKey(tmpStrId)){
			SoftReference<Bitmap> softReference = imageCache.get(tmpStrId);
			Bitmap bmp = softReference.get();
			if(bmp!=null){
				return bmp;
			}
		}
		
		final Handler handler = new Handler(){
			@Override
            public void handleMessage(Message message) {
				if(imageView.isShown())
				if (message.obj != null) {
					Bitmap bmp = (Bitmap) message.obj;
					imageCache.put(tmpStrId, new SoftReference<Bitmap>(bmp));
	                imageCallback.imageLoaded((Bitmap) message.obj);
				}				
            }
		};
		
		mThreadPool.submit(new Runnable() {				
			public void run() {
				Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(
						context.getContentResolver(), id,
						MediaStore.Images.Thumbnails.MICRO_KIND, null);
				if (imageView.isShown()){
	                Message message = Message.obtain();  
	                message.obj = bmp;
	                handler.sendMessage(message);	           
				}
			}
		});		
		return null;		
	}
}

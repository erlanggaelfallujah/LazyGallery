package com.erlangga.lazygallerytest.activity;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.erlangga.lazygallerytest.pojo.GridImageItem;
import com.erlangga.lazygallerytest.task.LazyGallery;
import com.erlangga.lazygallerytest.task.LazyGallery.ImageCallback;

/**
 * @author Erlangga
 *
 */

public class ImageSelector extends Activity {
	private int count;
	private ImageAdapter imageAdapter;
	private List<String> fileSelectorList;
	
	private LinkedList<GridImageItem> gridImageItemList;
	private GridView imagegrid = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_selector);	
		
		gridImageItemList = new LinkedList<GridImageItem>();
		fileSelectorList = new LinkedList<String>();
		
		imagegrid = (GridView) findViewById(R.id.imageGrid);
			
		new LoadImageGalleryTask(this).execute();		
	}
	
	private class LoadImageGalleryTask extends AsyncTask<Void, Void, Void>{		
		private Context context;
		private ProgressDialog progressDialog;
		private Cursor imagecursor = null;
		private int image_column_index;
		
		public LoadImageGalleryTask(Context context){
			this.context = context;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			imageAdapter = new ImageAdapter(context);
			imagegrid.setAdapter(imageAdapter);
			imagecursor.close();
			
			final Button selectBtn = (Button) findViewById(R.id.btnDone);
			selectBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {							
					int size = fileSelectorList.size();
					if(size>0){						
						String fileSelected = "You have selected : \n";
						for(int i=0;i<size;i++){
							String path = fileSelectorList.get(i) + "\n";
							fileSelected+=path;
						}
						Toast.makeText(context, fileSelected, Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(context, "You do not choose one of these files", Toast.LENGTH_LONG).show();
					}
				}
			});
			
			progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Loading... Please wait...");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.show();											
			
			String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
			String orderBy = MediaStore.Images.Media._ID;		
			
			CursorLoader loader = new CursorLoader(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
			imagecursor = loader.loadInBackground();			
		}

		@Override
		protected Void doInBackground(Void... params) {	
//			final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
//			final String orderBy = MediaStore.Images.Media._ID;
			
//			imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

//			managedQuery is deprecated, Ref : http://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore		
//			imagecursor = managedQuery(
//			MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
//			null, orderBy);		
			
			image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
			count = imagecursor.getCount();
			
			for (int i = 0; i <count; i++) {
				imagecursor.moveToPosition(i);
				int id = imagecursor.getInt(image_column_index);
				int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);				
				String arrPath = imagecursor.getString(dataColumnIndex);
				
				GridImageItem item = new GridImageItem();
				item.setId(id);
				item.setDataColumnIndex(dataColumnIndex);
				item.setArrPath(arrPath);				
				
				gridImageItemList.add(item);
			}												
			return null;
		}		
	}	
	
	public class ImageAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context context;
		private LazyGallery lazyGallery;
		
		public ImageAdapter(Context context) {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			lazyGallery = new LazyGallery();
			this.context = context;
		}

		public int getCount() {
			return count;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(
						R.layout.galleryitem, null);
				holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
				holder.checkbox = (CheckBox) convertView.findViewById(R.id.itemCheckBox);

				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.checkbox.setId(position);
			holder.imageview.setId(position);
			holder.checkbox.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {					
					CheckBox cb = (CheckBox) v;
					boolean thumbnailsselection = gridImageItemList.get(position).isThumbnailsselection();
					String arrPath = gridImageItemList.get(position).getArrPath();				
					if (thumbnailsselection){
						fileSelectorList.remove(arrPath);
						cb.setChecked(false);
						gridImageItemList.get(position).setThumbnailsselection(false);										
					} else {
						fileSelectorList.add(arrPath);											
						cb.setChecked(true);
						gridImageItemList.get(position).setThumbnailsselection(true);
					}
				}
			});
			
			lazyGallery.loadGallery(context, gridImageItemList.get(position).getId(), holder.imageview, new ImageCallback() {				
				public void imageLoaded(Bitmap bitmap) {
					holder.imageview.setImageBitmap(bitmap);
				}
			});
						
			holder.checkbox.setChecked(gridImageItemList.get(position).isThumbnailsselection());
			return convertView;
		}
	}
	
	private class ViewHolder {
		ImageView imageview;
		CheckBox checkbox;
	}	
}

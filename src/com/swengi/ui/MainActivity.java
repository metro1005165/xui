package com.swengi.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.swengi.db.ArticleTable;
import com.swengi.db.SwengiContentProvider;
import com.swengi.hs_v1.R;
import com.swengi.network.DownloadData;
import com.swengi.ui.GridViewCursorAdapter.ViewHolder;

public class MainActivity extends Activity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	// Fixed number of deps 
	private final int DEP_NUMBER = 8;
	
	// For optimization check
	private long start;
	private long finish;

	// Database uri and projection
	private final String[] projection = { ArticleTable.ID, ArticleTable.MAIN_HEADER,
			ArticleTable.DEPARTMENT_ID };
	private final Uri uri = SwengiContentProvider.ARTICLES_URI;
		
	private GridViewCursorAdapter adapter;
	private CursorLoader cursorLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		start = System.currentTimeMillis();
		setContentView(R.layout.activity_main);

		new LoadData().execute();
	}

	private void initLoaders() {	
		for (int i = 0; i < DEP_NUMBER; i++) {
			Bundle data = new Bundle();
			data.putInt("DEP_ID", (i+1));
			getLoaderManager().initLoader(i, data, MainActivity.this);
		}	
	}

	public void onDestroy() {
		super.onDestroy();
		destroyLoaders();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		finish = System.currentTimeMillis();
		Toast.makeText(this, "Loading time: " + (finish-start) + " ms", Toast.LENGTH_LONG).show();
	}

	private void destroyLoaders() {
		for (int i = 0; i < DEP_NUMBER; i++) {
			getLoaderManager().destroyLoader(i);
		}	
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		String filter = ArticleTable.DEPARTMENT_ID + "="
				+ args.getInt("DEP_ID");

		cursorLoader = new CursorLoader(this, uri, projection, filter, null,
				null);
		
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d("LOADER " + loader.getId(), "FINISHED");
		createGrid(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	// Dynamic view adding nice idea)))
	private GridView createGrid(Cursor cursor) {
		GridView gv = new GridView(MainActivity.this);
		gv.setPadding(5, 5, 5, 5);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(400,
				LayoutParams.MATCH_PARENT);
		gv.setLayoutParams(params);
		gv.setVerticalSpacing(10);
		LinearLayout ll = (LinearLayout) findViewById(R.id.lineargrid);
		ll.addView(gv);
		gv.setOnItemClickListener(gridClick);

		adapter = new GridViewCursorAdapter(this, cursor, false);
		gv.setAdapter(adapter);

		return gv;
	}

	private OnItemClickListener gridClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ViewHolder holder = (ViewHolder) view.getTag();
			
			int _id = holder.id;
			int depId = holder.depId;

//			Toast.makeText(
//					MainActivity.this,
//					"Header: " + tvHeader.getText().toString() + "\n" + "Id: "
//							+ _id + " Dep Id: " + depId, Toast.LENGTH_LONG).show();
			Bundle bundle = new Bundle();
			bundle.putInt("ID", _id);
			bundle.putInt("DEP_ID", depId);
			Intent intent = new Intent(MainActivity.this, VerticalArticlesActivity.class);
			intent.putExtra("YURI", bundle);
			MainActivity.this.startActivity(intent);
			
		}
	};
	
	private class LoadData extends AsyncTask<String, Void, String> {

		protected String doInBackground(String... urls) {
			// Create object
			DownloadData test1 = new DownloadData();
			DownloadData test2 = new DownloadData();
			try {
				String data1 = test1
						.getInternetData("http://www.hs.fi/rest/apps/1/k/frontpage");
				test1.saveDepartments(data1, MainActivity.this);

				String data2 = test2
						.getInternetData("http://www.hs.fi/rest/apps/1/k/articles/latest");
				test2.JSONtoDB(data2, MainActivity.this);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(String result) {
			initLoaders();
		}
	}
}

package com.swengi.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swengi.db.ArticleTable;
import com.swengi.hs_v1.R;

/**
 * 
 * Cursor adapter used for GridView
 *
 */
public class GridViewCursorAdapter extends CursorAdapter {
	
	private ViewHolder holder;
	private LayoutInflater inflater;
	
	public GridViewCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		
		inflater = LayoutInflater.from(context);
	}
	
	public static class ViewHolder {
		public TextView header;
		public int depId;
		public int id;
	}
	@Override 
	public void bindView(View view, Context context, Cursor cursor) {
		
		holder = (ViewHolder) view.getTag();
		
		int headerIndex = cursor.getColumnIndex(ArticleTable.MAIN_HEADER);
		String header = cursor.getString(headerIndex);
		
		holder.header.setText(header);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View view = inflater.inflate(R.layout.grid_view, parent, false);
		
		int idIndex = cursor.getColumnIndex(ArticleTable.ID);
		int id = cursor.getInt(idIndex);
		int depIdIndex = cursor.getColumnIndex(ArticleTable.DEPARTMENT_ID);
		int depId = cursor.getInt(depIdIndex);
		
		holder = new ViewHolder();
		holder.header = (TextView) view.findViewById(R.id.tv_grid_header);
		holder.id = id;
		holder.depId = depId;

		view.setTag(holder);

		return view;
	}
}

package com.swengi.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

import com.swengi.db.ArticleTable;
import com.swengi.db.SwengiContentProvider;
import com.swengi.hs_v1.R;

public class VerticalArticlesActivity extends Activity implements
LoaderManager.LoaderCallbacks<Cursor> {
	
	private final int LOADER = 0x19;
	
	// Database uri and projection
	private final String[] projection = { ArticleTable.ID,
			ArticleTable.MAIN_HEADER, ArticleTable.DEPARTMENT_ID };
	private final Uri uri = SwengiContentProvider.ARTICLES_URI;

	private Cursor depArtCursor;
	private CursorLoader cursorLoader;
	
	private int id;
	private int depId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_articles);
        
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("YURI");
        
        if (bundle != null) {
        	id = bundle.getInt("ID");
        	depId = bundle.getInt("DEP_ID");
        }
        
		if (savedInstanceState == null) {
			getLoaderManager().initLoader(LOADER, null, this);
		}
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vertical_articles, menu);
        return true;
    }


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String filter = ArticleTable.DEPARTMENT_ID + "="
				+ depId;

		cursorLoader = new CursorLoader(this, uri, null, filter, null,
				null);
		
		createFragment();
		
		return cursorLoader;
	}


	private void createFragment() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		depArtCursor = cursor;	
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		
	}
    
}

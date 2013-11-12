package com.swengi.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.swengi.db.ArticleTable;
import com.swengi.db.DepartmentTable;
import com.swengi.db.SwengiContentProvider;
import com.swengi.ui.MainActivity;

public class DownloadData {
	final String LOG_TAG = "DownloadData";

	public String getInternetData(String url) throws Exception{
		BufferedReader in = null;
		String data = null;
		try{

			/*		setting up all the necessary variables for the http request:
			 *		client and URI with the url (passed from Initial.java) where we download
			 */		
			HttpClient client =new DefaultHttpClient();
			URI website= new URI(url);
			HttpGet request= new HttpGet();

			//		set the uri with the url for the request

			request.setURI(website);

			//		executing the request

			HttpResponse response = client.execute(request);

			/*		getcontent receives the content from the entity opened by
			 *		getentity which is then converted to characters by inputstreamreader which is then buffered
			 */	
			in = new BufferedReader(new InputStreamReader (response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String nline = System.getProperty("line.separator");

			/*		as long as there's a line to read, a line of text and then a line separator
			 *		is appended to the stringbuffer sb
			 */
			while((line=in.readLine()) != null){
				sb.append(line+ nline);

			}
			//		close the bufferedreader

			in.close();

			//		the stringbuffer's data is stored into a variable and then it's returned

			data = sb.toString();
			return data;

			//		in case the stringbuffer didn't close poperly we make sure it does
		}finally{
			if(in != null){
				try{
					in.close();
					return data;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public void JSONtoDB(String data, MainActivity context){
		//FIRST YOU HAVE TO FILL DEPARTMENTS, THEN ARTICLES AND FINALLY IMAGES.
		//OTHERWISE SOME VALUES WILL BE LEFT NULL -> BREAKS FOREIGN KEY CONTRAINT.
		String articleServerId;
		String mainHeader;
		String lead;
		String date;
		String bodyText;
		String departmentId;
		String departmentTitle;
		int i=0;

		/*		a JSONObject is made from the data string we received from getInternetData and
		 * 		put the content of primaryContent and secondaryContent into JSONArrays
		 */

		//Here we do a query to Departments table to fetch the primary key of each department and save it into arraylist, which we use later on.
		Uri uriDep = SwengiContentProvider.DEPARTMENTS_URI;
		Cursor cursor = context.getContentResolver().query(uriDep, null, null, null, null);
		Log.d(LOG_TAG, "Cursor is here: " + cursor.getCount());

		//New arraylists
		ArrayList<String> listOfIds = new ArrayList<String>();
		ArrayList<String> listOfTitles = new ArrayList<String>();

		//If inside cursor a "row"/item(s) exists
		if (cursor.getCount() != 0 && cursor.moveToNext()) {
			//Move from index -1 to 0, since there are item(s) in the exist
			cursor.moveToFirst();

			//Loop through all the items in the cursor
			for(int a = 0; a<cursor.getCount(); a++) {

				//Get the index of wanted column
				int idIndex = cursor.getColumnIndex(DepartmentTable.ID);
				int titleIndex = cursor.getColumnIndex(DepartmentTable.TITLE);

				//Get the string from the wanted index
				String id = cursor.getString(idIndex);
				String title = cursor.getString(titleIndex);

				//Log.d(LOG_TAG, "Id is here: " + id);
				//Log.d(LOG_TAG, "Title is here: " + title);

				//Add it to the arraylists
				listOfIds.add(id);
				listOfTitles.add(title);

				//Move to the next item in the cursor
				cursor.moveToNext();
			}
		}
		cursor.close();

		//CONTINUE AFTER QUERY	
		try{
			//Fetch the data from the wanted url and get data JSONObject, which contains multiple lower levels, such as primaryContent and secondaryContent
			JSONObject fullcontent = (new JSONObject(data)).getJSONObject("data");
			JSONArray primarycontent = fullcontent.optJSONArray("primaryContent");
			JSONArray secondarycontent = fullcontent.optJSONArray("secondaryContent");

			//For each article we have...
			for (i = 0; i < primarycontent.length(); i++) {

				//Introduce valuables and initialize them
				String departmentIndexFromLoop = null;
				ContentValues values = new ContentValues();
				values.clear();		
				/*
				 * a JSONObject for the primary and secondary content of a single article is made
				 * and the data of the article is stored into the objects
				 */
				JSONObject p_array_content_object = primarycontent.getJSONObject(i);
				JSONObject s_array_content_object = secondarycontent.getJSONObject(i);

				//the fields of the article's JSONObjects are stored into variables

				articleServerId = p_array_content_object.optString("id").toString();
				mainHeader = p_array_content_object.optString("mainHeader").toString();
				lead = p_array_content_object.optString("lead").toString(); 
				date = p_array_content_object.optString("date").toString();
				bodyText = s_array_content_object.optString("bodyText").toString();
				departmentTitle = p_array_content_object.optString("departmentTitle").toString();
				departmentId = p_array_content_object.optString("departmentId").toString();

				//Here we loop through our listOfTitles and fetch one title at a time. Inside the loop we check IF one of the items match with the departmentTitle that we fetch from the JSON.
				//Ex. In the list we have every department there is, and we're trying to match it with some article's departmentTitle, so we can insert the proper foreign key into our table.
				int num = 0;
				for (String title : listOfTitles){

					Log.d(LOG_TAG, title + " == " + departmentTitle);

					//If the departmentTitle matches which one of the title items, we save the position of that item in the arraylist, which might be 0, 1, 2 etc.
					//Then we break the loop if we've managed to do it. We also increment num variable at the end, so we're looping through each item in the list.
					if (departmentTitle.equals(title)) {
						departmentIndexFromLoop = listOfIds.get(num);
						Log.d(LOG_TAG, "departmentInsideTheLoop" + departmentIndexFromLoop);
						break;
					} else {
						Log.d(LOG_TAG, "Loop number" + num);
					}
					num++;
				}
				
				// If no match was found in our departments table with departmenTitle, which we fetched from the server JSON. 
				// Then we create a new contentvalues container, fill it up and insert it. Then we "get" the id of the latest insert (row _id)
				if (departmentIndexFromLoop == null) {
					Log.d(LOG_TAG, "departmentId = " + departmentId + " departmentTitle" + departmentTitle);
					ContentValues depValues = new ContentValues();
					depValues.put(DepartmentTable.DEPARTMENT_ID, departmentId);
					depValues.put(DepartmentTable.TITLE, departmentTitle);
					context.getContentResolver().insert(uriDep, depValues);

					int temp = num + 1;
					departmentIndexFromLoop = String.valueOf(temp);
					Log.d(LOG_TAG, "Departmentindexfromloop inside new IF" + departmentIndexFromLoop);
				}

				//the values are stored into the ContentValues object
				values.put(ArticleTable.ARTICLE_ID, articleServerId);
				values.put(ArticleTable.BODY_TEXT, bodyText);
				values.put(ArticleTable.DATE, date);
				values.put(ArticleTable.LEAD, lead);
				values.put(ArticleTable.MAIN_HEADER, mainHeader);
				values.put(ArticleTable.DEPARTMENT_ID, departmentIndexFromLoop);

				Log.d(LOG_TAG, "Next : Uri uri = SwengiDbContentProvider.ARTICLES_CONTENT_URI");

				//		setting the uri needed to add articles to the database
				Uri uri = SwengiContentProvider.ARTICLES_URI;
				Log.d(LOG_TAG, "Uri = " + uri);
				Log.d(LOG_TAG, "Trying to insert first article...");

				// 		the article is added to the database
				Uri result = context.getContentResolver().insert(uri, values);
				Log.d(LOG_TAG, "Result: Uri = " + result);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//This is used to save the departments and we're using hs.fi/frontpage to get the departments.
	public void saveDepartments (String data, MainActivity context) {

		try{
			//Create the JSONObjects, we fetch the object data first and then go to lower levels of data.
			JSONObject fullcontent = (new JSONObject(data)).getJSONObject("data");
			JSONObject fullcontent_config = fullcontent.getJSONObject("config");
			JSONArray departmentGroup = fullcontent_config.getJSONArray("departmentGroups");
			ContentValues values = new ContentValues();

			//We calculate the amount of departments that are available
			int arrSize = departmentGroup.length();
			Log.d(LOG_TAG, "departmentgroup size: " + arrSize);

			//We loop through every department there is and fetch the id and title of it
			for(int i = 0; i < arrSize; ++i) {

				//Even lower level
				JSONObject a = departmentGroup.getJSONObject(i);
				JSONArray b = a.getJSONArray("departments");
				JSONObject c = b.getJSONObject(0);

				//Fetch the id and title from the object
				String id = c.getString("id");
				String departmentTitle = c.getString("title");

				//Save the values into ContentValues container
				values.put(DepartmentTable.DEPARTMENT_ID, id);
				values.put(DepartmentTable.TITLE, departmentTitle);

				//Introduce the URI (table "address"), where we send the values that we gathered above
				Uri uri = SwengiContentProvider.DEPARTMENTS_URI;
				context.getContentResolver().insert(uri, values);
				Log.d(LOG_TAG, "Finished inserting to department");

			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
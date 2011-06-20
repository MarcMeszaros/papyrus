/*******************************************************************************
 * Copyright (c) 2011 - Marc Meszaros
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package ca.marcmeszaros.papyrushunter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import ca.marcmeszaros.papyrus.Papyrus;
import ca.marcmeszaros.papyrus.database.DBHelper;
import ca.marcmeszaros.papyrus.database.TNManager;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.atom.AtomParser;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PapyrusHunter extends Thread {
	
	private static final String TAG = "PapyrusHunter";
	
	// class variables
	private Context context;
	private int libraryID;
	private int quantity;
	private Handler messageHandler;
	private String bar_code;
		
	public PapyrusHunter(Context applicationContext, Handler messageHandler, String bar_code, int libraryID, int quantity) {
		this.context = applicationContext;
		this.messageHandler = messageHandler;
		this.bar_code = bar_code;
		this.libraryID = libraryID;
		this.quantity = quantity;
	}
		
	@Override
	public void run() {
		
		Message msg = new Message();
		
		try {
			// setup the connection and create a buffered reader
			HttpTransport transport = GoogleTransport.create();
		    GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		    
		    String appName = "Papyrus/"+Papyrus.getVersionName(context, this.getClass());
		    Log.i(TAG, "Set application name to: " + appName);
		    headers.setApplicationName(appName);
		    headers.gdataVersion = "2"; 

		    XmlNamespaceDictionary NAMESPACE_DICTIONARY = new XmlNamespaceDictionary();
		    Map<String, String> map = NAMESPACE_DICTIONARY.namespaceAliasToUriMap;
		    map.put("", "http://www.w3.org/2005/Atom");
		    map.put("openSearch", "http://a9.com/-/spec/opensearch/1.1/");
		    map.put("gbs", "http://schemas.google.com/books/2008");
		    map.put("dc", "http://purl.org/dc/terms");
		    map.put("batch", "http://schemas.google.com/gdata/batch");
		    map.put("gd", "http://schemas.google.com/g/2005");
		    
		    AtomParser parser = new AtomParser();
		    parser.namespaceDictionary = NAMESPACE_DICTIONARY;
		    transport.addParser(parser);

		    BookUrl url = BookUrl.root();

		    url.set("q", "isbn:"+bar_code);
		    Log.i(TAG, "Request url: " + url.toString());
			BookFeed feed = BookFeed.executeGet(transport, url);
		    
			Log.i(TAG, "Number of results: " + feed.totalResults);
						
			if(feed.totalResults > 0) {
				for (Entry entry : feed.entries) {
					Log.i("PapyrusHunter", "Got book: " + entry.title + " " + entry.identifiers.get(1));
				}
			
			
				String isbn10 = ((feed.entries.get(0)).identifiers.get(1)).substring(5);
				String isbn13 = ((feed.entries.get(0)).identifiers.get(2)).substring(5);
				String title = feed.entries.get(0).title;
				String authors = (feed.entries.get(0)).dcCreator.get(0);
				String publishers = feed.entries.get(0).dcPublisher;
				String date = feed.entries.get(0).dcDate;
				
				URL thumbnail = new URL(feed.entries.get(0).getThumbnailUrl());
				
				Log.i(TAG, "Start saving book");
				DBHelper helper = new DBHelper(context);
				SQLiteDatabase db = helper.getWritableDatabase();
				
				// create the query
				ContentValues values = new ContentValues();
				values.put(DBHelper.BOOK_FIELD_TITLE, title);
				values.put(DBHelper.BOOK_FIELD_AUTHOR, authors);
				values.put(DBHelper.BOOK_FIELD_ISBN10, isbn10);
				values.put(DBHelper.BOOK_FIELD_ISBN13, isbn13);
				values.put(DBHelper.BOOK_FIELD_PUBLISHER, publishers);
				values.put(DBHelper.BOOK_FIELD_PUBLICATION_DATE, date);
				values.put(DBHelper.BOOK_FIELD_LIBRARY_ID, libraryID);
				values.put(DBHelper.BOOK_FIELD_QUANTITY, quantity);
				
				// insert the book
				db.insert(DBHelper.BOOK_TABLE_NAME, "", values);
				Log.i(TAG, "Saving book complete");
				
				// get the thumbnail and save it
				// check if we got an isbn10 number from query and file exists
				if(isbn10 != null && isbn13.length() == 10){
					TNManager.saveThumbnail(thumbnail, isbn10);
				}
				// check if we got an isbn13 number from query and file exists
				else if(isbn13 != null && isbn13.length() == 13){
					TNManager.saveThumbnail(thumbnail, isbn13);
				}
				Log.i(TAG, "Got thumbnail");
				
				// send message that we saved the book
				msg.what = -1;
				msg.obj = feed.entries.get(0).title;
				
				messageHandler.sendMessage(msg);
			
			} else {
				msg.what = 1; // no book info
				messageHandler.sendMessage(msg); // send message to handler
			}
			
		}
		
		catch (MalformedURLException e) {
			Log.e(TAG, "Malformed URL Exception.");
		}
		catch (IOException e){
			messageHandler.sendEmptyMessage(0);
			Log.e(TAG, "Couldn't connect to the server.");
		}
		
	}
}

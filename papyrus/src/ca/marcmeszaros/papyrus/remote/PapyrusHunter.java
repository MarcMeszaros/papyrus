/**
 * Copyright 2011 Marc Meszaros
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.marcmeszaros.papyrus.remote;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;
import ca.marcmeszaros.papyrus.remote.google.BookFeed;
import ca.marcmeszaros.papyrus.remote.google.BookUrl;
import ca.marcmeszaros.papyrus.remote.google.Entry;
import ca.marcmeszaros.papyrus.tools.Manifest;
import ca.marcmeszaros.papyrus.tools.TNManager;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.http.xml.atom.AtomParser;

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
			// create the book query
			BookUrl url = BookUrl.root();
		    url.set("q", "isbn:"+bar_code);
		    Log.i(TAG, "Request url: " + url.toString());
			
			// setup the connection and create a buffered reader
			Log.d(TAG, "About to open a transport");
			HttpTransport transport = AndroidHttp.newCompatibleTransport();
			Log.d(TAG, "We opened a transport!");
			GoogleHeaders headers = new GoogleHeaders();
		    
			// setup the request headers
		    String appName = "Papyrus/"+Manifest.getVersionName(context);
		    Log.i(TAG, "Set application name to: " + appName);
		    headers.setApplicationName(appName);
		    headers.gdataVersion = "2"; 

		    // define the XML namespaces (from Google Books API)
		    XmlNamespaceDictionary NAMESPACE_DICTIONARY = new XmlNamespaceDictionary();
		    NAMESPACE_DICTIONARY.set("", "http://www.w3.org/2005/Atom");
		    NAMESPACE_DICTIONARY.set("openSearch", "http://a9.com/-/spec/opensearch/1.1/");
		    NAMESPACE_DICTIONARY.set("gbs", "http://schemas.google.com/books/2008");
		    NAMESPACE_DICTIONARY.set("dc", "http://purl.org/dc/terms");
		    NAMESPACE_DICTIONARY.set("batch", "http://schemas.google.com/gdata/batch");
		    NAMESPACE_DICTIONARY.set("gd", "http://schemas.google.com/g/2005");
		    
		    // create parser and add namespaces
		    AtomParser parser = new AtomParser();
		    parser.namespaceDictionary = NAMESPACE_DICTIONARY;
		    
		    // create a request from a factory generated by the transport 
		    HttpRequestFactory factory = transport.createRequestFactory();
		    HttpRequest request = factory.buildGetRequest(url);
		    
		    // add the headers and parser to the request
		    request.headers = headers;
		    request.addParser(parser);
		    
		    // send the request on it's way...
		    BookFeed feed = BookFeed.executeGet(transport, url, request);
		    
			Log.d(TAG, "Number of results: " + feed.totalResults);
			
			// parse some results... if we have some of course...
			if(feed.totalResults > 0) {
				for (Entry entry : feed.entries) {
					Log.i("PapyrusHunter", "Got book: " + entry.title + " " + entry.identifiers.get(1));
				}
			
				// get the first entry
				Entry entry = feed.entries.get(0);
				
				// get the isbn numbers
				String isbn10 = "";
				String isbn13 = "";
				// iterate through the identifiers
				for (String identifier : entry.identifiers) {
					if(identifier.startsWith("ISBN:") && identifier.substring(5).length() == 10) {
						isbn10 = identifier.substring(5);
					} else if(identifier.startsWith("ISBN:") && identifier.substring(5).length() == 13) {
						isbn13 = identifier.substring(5);
					}
				}
				
				// get the title
				String title = entry.title;
				
				// get the authors
				String authors = "";
				for(String author : entry.dcCreator) {
					authors += author+", ";
				}
				// fix the last ', '
				if(authors.length() > 0) {
					authors = authors.substring(0, authors.length()-2);
				}
				
				// get other data
				String publishers = entry.dcPublisher;
				String date = entry.dcDate;
				
				// the thumbnail url
				String rawThumbnailUrl = entry.getThumbnailUrl();
				URL thumbnail = null;
				if (rawThumbnailUrl != null) {
					thumbnail = new URL(rawThumbnailUrl.replace("&amp;", "&"));
					Log.d(TAG, "thumbnail url: "+thumbnail);
				}
				
				Log.i(TAG, "Start saving book");
				// get the local SQL db connection
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
				db.close();
				Log.d(TAG, "Saving book complete");
				
				// get the thumbnail and save it
				// check if we got an isbn10 number from query and file exists
				if(isbn10 != null && isbn10 != "" && isbn13.length() == 10 && thumbnail != null) {
					TNManager.saveThumbnail(thumbnail, isbn10);
					Log.d(TAG, "Got thumbnail");
				}
				// check if we got an isbn13 number from query and file exists
				else if(isbn13 != null && isbn13 != "" && isbn13.length() == 13 && thumbnail != null) {
					TNManager.saveThumbnail(thumbnail, isbn13);
					Log.d(TAG, "Got thumbnail");
				}
				
				// send message that we saved the book
				msg.what = -1;
				msg.obj = title;
				messageHandler.sendMessage(msg);
			
			} else {
				msg.what = 1; // no book info
				messageHandler.sendMessage(msg); // send message to handler
			}
			
		}
		
		catch (MalformedURLException e) {
			messageHandler.sendEmptyMessage(0);
			Log.e(TAG, "Malformed URL Exception.", e);
		}
		catch (IOException e) {
			messageHandler.sendEmptyMessage(0);
			Log.e(TAG, "Couldn't connect to the server.", e);
		}
		
	}
}

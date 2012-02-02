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
package ca.marcmeszaros.papyrus.provider;

import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class BooksContentProvider extends ContentProvider {

	private static final String TAG = "BooksContentProvider";
	
	public static final String AUTHORITY = "ca.marcmeszaros.papyrus.provider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/books");

	// map the DB columns to fields in the content provider
	public static final String TABLE_NAME = DBHelper.BOOK_TABLE_NAME;
	public static final String FIELD_ID = DBHelper.BOOK_FIELD_ID;
	public static final String FIELD_LIBRARY_ID = DBHelper.BOOK_FIELD_LIBRARY_ID;
	public static final String FIELD_ISBN10 = DBHelper.BOOK_FIELD_ISBN10;
	public static final String FIELD_ISBN13 = DBHelper.BOOK_FIELD_ISBN13;
	public static final String FIELD_BAR_CODE = DBHelper.BOOK_FIELD_BAR_CODE;
	public static final String FIELD_TITLE = DBHelper.BOOK_FIELD_TITLE;
	public static final String FIELD_AUTHOR = DBHelper.BOOK_FIELD_AUTHOR;
	public static final String FIELD_EDITION = DBHelper.BOOK_FIELD_EDITION;
	public static final String PUBLICATION_DATE = DBHelper.BOOK_FIELD_PUBLICATION_DATE;
	public static final String FIELD_PUBLISHER = DBHelper.BOOK_FIELD_PUBLISHER;
	public static final String FIELD_PAGES = DBHelper.BOOK_FIELD_PAGES;
	public static final String FIELD_QUANTITY = DBHelper.BOOK_FIELD_QUANTITY;
	
	// uri matching static variables
	private static final int BOOKS = 1;
	private static final int BOOK_ID = 2;
	
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "books", BOOKS);
		uriMatcher.addURI(AUTHORITY, "books/#", BOOK_ID);
	}

	// class variables
	private DBHelper helper;
	private SQLiteDatabase db;

	@Override
	public boolean onCreate() {
		helper = new DBHelper(getContext());

		// return success or failure
		if (helper != null) {
			Log.d(TAG, "content provider created");
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// create an SQL builder object and set the table to books
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DBHelper.BOOK_TABLE_NAME);
		
		// instantiate the objects
		String order = DBHelper.BOOK_FIELD_TITLE;
		Cursor result = null;
		
		// if there is a defined sort order user it, otherwise
		// use the default sort order
		if (sortOrder != null) {
			order = sortOrder;
		}

		// execute the query and return the result
		result = builder.query(helper.getReadableDatabase(), projection, selection, selectionArgs, null, null, order);
		result.setNotificationUri(getContext().getContentResolver(), uri);
		return result;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			// get all books
			case BOOKS:
				return "vnd.android.cursor.dir/vnd.ca.marcmeszaros.papyrus.book";
			// get a specific book
			case BOOK_ID:
				return "vnd.android.cursor.item/vnd.ca.marcmeszaros.papyrus.book";
			// not a valid URI
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// create an SQL builder object and set the table to books
		db = helper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
			case BOOKS:
				// get the new id and close the db
				long id = db.insert(DBHelper.BOOK_TABLE_NAME, null, values);
				db.close();
			
				// build the result uri, notify of data change, and return
				Uri result = ContentUris.withAppendedId(CONTENT_URI, id);
				getContext().getContentResolver().notifyChange(uri, null);
				return result;

			default:
				return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// create an SQL builder object and set the table to books
		db = helper.getWritableDatabase();
		int rowsAffected = 0;
		
		switch (uriMatcher.match(uri)) {
			// delete all books
			case BOOKS:
				// the "1" is required to return the number of rows deleted
				rowsAffected = db.delete(DBHelper.BOOK_TABLE_NAME, "1", null);
				getContext().getContentResolver().notifyChange(uri, null);
				return rowsAffected;

			// delete the book matching the id
			case BOOK_ID:
				// get the book id from the uri and build the query parts
				long id = ContentUris.parseId(uri);
				String whereClause = DBHelper.BOOK_FIELD_ID + " = ?";
				String[] whereArgs = { Long.toString(id) };
				
				// execute the delete and return the number of rows affected
				rowsAffected = db.delete(DBHelper.BOOK_TABLE_NAME, whereClause, whereArgs);
				getContext().getContentResolver().notifyChange(uri, null);
				return rowsAffected;

			// nothing to do
			default:
				return rowsAffected;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// create an SQL builder object and set the table to books
		db = helper.getWritableDatabase();
		int rowsAffected = 0;

		switch (uriMatcher.match(uri)) {
			// update all books matching where clause
			case BOOKS:
				rowsAffected = db.update(DBHelper.BOOK_TABLE_NAME, values, selection, selectionArgs);
				getContext().getContentResolver().notifyChange(uri, null);
				return rowsAffected;

			// delete the book matching the id
			case BOOK_ID:
				// get the book id from the uri and build the query parts
				long id = ContentUris.parseId(uri);
				String whereClause = DBHelper.BOOK_FIELD_ID + " = ?";
				String[] whereArgs = { Long.toString(id) };
					
				// execute the delete and return the number of rows affected
				rowsAffected = db.update(DBHelper.BOOK_TABLE_NAME, values, whereClause, whereArgs);
				getContext().getContentResolver().notifyChange(uri, null);
				return rowsAffected;

			// nothing to do
			default:
				return rowsAffected;
		}
	}

}

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

import java.io.File;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import ca.marcmeszaros.papyrus.BuildConfig;

public class PapyrusContentProvider extends ContentProvider {

	private static final String TAG = "PapyrusContentProvider";

	public static final String AUTHORITY = (BuildConfig.DEBUG) ?
            "ca.marcmeszaros.papyrus.debug.provider.PapyrusContentProvider" :
            "ca.marcmeszaros.papyrus.provider.PapyrusContentProvider";


	/**
	 * A nested class defining various attributes of the "books" table.
	 */
	public static final class Books {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/books");

		// map the DB columns to fields in the content provider
		public static final String TABLE_NAME = "books";
		public static final String FIELD_ID = BaseColumns._ID;
		public static final String FIELD_LIBRARY_ID = "library_ID";
		public static final String FIELD_ISBN10 = "ISBN10";
		public static final String FIELD_ISBN13 = "ISBN13";
		public static final String FIELD_BAR_CODE = "bar_code";
		public static final String FIELD_TITLE = "title";
		public static final String FIELD_AUTHOR = "author";
		public static final String FIELD_EDITION = "edition";
		public static final String FIELD_PUBLICATION_DATE = "publication_date";
		public static final String FIELD_PUBLISHER = "publisher";
		public static final String FIELD_PAGES = "pages";
		public static final String FIELD_QUANTITY = "quantity";
	}

	/**
	 * A nested class defining various attributes of the "loans" table.
	 */
	public static final class Loans {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/loans");

		// map the DB columns to fields in the content provider
		public static final String TABLE_NAME = "loans";
		public static final String FIELD_ID = BaseColumns._ID;
		public static final String FIELD_BOOK_ID = "book_ID";
		public static final String FIELD_CONTACT_ID = "contact_ID";
		public static final String FIELD_LEND_DATE = "lend_date";
		public static final String FIELD_DUE_DATE = "due_date";
	}

	/**
	 * A nested class defining various attributes of the "libraries" table.
	 */
	public static final class Libraries {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/libraries");

		// map the DB columns to fields in the content provider
		public static final String TABLE_NAME = "libraries";
		public static final String FIELD_ID = BaseColumns._ID;
		public static final String FIELD_ADDRESS = "address";
		public static final String FIELD_GEO_LONGITUDE = "geo_tag_longitude";
		public static final String FIELD_GEO_LATITUDE = "geo_tag_latitude";
		public static final String FIELD_NAME = "name";
	}

	// uri matching static variables
	private static final int BOOKS = 1001;
	private static final int BOOK_ID = 1002;

	private static final int LOANS = 2001;
	private static final int LOAN_ID = 2002;
	private static final int LOAN_DETAILS = 2003;
	private static final int LOANS_DETAILS = 2004;

	private static final int LIBRARIES = 3001;
	private static final int LIBRARY_ID = 3002;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "books", BOOKS);
		uriMatcher.addURI(AUTHORITY, "books/#", BOOK_ID);

		uriMatcher.addURI(AUTHORITY, "loans", LOANS);
		uriMatcher.addURI(AUTHORITY, "loans/#", LOAN_ID);
		uriMatcher.addURI(AUTHORITY, "loans/#/details", LOAN_DETAILS);
		uriMatcher.addURI(AUTHORITY, "loans/details", LOANS_DETAILS);

		uriMatcher.addURI(AUTHORITY, "libraries", LIBRARIES);
		uriMatcher.addURI(AUTHORITY, "libraries/#", LIBRARY_ID);
	}

	// class variables
	private DBHelper helper;

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
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case BOOKS:
			return "vnd.android.cursor.dir/vnd.ca.marcmeszaros.papyrus.book";
		case BOOK_ID:
			return "vnd.android.cursor.item/vnd.ca.marcmeszaros.papyrus.book";
		case LOANS:
			return "vnd.android.cursor.dir/vnd.ca.marcmeszaros.papyrus.loan";
		case LOAN_ID:
			return "vnd.android.cursor.item/vnd.ca.marcmeszaros.papyrus.loan";
		case LOANS_DETAILS:
			return "vnd.android.cursor.dir/vnd.ca.marcmeszaros.papyrus.loan.details";
		case LOAN_DETAILS:
			return "vnd.android.cursor.item/vnd.ca.marcmeszaros.papyrus.loan.details";
		case LIBRARIES:
			return "vnd.android.cursor.dir/vnd.ca.marcmeszaros.papyrus.library";
		case LIBRARY_ID:
			return "vnd.android.cursor.item/vnd.ca.marcmeszaros.papyrus.library";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// create an SQL builder object
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

		// set/initiate some objects
		Cursor result = null;
		String order = null;
		String[] columns = null;

		// create the appropriate query based on the Uri
		switch (uriMatcher.match(uri)) {
		// handle the case for all books
		case BOOKS:
			// if the sort order is defined use it, or set a default order
			order = (sortOrder != null) ? sortOrder : Books.FIELD_TITLE;
			builder.setTables(Books.TABLE_NAME);
			columns = (projection != null) ? projection : null;
			break;
		// handle the case for a specific book id
		case BOOK_ID:
			// if the sort order is defined use it, or set a default order
			order = (sortOrder != null) ? sortOrder : Books.FIELD_TITLE;
			builder.setTables(Books.TABLE_NAME);
			columns = (projection != null) ? projection : null;

			// build the query with the specified id
			selection = Books.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
			break;

		// handle the case for all loans
		case LOANS:
			// if the sort order is defined use it, or set a default order
			order = (sortOrder != null) ? sortOrder : null;
			builder.setTables(Loans.TABLE_NAME);
			columns = (projection != null) ? projection : null;
			break;
		// handle the case for a specific loan id
		case LOAN_ID:
			// if the sort order is defined use it, or set a default order
			order = (sortOrder != null) ? sortOrder : null;
			builder.setTables(Loans.TABLE_NAME);
			columns = (projection != null) ? projection : null;

			// build the query with the specified id
			selection = Loans.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
			break;

		// handle the case for loan details
		case LOANS_DETAILS:
			// if the sort order is defined use it, or set a default order
			order = (sortOrder != null) ? sortOrder : null;
			builder.setTables(Loans.TABLE_NAME + ", " + Books.TABLE_NAME);
			columns = (projection != null) ? projection : new String[] {
				Loans.TABLE_NAME + "." + Loans.FIELD_ID,
				Loans.FIELD_BOOK_ID,
				Loans.FIELD_CONTACT_ID,
				Loans.FIELD_LEND_DATE,
				Loans.FIELD_DUE_DATE,
				Books.FIELD_ISBN10,
				Books.FIELD_ISBN13,
				Books.FIELD_TITLE,
				Books.FIELD_AUTHOR
			};

			// build the query
			selection = Loans.TABLE_NAME + "." + Loans.FIELD_BOOK_ID + " = " + Books.TABLE_NAME + "." + Books.FIELD_ID;
			selectionArgs = null;

			// we set the uri to be more generic for the notification URI
			// so that a modification of a loan will tell loan details to
			// update itself
			uri = Loans.CONTENT_URI;
			break;

		// handle the case for loan details
		case LOAN_DETAILS:
			// if the sort order is defined use it, or set a default order
			order = (sortOrder != null) ? sortOrder : null;
			builder.setTables(Loans.TABLE_NAME + ", " + Books.TABLE_NAME);
			columns = (projection != null) ? projection : new String[] {
				Loans.TABLE_NAME + "." + Loans.FIELD_ID,
				Loans.FIELD_BOOK_ID,
				Loans.FIELD_CONTACT_ID,
				Loans.FIELD_LEND_DATE,
				Loans.FIELD_DUE_DATE,
				Books.FIELD_ISBN10,
				Books.FIELD_ISBN13,
				Books.FIELD_TITLE,
				Books.FIELD_AUTHOR
			};

			List<String> segments = uri.getPathSegments();
			String id = segments.get(segments.size() - 2);

			// build the query
			selection = Loans.TABLE_NAME + "." + Loans.FIELD_BOOK_ID + " = " + Books.TABLE_NAME + "." + Books.FIELD_ID
					+ " AND " + Loans.TABLE_NAME + "." + Loans.FIELD_ID + " = ?";
			selectionArgs = new String[] { id };

			// we set the uri to be more generic for the notification URI
			// so that a modification of a loan will tell loan details to
			// update itself
			uri = Uri.withAppendedPath(Loans.CONTENT_URI, id);
			break;

		// handle the case for all libraries
		case LIBRARIES:
			// if the sort order is defined use it, or set a default order
			order = (sortOrder != null) ? sortOrder : Libraries.FIELD_NAME;
			builder.setTables(Libraries.TABLE_NAME);
			columns = (projection != null) ? projection : null;
			break;
		// handle the case for a specific library id
		case LIBRARY_ID:
			// if the sort order is defined use it, or set a default order
			order = (sortOrder != null) ? sortOrder : Libraries.FIELD_NAME;
			builder.setTables(Libraries.TABLE_NAME);
			columns = (projection != null) ? projection : null;

			// build the query with the specified id
			selection = Loans.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
			break;

		default:
			break;
		}

		// notify of data change and return the result
		result = builder.query(helper.getReadableDatabase(), columns, selection, selectionArgs, null, null, order);
		result.setNotificationUri(getContext().getContentResolver(), uri);
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// create an SQL object
		SQLiteDatabase db = helper.getWritableDatabase();

		// set/initiate some objects
		Uri result = null;

		// insert the new row for each respective table
		switch (uriMatcher.match(uri)) {
		case BOOKS:
			result = ContentUris.withAppendedId(Books.CONTENT_URI, db.insert(Books.TABLE_NAME, null, values));
			break;
		case LOANS:
			result = ContentUris.withAppendedId(Loans.CONTENT_URI, db.insert(Loans.TABLE_NAME, null, values));
			break;
		case LIBRARIES:
			result = ContentUris.withAppendedId(Libraries.CONTENT_URI, db.insert(Libraries.TABLE_NAME, null, values));
			break;

		// if we failed close the db and return
		default:
			return null;
		}

		// we successfully inserted the row, close the db, notify of change and return
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// create an SQL object and initialise some objects
		SQLiteDatabase db = helper.getWritableDatabase();
		int rowsAffected = 0;
		String table = null;

		switch (uriMatcher.match(uri)) {
		// delete all books
		case BOOKS:
			table = Books.TABLE_NAME;
			break;

		// delete the book matching the id
		case BOOK_ID:
			// get the id from the uri and build the query parts
			table = Books.TABLE_NAME;
			selection = Books.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
			break;

		// delete all loans
		case LOANS:
			table = Loans.TABLE_NAME;
			break;

		// delete the loan matching the id
		case LOAN_ID:
			// get the id from the uri and build the query parts
			table = Loans.TABLE_NAME;
			selection = Loans.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
			break;

		// delete all libraries
		case LIBRARIES:
			table = Libraries.TABLE_NAME;
			break;

		// delete the library matching the id
		case LIBRARY_ID:
			// get the id from the uri and build the query parts
			table = Libraries.TABLE_NAME;
			selection = Libraries.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
			break;

		// we encountered and error, close and return
		default:
			return rowsAffected;
		}

		// the "1" is required to return the number of rows deleted when deleting all records
		if (TextUtils.isEmpty(selection)) {
			selection = "1";
			selectionArgs = null;
		}

		// we successfully deleted, close the db, notify of change and return
		rowsAffected = db.delete(table, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// create an SQL object and initialise some objects
		SQLiteDatabase db = helper.getWritableDatabase();
		int rowsAffected = 0;
		String table = null;

		switch (uriMatcher.match(uri)) {
		// update all books
		case BOOKS:
			table = Books.TABLE_NAME;
			break;

		// update the matching id
		case BOOK_ID:
			table = Books.TABLE_NAME;
			selection = Books.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
			break;

		// update all loans
		case LOANS:
			table = Loans.TABLE_NAME;
			getContext().getContentResolver().notifyChange(uri, null);
			break;

		// update the matching id
		case LOAN_ID:
			table = Loans.TABLE_NAME;
			selection = Loans.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
			getContext().getContentResolver().notifyChange(uri, null);
			break;

		// update all libraries
		case LIBRARIES:
			table = Libraries.TABLE_NAME;
			break;

		// update the matching id
		case LIBRARY_ID:
			table = Libraries.TABLE_NAME;
			selection = Libraries.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
			break;

		// an error occurred, close the db and return
		default:
			return rowsAffected;
		}

		// we successfully updated, close the db, notify of change and return
		rowsAffected = db.update(table, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}
	
	/**
	 * Nested class for managing the SQL database.
	 */
	private class DBHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 2;

		public DBHelper(Context context) {
			super(context, "papyrus.db", null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// create the tables
			createBookTable(db);
			createLibraryTable(db);
			createLoanTable(db);
		}

		/**
		 * Creates the book table in the database.
		 */
		private void createBookTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + Books.TABLE_NAME + " ("
					+ Books.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ Books.FIELD_LIBRARY_ID + " INTEGER,"
					+ Books.FIELD_ISBN10 + " TEXT(10),"
					+ Books.FIELD_ISBN13 + " TEXT(13),"
					+ Books.FIELD_BAR_CODE + " TEXT(13),"
					+ Books.FIELD_TITLE + " TEXT(255),"
					+ Books.FIELD_AUTHOR + " TEXT(255),"
					+ Books.FIELD_EDITION + " INTEGER(2),"
					+ Books.FIELD_PUBLICATION_DATE + " TEXT(10),"
					+ Books.FIELD_PUBLISHER + " TEXT(255),"
					+ Books.FIELD_PAGES + " INTEGER(5),"
					+ Books.FIELD_QUANTITY + " INTEGER(2),"
					+ "FOREIGN KEY(" + Books.FIELD_LIBRARY_ID + ") REFERENCES " + Libraries.TABLE_NAME + "(" + Libraries.FIELD_ID + ")"
					+ ");");
		}

		/**
		 * Creates the library table in the database.
		 */
		private void createLibraryTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + Libraries.TABLE_NAME + " ("
					+ Libraries.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ Libraries.FIELD_ADDRESS + " TEXT(255)," // not used yet
					+ Libraries.FIELD_GEO_LONGITUDE + " REAL(15)," // not used yet
					+ Libraries.FIELD_GEO_LATITUDE + " REAL(15)," // not used yet
					+ Libraries.FIELD_NAME + " TEXT(255)"
					+ ");");
		}

		/**
		 * Creates the Loan table in the database
		 */
		private void createLoanTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + Loans.TABLE_NAME + " ("
					+ Loans.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ Loans.FIELD_BOOK_ID + " INTEGER,"
					+ Loans.FIELD_CONTACT_ID + " INTEGER,"
					+ Loans.FIELD_LEND_DATE + " INTEGER,"
					+ Loans.FIELD_DUE_DATE + " INTEGER,"
					+ "FOREIGN KEY(" + Loans.FIELD_BOOK_ID + ") REFERENCES " + Books.TABLE_NAME + "(" + Books.FIELD_ID + ")"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// move the thumbnails to a new location on the SD card based on
			// API documentation recommendations
			if (oldVersion == 1) {
				// define some paths
				String oldPath = "/Papyrus/";
				String newPath = "/Android/data/ca.marcmeszaros.papyrus/files/";

				// create the new file folder if required
				new File(Environment.getExternalStorageDirectory(), newPath).mkdirs();

				String[] fileList = new File(Environment.getExternalStorageDirectory(), oldPath).list();
				for (String file : fileList) {
					File newFile = new File(Environment.getExternalStorageDirectory(), newPath + file);
					File oldFile = new File(Environment.getExternalStorageDirectory(), oldPath + file);
					oldFile.renameTo(newFile);
				}

				// delete the old directory and move to next migration
				new File(Environment.getExternalStorageDirectory(), oldPath).delete();
				oldVersion++;
			}
		}

	}

}

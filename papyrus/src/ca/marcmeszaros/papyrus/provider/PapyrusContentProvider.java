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

import java.util.List;

import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class PapyrusContentProvider extends ContentProvider {

	private static final String TAG = "PapyrusContentProvider";

	public static final String AUTHORITY = "ca.marcmeszaros.papyrus.provider.PapyrusContentProvider";

	/**
	 * A nested class defining various attributes of the "books" table.
	 */
	public static final class Books {
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
		public static final String FIELD_PUBLICATION_DATE = DBHelper.BOOK_FIELD_PUBLICATION_DATE;
		public static final String FIELD_PUBLISHER = DBHelper.BOOK_FIELD_PUBLISHER;
		public static final String FIELD_PAGES = DBHelper.BOOK_FIELD_PAGES;
		public static final String FIELD_QUANTITY = DBHelper.BOOK_FIELD_QUANTITY;
	}

	/**
	 * A nested class defining various attributes of the "loans" table.
	 */
	public static final class Loans {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/loans");

		// map the DB columns to fields in the content provider
		public static final String TABLE_NAME = DBHelper.LOAN_TABLE_NAME;
		public static final String FIELD_ID = DBHelper.LOAN_FIELD_ID;
		public static final String FIELD_BOOK_ID = DBHelper.LOAN_FIELD_BOOK_ID;
		public static final String FIELD_CONTACT_ID = DBHelper.LOAN_FIELD_CONTACT_ID;
		public static final String FIELD_LEND_DATE = DBHelper.LOAN_FIELD_LEND_DATE;
		public static final String FIELD_DUE_DATE = DBHelper.LOAN_FIELD_DUE_DATE;
	}

	/**
	 * A nested class defining various attributes of the "libraries" table.
	 */
	public static final class Libraries {
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/libraries");

		// map the DB columns to fields in the content provider
		public static final String TABLE_NAME = DBHelper.LIBRARY_TABLE_NAME;
		public static final String FIELD_ID = DBHelper.LIBRARY_FIELD_ID;
		public static final String FIELD_ADDRESS = DBHelper.LIBRARY_FIELD_ADDRESS;
		public static final String FIELD_GEO_LONGITUDE = DBHelper.LIBRARY_FIELD_GEO_LONGITUDE;
		public static final String FIELD_GEO_LATITUDE = DBHelper.LIBRARY_FIELD_GEO_LATITUDE;
		public static final String FIELD_NAME = DBHelper.LIBRARY_FIELD_NAME;
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
			return "vnd.android.cursor.dir/vnd.ca.marcmeszaros.papyrus.loan";
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

			// build the query
			selection = Loans.TABLE_NAME + "." + Loans.FIELD_BOOK_ID + " = " + Books.TABLE_NAME + "." + Books.FIELD_ID
					+ " AND " + Loans.TABLE_NAME + "." + Loans.FIELD_ID + " = ?";
			selectionArgs = new String[] { segments.get(segments.size() - 2) };
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
			break;

		// update the matching id
		case LOAN_ID:
			table = Loans.TABLE_NAME;
			selection = Loans.FIELD_ID + " = ?";
			selectionArgs = new String[] { Long.toString(ContentUris.parseId(uri)) };
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

}

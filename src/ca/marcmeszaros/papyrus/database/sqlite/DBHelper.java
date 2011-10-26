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

package ca.marcmeszaros.papyrus.database.sqlite;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	// database static variables
	public static final String BOOK_TABLE_NAME = "books";
	public static final String LIBRARY_TABLE_NAME = "libraries";
	public static final String CONTACT_TABLE_NAME = "contacts";
	public static final String LOAN_TABLE_NAME = "loans";

	// database column names static variables
	public static final String BOOK_FIELD_ID = BaseColumns._ID;
	public static final String BOOK_FIELD_LIBRARY_ID = "library_ID";
	public static final String BOOK_FIELD_ISBN10 = "ISBN10";
	public static final String BOOK_FIELD_ISBN13 = "ISBN13";
	public static final String BOOK_FIELD_BAR_CODE = "bar_code";
	public static final String BOOK_FIELD_TITLE = "title";
	public static final String BOOK_FIELD_AUTHOR = "author";
	public static final String BOOK_FIELD_EDITION = "edition";
	public static final String BOOK_FIELD_PUBLICATION_DATE = "publication_date";
	public static final String BOOK_FIELD_PUBLISHER = "publisher";
	public static final String BOOK_FIELD_PAGES = "pages";
	public static final String BOOK_FIELD_QUANTITY = "quantity";
		
	public static final String LIBRARY_FIELD_ID = BaseColumns._ID;
	public static final String LIBRARY_FIELD_ADDRESS = "address";
	public static final String LIBRARY_FIELD_GEO_LONGITUDE = "geo_tag_longitude";
	public static final String LIBRARY_FIELD_GEO_LATITUDE = "geo_tag_latitude";
	public static final String LIBRARY_FIELD_NAME = "name";
		
	public static final String LOAN_FIELD_ID = BaseColumns._ID;
	public static final String LOAN_FIELD_BOOK_ID = "book_ID";
	public static final String LOAN_FIELD_CONTACT_ID = "contact_ID";
	public static final String LOAN_FIELD_LEND_DATE = "lend_date";
	public static final String LOAN_FIELD_DUE_DATE = "due_date";

	// SD card static variables
	public static final String PAPYRUS_SDCARD_NAME = "Papyrus";
	public static String PAPYRUS_SDCARD_STATE;

	// class variables
	public static File PAPYRUS_SDCARD_ROOT = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), PAPYRUS_SDCARD_NAME);

	public DBHelper(Context context) {
		super(context, "papyrus.db", null, DATABASE_VERSION);
		PAPYRUS_SDCARD_STATE = Environment.getExternalStorageState();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create the tables
		createBookTable(db);
		createLibraryTable(db);
		createLoanTable(db);
	}

	/*
	 * method: createBookTable
	 * 
	 * description: Creates the Book table in the database
	 */
	private void createBookTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + BOOK_TABLE_NAME + " ("
				+ BOOK_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ BOOK_FIELD_LIBRARY_ID + " INTEGER," 
				+ BOOK_FIELD_ISBN10 + " TEXT(10),"
				+ BOOK_FIELD_ISBN13 + " TEXT(13)," 
				+ BOOK_FIELD_BAR_CODE + " TEXT(13),"
				+ BOOK_FIELD_TITLE + " TEXT(255)," 
				+ BOOK_FIELD_AUTHOR + " TEXT(255),"
				+ BOOK_FIELD_EDITION + " INTEGER(2)," 
				+ BOOK_FIELD_PUBLICATION_DATE + " TEXT(10),"
				+ BOOK_FIELD_PUBLISHER + " TEXT(255)," 
				+ BOOK_FIELD_PAGES + " INTEGER(5),"
				+ BOOK_FIELD_QUANTITY + " INTEGER(2),"
				+ "FOREIGN KEY("+BOOK_FIELD_LIBRARY_ID+") REFERENCES libaries("+LIBRARY_FIELD_ID+")"
				+ ");");
	}

	/*
	 * method: createLibraryTable
	 * 
	 * description: Creates the Library table in the database
	 */
	private void createLibraryTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + LIBRARY_TABLE_NAME + " ("
				+ LIBRARY_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ LIBRARY_FIELD_ADDRESS + " TEXT(255)," 
				+ LIBRARY_FIELD_GEO_LONGITUDE + " REAL(15),"
				+ LIBRARY_FIELD_GEO_LATITUDE + " REAL(15)," 
				+ LIBRARY_FIELD_NAME + " TEXT(255)" 
				+ ");");
	}

	/*
	 * method: createLoanTable
	 * 
	 * description: Creates the Loan table in the database
	 */
	private void createLoanTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + LOAN_TABLE_NAME + " ("
				+ LOAN_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ LOAN_FIELD_BOOK_ID + " INTEGER,"
				+ LOAN_FIELD_CONTACT_ID + " INTEGER,"
				+ LOAN_FIELD_LEND_DATE + " INTEGER,"
				+ LOAN_FIELD_DUE_DATE + " INTEGER,"
				+ "FOREIGN KEY("+LOAN_FIELD_BOOK_ID+") REFERENCES books("+BOOK_FIELD_ID+")"
				+ ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

}

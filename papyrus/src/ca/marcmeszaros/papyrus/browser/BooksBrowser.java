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
package ca.marcmeszaros.papyrus.browser;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import ca.marcmeszaros.papyrus.AlarmReceiver;
import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.Settings;
import ca.marcmeszaros.papyrus.database.AddBook;
import ca.marcmeszaros.papyrus.database.AddLibrary;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.database.Loan;
import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;

public class BooksBrowser extends ListActivity implements
		OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener,
		DialogInterface.OnClickListener, OnDateSetListener {

	private static final String TAG = "BooksBrowser";
	
	// class variables
	private long selectedBookID;

	private int mYear;
	private int mMonth;
	private int mDay;

	static final int DATE_DIALOG_ID = 0;

	private Intent loanData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_books_browser);

		// set listeners for list clicks and long clicks to this activity
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);

		// create an instance of the db helper class
		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase db = helper.getWritableDatabase();

		// run a query on the DB and get a Cursor (aka result)
		Cursor result = db.query(DBHelper.BOOK_TABLE_NAME, null, null, null, null, null, DBHelper.BOOK_FIELD_TITLE);
		startManagingCursor(result);

		// create our custom adapter with our result and
		// set the adapter to the ListView to display the books
		setListAdapter(new BookAdapter(this, result));

		// get the library spinner
		Spinner spinner = (Spinner) findViewById(R.id.BooksBrowser_spinner_library);

		// get all the libraries
		Cursor library = db.query(DBHelper.LIBRARY_TABLE_NAME, null, null,
			null, null, null, DBHelper.LIBRARY_FIELD_NAME);
		startManagingCursor(library);

		// specify what fields to map to what views
		String[] from = { DBHelper.LIBRARY_FIELD_NAME };
		int[] to = { android.R.id.text1 };

		// create a cursor adapter and set it to the list
		SimpleCursorAdapter adp = new SimpleCursorAdapter(this,
			android.R.layout.simple_spinner_item, library, from, to);
		adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adp);
		spinner.setOnItemSelectedListener(this);
	}

	/**
	 * Handles a Click from an item in the list.
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		// set the item id to a class variable
		this.selectedBookID = id;

		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase db = helper.getReadableDatabase();

		String[] columns = { DBHelper.BOOK_FIELD_ISBN10,
			DBHelper.BOOK_FIELD_ISBN13, DBHelper.BOOK_FIELD_TITLE,
			DBHelper.BOOK_FIELD_AUTHOR, DBHelper.BOOK_FIELD_PUBLISHER,
			DBHelper.BOOK_FIELD_QUANTITY, DBHelper.BOOK_FIELD_ID,
			DBHelper.BOOK_FIELD_LIBRARY_ID };

		// delete the entry in the database
		Cursor bookCursor = db.query(DBHelper.BOOK_TABLE_NAME, columns,
			DBHelper.BOOK_FIELD_ID + "=" + selectedBookID, null, null,
			null, null);
		startManagingCursor(bookCursor);

		bookCursor.moveToFirst();

		Book book = new Book(bookCursor.getString(0), bookCursor.getString(1),
			bookCursor.getString(2), bookCursor.getString(3));
		book.setPublisher(bookCursor.getString(4));
		book.setQuantity(bookCursor.getInt(5));
		book.setBookID(bookCursor.getInt(6));
		book.setLibraryID(bookCursor.getInt(7));

		Intent intent = new Intent(this, BookDetails.class);

		intent.putExtra("book", book);
		db.close();

		startActivity(intent);
	}

	/**
	 * Handles a LongClick from an item in the list (create a dialog).
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			final int position, final long id) {
		// set the item id to a class variable
		this.selectedBookID = id;

		// setup the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.BooksBrowser_LongClickDialog_title));

		// create the dialog items
		final CharSequence[] items = {
			// getString(R.string.BooksBrowser_LongClickDialog_edit),
			getString(R.string.BooksBrowser_LongClickDialog_delete),
			getString(R.string.BooksBrowser_LongClickDialog_lendTo) };

		// set the items and the click listener
		builder.setItems(items, this);

		// create the dialog box and show it
		AlertDialog alert = builder.create();
		alert.show();

		return true;
	}

	/**
	 * Handles a click event from the LongClickDialog.
	 */
	@Override
	public void onClick(DialogInterface dialog, int position) {
		switch (position) {
		// edit
		// case 0:
		// Toast.makeText(getApplicationContext(),
		// "Feature not implemented yet.", Toast.LENGTH_SHORT).show();
		// break;

		// delete
		case 0:
			// create an instance of the db helper class
			DBHelper helper = new DBHelper(getApplicationContext());
			SQLiteDatabase db = helper.getWritableDatabase();

			// delete the entry in the database
			db.delete(DBHelper.BOOK_TABLE_NAME, DBHelper.BOOK_FIELD_ID + "=" + selectedBookID, null);
			db.close();

			// requery the database
			((BookAdapter) getListAdapter()).getCursor().requery();

			// tell the list we have new data
			((BookAdapter) getListAdapter()).notifyDataSetChanged();

			Toast.makeText(getApplicationContext(),
					getString(R.string.BooksBrowser_toast_bookDeleted),
					Toast.LENGTH_SHORT).show();
			break;
		// lend book to someone
		case 1:
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, 1001);
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			// LOAN A BOOK
			case 1001:

				// there are sufficient copies of the book to lend
				if (canLoanBook()) {
					loanData = data;

					// set default due date
					final Calendar c = Calendar.getInstance();
					c.setTimeInMillis(System.currentTimeMillis()+ (1000 * 60 * 60 * 24 * 14));
					mYear = c.get(Calendar.YEAR);
					mMonth = c.get(Calendar.MONTH);
					mDay = c.get(Calendar.DAY_OF_MONTH);

					// Launch Date Picker Box
					showDialog(DATE_DIALOG_ID);
				}
				// there are no more copies left in the library
				else {
					Toast.makeText(this, getString(R.string.BooksBrowser_toast_allCopiesLentOut), Toast.LENGTH_LONG).show();
				}

				break;
			}

		} else {
			// gracefully handle failure
			// Log.w(DEBUG_TAG, "resultWarning: activity result not ok");
		}
	}

	/**
	 * Creates the menu when the "menu" button is pressed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.books_browser, menu);
		return true;
	}

	/**
	 * Handles the event when an option is selected from the option menu.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.BooksBrowser_menu_addBook:
			SQLiteDatabase db = new DBHelper(getApplicationContext())
					.getReadableDatabase();
			Cursor result = db.query(DBHelper.LIBRARY_TABLE_NAME, null, null,
					null, null, null, null, null);
			startManagingCursor(result);
			if (result.getCount() > 0) {
				startActivity(new Intent(this, AddBook.class));
			} else {
				startActivity(new Intent(this, AddLibrary.class));
			}
			db.close();
			break;
		case R.id.BooksBrowser_Settings_menu:
			startActivity(new Intent(this, Settings.class));		
			break;
		}
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long id) {
		// create an instance of the db helper class
		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase db = helper.getReadableDatabase();

		Log.i(TAG, "Item select ID: " + id);

		String selection = DBHelper.BOOK_TABLE_NAME + "."
				+ DBHelper.BOOK_FIELD_LIBRARY_ID + "=" + id;

		// run a query on the DB and get a Cursor (aka result)
		Cursor result = db.query(DBHelper.BOOK_TABLE_NAME, null, selection,
				null, null, null, DBHelper.BOOK_FIELD_TITLE);
		startManagingCursor(result);

		setListAdapter(new BookAdapter(this, result));

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * Date Picking
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		}
		return null;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		// TODO Auto-generated method stub
	}

	/**
	 * the callback received when the user "sets" the date in the dialog
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			loanBook();
		}
	};

	/**
	 * Executes the query to loan out the book
	 */
	private void loanBook() {
		// set the due date
		Calendar c = Calendar.getInstance();
		c.set(mYear, mMonth, mDay);

		// gets the uri path to the user selected
		Uri user = loanData.getData();

		// gets the user id
		String id = user.getLastPathSegment();

		// get a reference to the database
		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase db = helper.getWritableDatabase();
		
		// prepare the query
		ContentValues values = new ContentValues();
		values.put(DBHelper.LOAN_FIELD_BOOK_ID, selectedBookID);
		values.put(DBHelper.LOAN_FIELD_CONTACT_ID, id);
		values.put(DBHelper.LOAN_FIELD_LEND_DATE, System.currentTimeMillis());
		values.put(DBHelper.LOAN_FIELD_DUE_DATE, c.getTimeInMillis());

		// insert the entry in the database
		db.insert(DBHelper.LOAN_TABLE_NAME, "", values);
		
		// loan the new id
		String tables = DBHelper.LOAN_TABLE_NAME;
		String selection = DBHelper.LOAN_TABLE_NAME + "."
				+ DBHelper.LOAN_FIELD_BOOK_ID + " = " + selectedBookID
				+ " AND " +
				DBHelper.LOAN_TABLE_NAME + "." + DBHelper.LOAN_FIELD_CONTACT_ID + " = " + id;
		String[] columns = {
			DBHelper.LOAN_FIELD_ID
		};
		Cursor cursor = db.query(tables, columns, selection, null, null, null, DBHelper.LOAN_FIELD_ID+" DESC");
		cursor.moveToFirst();
		int loanID = cursor.getInt(0);
		cursor.close();
		
		// close the db
		db.close();
		
		//Book book = new Book(isbn10, title, author);
		Loan loan = new Loan(
				loanID,
				values.getAsInteger(DBHelper.LOAN_FIELD_BOOK_ID),
				values.getAsInteger(DBHelper.LOAN_FIELD_CONTACT_ID),
				values.getAsLong(DBHelper.LOAN_FIELD_LEND_DATE), 
				values.getAsLong(DBHelper.LOAN_FIELD_DUE_DATE)
		);

		// get an alarm manager
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // create the intent for the alarm
        Intent intent = new Intent(this, AlarmReceiver.class);
        
        // put the loan object into the alarm receiver
        intent.putExtra("loan", loan);
        
        // create the pendingIntent to run when the alarm goes off and be handled by a receiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        // set the repeating alarm
        am.set(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);
		
		Toast.makeText(this, getString(R.string.BooksBrowser_toast_loanSuccessful), Toast.LENGTH_LONG).show();
	}

	/**
	 * Checks that there are enough books to loan out this copy
	 */
	public boolean canLoanBook() {

		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase db = helper.getReadableDatabase();

		// Get the quantity of books stored
		String tables = DBHelper.BOOK_TABLE_NAME;
		String selection = DBHelper.BOOK_TABLE_NAME + "."
				+ DBHelper.BOOK_FIELD_ID + " = " + selectedBookID;
		String[] columns = { DBHelper.BOOK_FIELD_QUANTITY };

		// store result of query
		Cursor result = db.query(tables, columns, selection, null, null, null,
				null);
		result.moveToFirst();
		int qty = result.getShort(0);

		tables = DBHelper.LOAN_TABLE_NAME;
		selection = DBHelper.LOAN_TABLE_NAME + "."
				+ DBHelper.LOAN_FIELD_BOOK_ID + " = " + selectedBookID;
		columns[0] = DBHelper.LOAN_FIELD_ID;

		// store result of query
		result = db.query(tables, columns, selection, null, null, null, null);

		// determine the number of books on loan
		int onLoan = 0;
		while (result.moveToNext())
			onLoan++;

		if (onLoan < qty)
			return true;

		return false;
	}
}

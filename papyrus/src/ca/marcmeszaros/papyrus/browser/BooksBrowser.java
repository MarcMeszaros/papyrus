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

import ca.marcmeszaros.papyrus.AlarmReceiver;
import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.Settings;
import ca.marcmeszaros.papyrus.database.AddBook;
import ca.marcmeszaros.papyrus.database.AddLibrary;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.database.Loan;
import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;
import ca.marcmeszaros.papyrus.fragment.BooksListFragment;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Calendar;

public class BooksBrowser extends FragmentActivity implements OnItemSelectedListener, OnItemClickListener,
		OnItemLongClickListener, DialogInterface.OnClickListener, OnDateSetListener {

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

		// Create the list fragment and add it as our sole content.
		if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
			BooksListFragment list = new BooksListFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
		}
	}

	/**
	 * Handles a Click from an item in the list.
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		// set the item id to a class variable
		this.selectedBookID = id;

		String[] columns = { 
				PapyrusContentProvider.Books.FIELD_ISBN10, 
				PapyrusContentProvider.Books.FIELD_ISBN13, 
				PapyrusContentProvider.Books.FIELD_TITLE,
				PapyrusContentProvider.Books.FIELD_AUTHOR, 
				PapyrusContentProvider.Books.FIELD_PUBLISHER, 
				PapyrusContentProvider.Books.FIELD_QUANTITY,
				PapyrusContentProvider.Books.FIELD_ID, 
				PapyrusContentProvider.Books.FIELD_LIBRARY_ID };

		// delete the entry in the database
		Uri bookQuery = ContentUris.withAppendedId(PapyrusContentProvider.Books.CONTENT_URI, id);
		Cursor bookCursor = getContentResolver().query(bookQuery, columns, null, null, null);

		bookCursor.moveToFirst();

		Book book = new Book(bookCursor.getString(0), bookCursor.getString(1), bookCursor.getString(2),
				bookCursor.getString(3));
		book.setPublisher(bookCursor.getString(4));
		book.setQuantity(bookCursor.getInt(5));
		book.setBookID(bookCursor.getInt(6));
		book.setLibraryID(bookCursor.getInt(7));

		bookCursor.close();
		
		// create the intent and start the activity
		Intent intent = new Intent(this, BookDetails.class);
		intent.putExtra("book", book);
		startActivity(intent);
	}

	/**
	 * Handles a LongClick from an item in the list (create a dialog).
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, final long id) {
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
		// delete
		case 0:
			// delete the entry in the database
			Uri bookDelete = ContentUris.withAppendedId(PapyrusContentProvider.Books.CONTENT_URI, selectedBookID);
			getContentResolver().delete(bookDelete, null, null);

			Toast.makeText(getApplicationContext(), getString(R.string.BooksBrowser_toast_bookDeleted),
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
					c.setTimeInMillis(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 14));
					mYear = c.get(Calendar.YEAR);
					mMonth = c.get(Calendar.MONTH);
					mDay = c.get(Calendar.DAY_OF_MONTH);

					// Launch Date Picker Box
					showDialog(DATE_DIALOG_ID);
				} else {
					// there are no more copies left in the library
					Toast.makeText(this, getString(R.string.BooksBrowser_toast_allCopiesLentOut), Toast.LENGTH_LONG)
							.show();
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
			Cursor result = getContentResolver().query(PapyrusContentProvider.Libraries.CONTENT_URI, null, null, null, null);
			if (result.getCount() > 0) {
				startActivity(new Intent(this, AddBook.class));
			} else {
				startActivity(new Intent(this, AddLibrary.class));
			}
			result.close();
			break;
		case R.id.BooksBrowser_Settings_menu:
			startActivity(new Intent(this, Settings.class));
			break;
		}
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> adapter, View selected, int position, long id) {
		// create an instance of the db helper class
		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase db = helper.getReadableDatabase();

		switch (adapter.getId()) {
		case R.id.BooksBrowser_spinner_library:
			String selection = DBHelper.BOOK_TABLE_NAME + "." + DBHelper.BOOK_FIELD_LIBRARY_ID + "=" + id;
			Cursor result = db
					.query(DBHelper.BOOK_TABLE_NAME, null, selection, null, null, null, DBHelper.BOOK_FIELD_TITLE);
			startManagingCursor(result);

			((ListView) findViewById(android.R.id.list)).setAdapter(new BookAdapter(this, result));
			break;

		default:
			break;
		}

		db.close();
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
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
		}
		return null;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		// TODO Auto-generated method stub
	}

	/**
	 * the callback received when the user "sets" the date in the dialog
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
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
		String selection = DBHelper.LOAN_TABLE_NAME + "." + DBHelper.LOAN_FIELD_BOOK_ID + " = " + selectedBookID
				+ " AND " + DBHelper.LOAN_TABLE_NAME + "." + DBHelper.LOAN_FIELD_CONTACT_ID + " = " + id;
		String[] columns = { DBHelper.LOAN_FIELD_ID };
		Cursor cursor = db.query(tables, columns, selection, null, null, null, DBHelper.LOAN_FIELD_ID + " DESC");
		cursor.moveToFirst();
		int loanID = cursor.getInt(0);
		cursor.close();

		// close the db
		db.close();

		// Book book = new Book(isbn10, title, author);
		Loan loan = new Loan(loanID, values.getAsInteger(DBHelper.LOAN_FIELD_BOOK_ID),
				values.getAsInteger(DBHelper.LOAN_FIELD_CONTACT_ID), values.getAsLong(DBHelper.LOAN_FIELD_LEND_DATE),
				values.getAsLong(DBHelper.LOAN_FIELD_DUE_DATE));

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
		// Get the quantity of books stored
		Uri bookQuery = ContentUris.withAppendedId(PapyrusContentProvider.Books.CONTENT_URI, selectedBookID);
		String[] columns = { PapyrusContentProvider.Books.FIELD_QUANTITY };
		// store result of query
		Cursor result = getContentResolver().query(bookQuery, columns, null, null, null);
		result.moveToFirst();
		int qty = result.getShort(0);

		String selection = PapyrusContentProvider.Loans.FIELD_BOOK_ID + " = ?";
		String[] selectionArgs = { Long.toString(selectedBookID) };
		columns[0] = PapyrusContentProvider.Loans.FIELD_ID;

		// store result of query
		result = getContentResolver().query(PapyrusContentProvider.Loans.CONTENT_URI, columns, selection, selectionArgs, null);

		if (result.getCount() < qty) {
			result.close();
			return true;
		} else {
			result.close();
			return false;
		}
	}
}
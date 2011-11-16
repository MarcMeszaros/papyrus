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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.Settings;
import ca.marcmeszaros.papyrus.database.AddLibrary;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.database.Loan;
import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;

public class LoansBrowser extends ListActivity implements OnItemClickListener,
		OnItemLongClickListener, DialogInterface.OnClickListener {

	// class variables
	private BookAdapter adapter;
	private Cursor result;
	private long selectedLoanID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loans_browser);

		// set listeners for list clicks and long clicks to this activity
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);

		// create an instance of the db helper class
		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase db = helper.getWritableDatabase();

		// strings for use in db query
		String tables = DBHelper.LOAN_TABLE_NAME + ", " + DBHelper.BOOK_TABLE_NAME;
		String[] columns = {
				DBHelper.BOOK_TABLE_NAME + "." + DBHelper.BOOK_FIELD_TITLE,
				DBHelper.BOOK_TABLE_NAME + "." + DBHelper.BOOK_FIELD_AUTHOR,
				DBHelper.BOOK_TABLE_NAME + "." + DBHelper.BOOK_FIELD_ISBN10,
				DBHelper.BOOK_TABLE_NAME + "." + DBHelper.BOOK_FIELD_ISBN13,
				DBHelper.LOAN_TABLE_NAME + "." + DBHelper.LOAN_FIELD_CONTACT_ID,
				DBHelper.LOAN_TABLE_NAME + "." + DBHelper.LOAN_FIELD_ID };
		String selection = DBHelper.LOAN_TABLE_NAME + "."
				+ DBHelper.LOAN_FIELD_BOOK_ID + " = "
				+ DBHelper.BOOK_TABLE_NAME + "." + DBHelper.BOOK_FIELD_ID;
		/*
		 * SELECT books.title, loans.contact_ID FROM books, loans where
		 * loans.book_ID = books."_id"
		 */
		this.result = db.query(tables, columns, selection, null, null, null, null);

		startManagingCursor(result);

		// create our custom adapter with our result
		this.adapter = new BookAdapter(this, result);

		// set the adapter to the ListView to display the books
		setListAdapter(adapter);
	}

	/**
	 * Handles a Click from an item in the list.
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "Loan details not implemented yet.", Toast.LENGTH_SHORT).show();
		
		// set the item id to a class variable
		this.selectedLoanID = id;
		
		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase db = helper.getReadableDatabase();
		
		/* do a join on Loan and Book to get the book information and
		 * the contact ID for the person the book is loaned to
		 */
		String tables = DBHelper.LOAN_TABLE_NAME + ", " + DBHelper.BOOK_TABLE_NAME;
		String selection = DBHelper.LOAN_TABLE_NAME + "."
				+ DBHelper.LOAN_FIELD_BOOK_ID + " = "
				+ DBHelper.BOOK_TABLE_NAME + "." + DBHelper.BOOK_FIELD_ID
				+ " AND " +
				DBHelper.LOAN_TABLE_NAME + "." + DBHelper.LOAN_FIELD_ID + " = " + selectedLoanID;
		String[] columns = {
			DBHelper.BOOK_FIELD_ISBN10,
			DBHelper.BOOK_FIELD_ISBN13,
			DBHelper.BOOK_FIELD_TITLE,
			DBHelper.BOOK_FIELD_AUTHOR,
			DBHelper.LOAN_TABLE_NAME+"."+DBHelper.LOAN_FIELD_ID,
			DBHelper.LOAN_FIELD_BOOK_ID,
			DBHelper.LOAN_FIELD_CONTACT_ID,
			DBHelper.LOAN_FIELD_LEND_DATE,
			DBHelper.LOAN_FIELD_DUE_DATE
		};
		
		// store result of query
		Cursor result = db.query(tables, columns, selection, null, null, null, null);
		
		result.moveToFirst();
		
		Book book = new Book(result.getString(0), result.getString(1), result.getString(2), result.getString(3));
		Loan loan = new Loan(result.getInt(4), result.getInt(5), result.getInt(6), result.getLong(7), result.getLong(8));
		
		Intent intent = new Intent(this, LoanDetails.class);
		
		intent.putExtra("book", book);
		intent.putExtra("loan", loan);
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
		this.selectedLoanID = id;

		// setup the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.LoansBrowser_LongClickDialog_title));

		// create the dialog items
		final CharSequence[] items = { getString(R.string.LoansBrowser_LongClickDialog_returnBook) };

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
		// return book
		case 0:
			// create an instance of the db helper class
			DBHelper helper = new DBHelper(getApplicationContext());
			SQLiteDatabase db = helper.getWritableDatabase();

			// delete the entry in the database
			db.delete(DBHelper.LOAN_TABLE_NAME, DBHelper.LOAN_FIELD_ID + "="
					+ selectedLoanID, null);
			db.close();

			// requery the database
			result.requery();
			// tell the list we have new data
			adapter.notifyDataSetChanged();
			Toast.makeText(getApplicationContext(), getString(R.string.LoansBrowser_toast_bookReturned),
					Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	/**
	 * Creates the menu when the "menu" button is pressed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.loans_browser , menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.LibrariesBrowser_menu_addLibrary:
			startActivity(new Intent(this, AddLibrary.class));
			break;
		case R.id.BooksBrowser_Settings_menu:
			startActivity(new Intent(this, Settings.class));		
			break;
		}
		return false;
	}

}

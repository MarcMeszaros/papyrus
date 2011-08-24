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

package ca.marcmeszaros.papyrus.browser;

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.database.DBHelper;
import ca.marcmeszaros.papyrus.database.Loan;
import ca.marcmeszaros.papyrus.database.TNManager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

//imports for getting contact information
import android.provider.ContactsContract;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

public class LoanDetails extends Activity implements OnClickListener, OnDateSetListener {

	private Loan loan;
	private int mYear;
	private int mMonth;
	private int mDay;

	private long dDate;
	private Button dueDate;
	private final String[] MONTH_ENUM = { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October",
			"November", "December" };

	static final int DATE_DIALOG_ID = 0;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loan_details);

		/*
		 * get book information, loan information and contact ID
		 */
		Bundle bundle = getIntent().getExtras();
		Book book = bundle.getParcelable("book");
		loan = bundle.getParcelable("loan");
		String name = "";
		
		// retrieve contact information
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				int id = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts._ID));
				// we have a match, get the name, phone, and email
				if (loan.getContactID() == id) {
					// get name
					name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				}
			}
		}
		/*
		 * show book thumbnail, title, and author
		 */
		ImageView cover = (ImageView) findViewById(R.id.LoanDetails_book_cover);
		TextView title = (TextView) findViewById(R.id.LoanDetails_book_title);
		TextView author = (TextView) findViewById(R.id.LoanDetails_book_author);

		title.setText(book.getTitle());
		author.setText(book.getAuthor());

		/*
		 * show loan date and due date
		 */
		Button lendDate = (Button) findViewById(R.id.LoanDetails_lendDate_button);
		dueDate = (Button) findViewById(R.id.LoanDetails_dueDate_button);
		Calendar c = Calendar.getInstance();
		dDate = loan.getDueDate(); // Used in editing the due
													// date

		// Set the Lend Date Button
		c.setTimeInMillis(loan.getLendDate());
		lendDate.setText(MONTH_ENUM[c.get(Calendar.MONTH)] + " "
				+ c.get(Calendar.DAY_OF_MONTH) + ", " + c.get(Calendar.YEAR));

		// Set the Due Date button
		c.setTimeInMillis(loan.getDueDate());
		dueDate.setText(MONTH_ENUM[c.get(Calendar.MONTH)] + " "
				+ c.get(Calendar.DAY_OF_MONTH) + ", " + c.get(Calendar.YEAR));
		dueDate.setOnClickListener(this);

		/*
		 * show contact book is loaned to
		 */
		Button contact = (Button) findViewById(R.id.LoanDetails_button);
		contact.setOnClickListener(this);
		contact.setText(name);

		/*
		 * thumbnail image
		 */
		if (book.getISBN10() != null
				&& TNManager.getThumbnail(book.getISBN10()).exists()) {
			cover.setImageURI(Uri.parse(TNManager
					.getThumbnail(book.getISBN10()).getAbsolutePath()));
		} else if (book.getISBN13() != null
				&& TNManager.getThumbnail(book.getISBN13()).exists()) {
			cover.setImageURI(Uri.parse(TNManager
					.getThumbnail(book.getISBN13()).getAbsolutePath()));
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.LoanDetails_button) {

			// append the contact id to the end of the contact CONTENT_URI
			Uri uri = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI, loan.getContactID());

			// create a new intent to view this 'content'
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			// start the activity
			startActivity(intent);

		} else if (v.getId() == R.id.LoanDetails_dueDate_button) {
			final Calendar c = Calendar.getInstance();
			c.setTimeInMillis(dDate);
			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);
			showDialog(DATE_DIALOG_ID);
		}
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
		// auto stub
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

			// update due date label and value in database
			try {

				Calendar c = Calendar.getInstance();
				c.set(mYear, mMonth, mDay);
				dDate = c.getTimeInMillis();

				/*
				 * Update due date label
				 */
				dueDate = (Button) findViewById(R.id.LoanDetails_dueDate_button);
				dueDate.setText(MONTH_ENUM[mMonth] + " " + mDay + ", " + mYear);

				/*
				 * Update Database
				 */
				SQLiteDatabase db = new DBHelper(getApplicationContext())
						.getWritableDatabase();

				// create the update query
				ContentValues values = new ContentValues();
				values.put(DBHelper.LOAN_FIELD_DUE_DATE, dDate);

				// select the right book
				String whereClause = DBHelper.LOAN_TABLE_NAME + "."
						+ DBHelper.LOAN_FIELD_ID + "=" + loan.getLoanID();

				// update and close the db
				db.update(DBHelper.LOAN_TABLE_NAME, values, whereClause, null);
				db.close();

			} catch (Exception e) {
				// do something eventually
			}
		}
	};
}

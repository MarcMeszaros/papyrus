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

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.database.Loan;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;
import ca.marcmeszaros.papyrus.tools.TNManager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

public class LoanDetails extends Activity implements OnClickListener {

	private Loan loan;

	private long dDate;
	private Button dueDate;
	private final String[] MONTH_ENUM = {
			"January", "February", "March",
			"April", "May", "June",
			"July", "August", "September",
			"October", "November", "December"
	};

	static final int DATE_DIALOG_ID = 0;

	private ContentResolver resolver;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loan_details);

		// get the content resolver
		resolver = getContentResolver();

		/*
		 * get book information, loan information and contact ID
		 */
		Bundle bundle = getIntent().getExtras();
		Book book = bundle.getParcelable("book");
		loan = bundle.getParcelable("loan");
		String name = "";

		// retrieve contact information
		Cursor cur = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
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
		if (book.getISBN10() != null && TNManager.getThumbnail(book.getISBN10()).exists()) {
			cover.setImageURI(Uri.parse(TNManager.getThumbnail(book.getISBN10()).getAbsolutePath()));
		} else if (book.getISBN13() != null && TNManager.getThumbnail(book.getISBN13()).exists()) {
			cover.setImageURI(Uri.parse(TNManager.getThumbnail(book.getISBN13()).getAbsolutePath()));
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.LoanDetails_button) {

			// append the contact id to the end of the contact CONTENT_URI
			Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, loan.getContactID());

			// create a new intent to view this 'content'
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			// start the activity
			startActivity(intent);

		} else if (v.getId() == R.id.LoanDetails_dueDate_button) {
			final Calendar c = Calendar.getInstance();
			c.setTimeInMillis(dDate);
			int mYear = c.get(Calendar.YEAR);
			int mMonth = c.get(Calendar.MONTH);
			int mDay = c.get(Calendar.DAY_OF_MONTH);

			// create the custom dialog title view block
			LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(
					R.layout.datepickerdialog_customtitle_twoline, null);
			TextView title = (TextView) linearLayout
					.findViewById(R.id.DatePickerDialog_customTitle_twoline_title);
			TextView titleDescription = (TextView) linearLayout
					.findViewById(R.id.DatePickerDialog_customTitle_twoline_description);

			// set the text
			title.setText(R.string.AlertDialog_LoanReturnDateDialog_title);
			titleDescription.setText(R.string.AlertDialog_LoanReturnDateDialog_titleDescription);

			// create the dialog with the custom header and display it
			DatePickerDialog dialog = new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
			dialog.setCustomTitle(linearLayout);
			dialog.show();
		}
	}

	/**
	 * the callback received when the user "sets" the date in the dialog
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			// update due date label and value in database
			try {

				Calendar c = Calendar.getInstance();
				c.set(year, monthOfYear, dayOfMonth);
				dDate = c.getTimeInMillis();

				/*
				 * Update due date label
				 */
				dueDate = (Button) findViewById(R.id.LoanDetails_dueDate_button);
				dueDate.setText(MONTH_ENUM[monthOfYear] + " " + dayOfMonth + ", " + year);

				// create the update query
				ContentValues values = new ContentValues();
				values.put(PapyrusContentProvider.Loans.FIELD_DUE_DATE, dDate);

				// select the right book and update it
				Uri updateLoan = ContentUris.withAppendedId(PapyrusContentProvider.Loans.CONTENT_URI, loan.getLoanID());
				resolver.update(updateLoan, values, null, null);

			} catch (Exception e) {
				// do something eventually
			}
		}
	};
}

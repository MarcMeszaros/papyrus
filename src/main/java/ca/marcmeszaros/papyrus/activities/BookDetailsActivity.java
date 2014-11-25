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
package ca.marcmeszaros.papyrus.activities;

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;
import ca.marcmeszaros.papyrus.util.TNManager;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BookDetailsActivity extends Activity implements OnClickListener {

	private EditText quantity;
	private Spinner library;
	private Book book;
	private ContentResolver resolver;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_details);

		// get the content resolver for the activity
		resolver = getContentResolver();

		Bundle bundle = getIntent().getExtras();
		this.book = bundle.getParcelable("book");

		findViewById(R.id.activity_book_details__save_button).setOnClickListener(this);

		// get handles on all the elements in the layout
		ImageView cover = (ImageView) findViewById(R.id.activity_book_details__cover);
		TextView title = (TextView) findViewById(R.id.activity_book_details__title);
		TextView author = (TextView) findViewById(R.id.activity_book_details__author);
		TextView publisher = (TextView) findViewById(R.id.activity_book_details__publisher);
		TextView isbn10 = (TextView) findViewById(R.id.activity_book_details__isbn10);
		TextView isbn13 = (TextView) findViewById(R.id.activity_book_details__isbn13);
		this.quantity = (EditText) findViewById(R.id.activity_book_details__quantity);
		this.library = (Spinner) findViewById(R.id.activity_book_details__library);

		// fill in all the values we can from the book object
		title.setText(book.getTitle());
		author.setText(book.getAuthor());
		publisher.setText(book.getPublisher());
		isbn10.setText(book.getISBN10());
		isbn13.setText(book.getISBN13());
		quantity.setText(String.valueOf(book.getQuantity()));

		// get the library the book belongs to
		Cursor result = resolver.query(PapyrusContentProvider.Libraries.CONTENT_URI, null, null, null, PapyrusContentProvider.Libraries.FIELD_NAME);
		startManagingCursor(result);

		// specify what fields to map to what views
		String[] from = { PapyrusContentProvider.Libraries.FIELD_NAME };
		int[] to = { android.R.id.text1 };

		// create a cursor adapter and set it to the list
		SimpleCursorAdapter adp = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, result, from, to);
		adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		library.setAdapter(adp);

		// set the spinner selection to the matching default library id
		for (int i = 0; i < library.getCount(); i++) {
			Cursor value = (Cursor) library.getItemAtPosition(i);
			long id = value.getLong(value.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_ID));
			if (id == book.getLibraryID()) {
				library.setSelection(i);
			}
		}

		// set the thumbnail
		if (book.getISBN10() != null && TNManager.getThumbnail(book.getISBN10()).exists()) {
			cover.setImageURI(Uri.parse(TNManager.getThumbnail(book.getISBN10()).getAbsolutePath()));
		} else if (book.getISBN13() != null && TNManager.getThumbnail(book.getISBN13()).exists()) {
			cover.setImageURI(Uri.parse(TNManager.getThumbnail(book.getISBN13()).getAbsolutePath()));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_book_details__save_button:
			// create the update query
			ContentValues values = new ContentValues();
			values.put(PapyrusContentProvider.Books.FIELD_QUANTITY, Integer.parseInt(quantity.getText().toString()));
			values.put(PapyrusContentProvider.Books.FIELD_LIBRARY_ID, library.getSelectedItemId());

			// update the right book
			Uri updateBook = ContentUris.withAppendedId(PapyrusContentProvider.Books.CONTENT_URI, book.getBookID());
			resolver.update(updateBook, values, null, null);

			Toast.makeText(this, getString(R.string.BookDetails_book_update_toast), Toast.LENGTH_LONG).show();
			break;
		}

	}

}

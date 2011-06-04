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
import ca.marcmeszaros.papyrus.database.TNManager;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class BookDetails extends Activity implements OnClickListener {

	private EditText quantity;
	private Spinner library;
	private Book book;
	
	/** 
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_details);

		Bundle bundle = getIntent().getExtras();
		this.book = bundle.getParcelable("book");
		
		findViewById(R.id.BookDetails_book_update_button).setOnClickListener(this);
		
		// get handles on all the elements in the layout
		ImageView cover = (ImageView) findViewById(R.id.BookDetails_book_cover);
		TextView title = (TextView) findViewById(R.id.BookDetails_book_title);
		TextView author = (TextView) findViewById(R.id.BookDetails_book_author);
		TextView publisher = (TextView) findViewById(R.id.BookDetails_book_publisher);
		TextView isbn10 = (TextView) findViewById(R.id.BookDetails_book_isbn10);
		TextView isbn13 = (TextView) findViewById(R.id.BookDetails_book_isbn13);
		this.quantity = (EditText) findViewById(R.id.BookDetails_book_quantity);
		this.library = (Spinner) findViewById(R.id.BookDetails_book_library);
		
		// fill in all the values we can from the book object
		title.setText(book.getTitle());
		author.setText(book.getAuthor());
		publisher.setText(book.getPublisher());
		isbn10.setText(book.getISBN10());
		isbn13.setText(book.getISBN13());
		quantity.setText(String.valueOf(book.getQuantity()));
		
		// get the library name
		SQLiteDatabase db = new DBHelper(getApplicationContext()).getReadableDatabase();
		
		// get the library the book belongs to
		Cursor result = db.query(DBHelper.LIBRARY_TABLE_NAME, null, null, null, null, null, DBHelper.LIBRARY_FIELD_NAME);
		startManagingCursor(result);
		
		// set the library name from the db info
		// specify what fields to map to what views
		String[] from = {DBHelper.LIBRARY_FIELD_NAME};
		int[] to = {android.R.id.text1};
		
		// create a cursor adapter and set it to the list
		SimpleCursorAdapter adp = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, result, from, to);
		adp.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		library.setAdapter(adp);
		
			
		// set the spinner selection to the matching default library id
		for (int i = 0; i < library.getCount(); i++) {
		    Cursor value = (Cursor) library.getItemAtPosition(i);
		    long id = value.getLong(value.getColumnIndex(DBHelper.LIBRARY_FIELD_ID));
		    if (id == book.getLibraryID()) {
		        library.setSelection(i);
		    }
		}
		
		// set the thumbnail
		if(book.getISBN10() != null && TNManager.getThumbnail(book.getISBN10()).exists()){
			cover.setImageURI(Uri.parse(TNManager.getThumbnail(book.getISBN10()).getAbsolutePath()));
		}
		else if(book.getISBN13() != null && TNManager.getThumbnail(book.getISBN13()).exists()){
			cover.setImageURI(Uri.parse(TNManager.getThumbnail(book.getISBN13()).getAbsolutePath()));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.BookDetails_book_update_button:
			// update the database
			SQLiteDatabase db = new DBHelper(getApplicationContext()).getWritableDatabase();
			
			// create the update query
			ContentValues values = new ContentValues();
			values.put(DBHelper.BOOK_FIELD_QUANTITY, Integer.parseInt(quantity.getText().toString()));
			values.put(DBHelper.BOOK_FIELD_LIBRARY_ID, library.getSelectedItemId());
			
			// select the right book
			String whereClause = DBHelper.BOOK_TABLE_NAME+"."+DBHelper.BOOK_FIELD_ID+"="+book.getBookID();
			
			// update and close the db
			db.update(DBHelper.BOOK_TABLE_NAME, values, whereClause, null);
			db.close();
			
			Toast.makeText(this, getString(R.string.BookDetails_book_update_toast), Toast.LENGTH_LONG).show();
			break;
		}
		
	}
	
}

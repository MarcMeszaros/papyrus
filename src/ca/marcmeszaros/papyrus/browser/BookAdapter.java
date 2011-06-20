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
import ca.marcmeszaros.papyrus.database.DBHelper;
import ca.marcmeszaros.papyrus.database.TNManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class BookAdapter extends CursorAdapter implements SectionIndexer {
	
	private static final String TAG = "BookAdapter";
	
	public BookAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView cover = (ImageView) view.findViewById(R.id.BookRow_book_cover);
		TextView title = (TextView) view.findViewById(R.id.BookRow_book_title);
		TextView author = (TextView) view.findViewById(R.id.BookRow_book_author);
		
		title.setText(cursor.getString(cursor.getColumnIndex(DBHelper.BOOK_FIELD_TITLE)));
		author.setText(cursor.getString(cursor.getColumnIndex(DBHelper.BOOK_FIELD_AUTHOR)));
		
		// try and get both isbn numbers from the result
		String isbn10 = cursor.getString(cursor.getColumnIndex(DBHelper.BOOK_FIELD_ISBN10));
		String isbn13 = cursor.getString(cursor.getColumnIndex(DBHelper.BOOK_FIELD_ISBN13));
		
		// check if we got an isbn10 number from query and file exists
		if(isbn10 != null && TNManager.getThumbnail(isbn10).exists()){
			Log.i(TAG, "Set cover image path (ISBN10)");
			cover.setImageURI(Uri.parse(TNManager.getThumbnail(isbn10).getAbsolutePath()));
		}
		// check if we got an isbn13 number from query and file exists
		else if(isbn13 != null && TNManager.getThumbnail(isbn13).exists()){
			Log.i(TAG, "Set cover image path (ISBN13)");
			cover.setImageURI(Uri.parse(TNManager.getThumbnail(isbn13).getAbsolutePath()));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.view_book_row, parent, false);
		bindView(v, context, cursor);
		return v;
	}

	// TODO implement later
	@Override
	public int getPositionForSection(int section) {
		// TODO Auto-generated method stub
		return 0;
	}

	// TODO implement later
	@Override
	public int getSectionForPosition(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	// TODO implement later
	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		return null;
	}
}

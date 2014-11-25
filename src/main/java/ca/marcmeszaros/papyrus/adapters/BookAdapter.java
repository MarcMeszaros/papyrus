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
package ca.marcmeszaros.papyrus.adapters;

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;
import ca.marcmeszaros.papyrus.util.TNManager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookAdapter extends CursorAdapter {

	private static final String TAG = "BookAdapter";

	public BookAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView cover = (ImageView) view.findViewById(R.id.BookRow_book_cover);
		TextView title = (TextView) view.findViewById(R.id.BookRow_book_title);
		TextView author = (TextView) view.findViewById(R.id.BookRow_book_author);

		title.setText(cursor.getString(cursor.getColumnIndex(PapyrusContentProvider.Books.FIELD_TITLE)));
		author.setText(cursor.getString(cursor.getColumnIndex(PapyrusContentProvider.Books.FIELD_AUTHOR)));

		// try and get both isbn numbers from the result
		String isbn10 = cursor.getString(cursor.getColumnIndex(PapyrusContentProvider.Books.FIELD_ISBN10));
		String isbn13 = cursor.getString(cursor.getColumnIndex(PapyrusContentProvider.Books.FIELD_ISBN13));

		// check if we got an isbn10 number from query and file exists
		if (isbn10 != null && TNManager.getThumbnail(isbn10).exists()) {
			Log.i(TAG, "Set cover image path (ISBN10)");
			cover.setImageURI(Uri.parse(TNManager.getThumbnail(isbn10).getAbsolutePath()));
		} else if (isbn13 != null && TNManager.getThumbnail(isbn13).exists()) {
			// check if we got an isbn13 number from query and file exists
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

}
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
package ca.marcmeszaros.papyrus.fragments;

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.browser.BookAdapter;
import ca.marcmeszaros.papyrus.browser.BookDetails;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * Manage the fragment lifecycle that lists all the books.
 */
public class BooksListFragment extends ListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener {

	private static final String TAG = "BooksListFragment";
	
	private static final int BOOKS = 0x01;
	private static final int LIBRARIES = 0x02;
	
	// fragment variables
	BookAdapter books;
	SimpleCursorAdapter libraries;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Spinner spinner = (Spinner) getView().findViewById(R.id.BooksBrowser_spinner_library);
		
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener((OnItemLongClickListener) getActivity());
		spinner.setOnItemSelectedListener((OnItemSelectedListener) getActivity());
		
		books = new BookAdapter(getActivity(), null);
		setListAdapter(books);
		
		// specify what fields to map to what views
		String[] from = { PapyrusContentProvider.Libraries.FIELD_NAME };
		int[] to = { android.R.id.text1 };
		libraries = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, null, from, to);
		libraries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(libraries);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(BOOKS, null, this);
		getLoaderManager().initLoader(LIBRARIES, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_books_browser, null);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		switch (id) {
		case BOOKS:
			return new CursorLoader(getActivity(), PapyrusContentProvider.Books.CONTENT_URI, null, null, null,
					PapyrusContentProvider.Books.FIELD_TITLE);
			
		case LIBRARIES:
			return new CursorLoader(getActivity(), PapyrusContentProvider.Libraries.CONTENT_URI, null, null, null,
					PapyrusContentProvider.Libraries.FIELD_NAME);

		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		switch (loader.getId()) {
		case BOOKS:
			books.changeCursor(data);
			break;
		case LIBRARIES:
			libraries.changeCursor(data);
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		switch (loader.getId()) {
		case BOOKS:
			books.changeCursor(null);
			break;
			
		case LIBRARIES:
			libraries.changeCursor(null);
			break;

		default:
			break;
		}
		
	}

	/**
	 * Handles a Click from an item in the list.
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		// build the query
		String[] projection = {
				PapyrusContentProvider.Books.FIELD_ISBN10, 
				PapyrusContentProvider.Books.FIELD_ISBN13, 
				PapyrusContentProvider.Books.FIELD_TITLE,
				PapyrusContentProvider.Books.FIELD_AUTHOR, 
				PapyrusContentProvider.Books.FIELD_PUBLISHER, 
				PapyrusContentProvider.Books.FIELD_QUANTITY,
				PapyrusContentProvider.Books.FIELD_ID, 
				PapyrusContentProvider.Books.FIELD_LIBRARY_ID
		};
		String selection = PapyrusContentProvider.Books.FIELD_ID + "=?";
		String[] selectionArgs = { Long.toString(id) };
		
		// get the book from the database
		Cursor bookCursor = getActivity().getContentResolver().query(PapyrusContentProvider.Books.CONTENT_URI, projection, selection, selectionArgs, null);
		bookCursor.moveToFirst();

		// create the book model
		Book book = new Book(bookCursor.getString(0), bookCursor.getString(1), bookCursor.getString(2),bookCursor.getString(3));
		book.setPublisher(bookCursor.getString(4));
		book.setQuantity(bookCursor.getInt(5));
		book.setBookID(bookCursor.getInt(6));
		book.setLibraryID(bookCursor.getInt(7));

		// close the cursor
		bookCursor.close();
		
		// store the book as data to be passed
		Intent intent = new Intent(getActivity(), BookDetails.class);
		intent.putExtra("book", book);
		startActivity(intent);
	}

}

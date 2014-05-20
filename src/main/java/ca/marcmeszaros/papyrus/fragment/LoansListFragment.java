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
package ca.marcmeszaros.papyrus.fragment;

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.browser.BookAdapter;
import ca.marcmeszaros.papyrus.browser.LoanDetails;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.database.Loan;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * Manage the fragment lifecycle that lists all the books.
 */
public class LoansListFragment extends ListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener {

	private static final String TAG = "LoansListFragment";
	
	private static final int LOANS = 0x01;
	
	// fragment variables
	BookAdapter books;
	SimpleCursorAdapter libraries;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener((OnItemLongClickListener) getActivity());
		
		books = new BookAdapter(getActivity(), null);
		setListAdapter(books);
		
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(LOANS, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_loans_browser, null);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		switch (id) {
		case LOANS:
			Uri loansUri = Uri.withAppendedPath(PapyrusContentProvider.Loans.CONTENT_URI, "details");
			return new CursorLoader(getActivity(), loansUri, null, null, null, null);

		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		switch (loader.getId()) {
		case LOANS:
			books.changeCursor(data);
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
		case LOANS:
			books.changeCursor(null);
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
		/* do a join on Loan and Book to get the book information and
		 * the contact ID for the person the book is loaned to
		 */
		String[] columns = {
			PapyrusContentProvider.Loans.TABLE_NAME + "." + PapyrusContentProvider.Loans.FIELD_ID,
			PapyrusContentProvider.Loans.FIELD_BOOK_ID,
			PapyrusContentProvider.Loans.FIELD_CONTACT_ID,
			PapyrusContentProvider.Loans.FIELD_LEND_DATE,
			PapyrusContentProvider.Loans.FIELD_DUE_DATE,
			PapyrusContentProvider.Books.FIELD_ISBN10,
			PapyrusContentProvider.Books.FIELD_ISBN13,
			PapyrusContentProvider.Books.FIELD_TITLE,
			PapyrusContentProvider.Books.FIELD_AUTHOR
		};

		// store result of query
		Uri loansUri = Uri.withAppendedPath(ContentUris.withAppendedId(PapyrusContentProvider.Loans.CONTENT_URI, id), "details");
		Cursor result = getActivity().getContentResolver().query(loansUri, columns, null, null, null);
		result.moveToFirst();

		Book book = new Book(result.getString(5), result.getString(6), result.getString(7), result.getString(8));
		Loan loan = new Loan(result.getInt(0), result.getInt(1), result.getInt(2), result.getLong(3), result.getLong(4));

		// close the no longer needed cursor
		result.close();

		Intent intent = new Intent(getActivity(), LoanDetails.class);

		intent.putExtra("book", book);
		intent.putExtra("loan", loan);

		startActivity(intent);
	}

}

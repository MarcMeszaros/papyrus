package ca.marcmeszaros.papyrus.fragments;

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.activities.SettingsActivity;
import ca.marcmeszaros.papyrus.adapters.BookAdapter;
import ca.marcmeszaros.papyrus.activities.LoanDetailsActivity;
import ca.marcmeszaros.papyrus.activities.AddLibraryActivity;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.database.Loan;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Manage the fragment lifecycle that lists all the books.
 */
public class LoansListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, OnItemLongClickListener, DialogInterface.OnClickListener {

	private static final String TAG = "LoansListFragment";
	
	private static final int LOANS = 0x01;
	
	// fragment variables
    private long selectedLoanID;
	private BookAdapter books;
	private SimpleCursorAdapter libraries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_loans_list, null);
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);

        books = new BookAdapter(getActivity(), null);
        setListAdapter(books);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(LOANS, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.loans_browser, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.LibrariesBrowser_menu_addLibrary:
                startActivity(new Intent(getActivity(), AddLibraryActivity.class));
                break;
            case R.id.BooksBrowser_Settings_menu:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
        }
        return false;
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
     * Handles a click event from the LongClickDialog.
     */
    @Override
    public void onClick(DialogInterface dialog, int position) {
        switch (position) {
            // return book
            case 0:
                // delete the entry in the database
                Uri loanDelete = ContentUris.withAppendedId(PapyrusContentProvider.Loans.CONTENT_URI, selectedLoanID);
                getActivity().getContentResolver().delete(loanDelete, null, null);

                Toast.makeText(getActivity(), getString(R.string.LoansBrowser_toast_bookReturned), Toast.LENGTH_SHORT).show();
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

		Intent intent = new Intent(getActivity(), LoanDetailsActivity.class);

		intent.putExtra("book", book);
		intent.putExtra("loan", loan);

		startActivity(intent);
	}

    /**
     * Handles a LongClick from an item in the list (create a dialog).
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapter, View view, final int position, final long id) {
        // set the item id to a class variable
        this.selectedLoanID = id;

        // setup the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

}

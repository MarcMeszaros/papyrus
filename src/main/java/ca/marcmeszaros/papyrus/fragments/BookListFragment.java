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

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.activities.AddBookActivity;
import ca.marcmeszaros.papyrus.activities.AddLibraryActivity;
import ca.marcmeszaros.papyrus.activities.SettingsActivity;
import ca.marcmeszaros.papyrus.adapters.BookAdapter;
import ca.marcmeszaros.papyrus.activities.BookDetailsActivity;
import ca.marcmeszaros.papyrus.database.Book;
import ca.marcmeszaros.papyrus.database.Loan;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;
import ca.marcmeszaros.papyrus.util.AlarmReceiver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Manage the fragment lifecycle that lists all the books.
 */
public class BookListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnItemClickListener,
        OnItemSelectedListener,
        OnItemLongClickListener,
        DialogInterface.OnClickListener {

    private static final String ARG_LOADER_LIBRARY_ID = "arg.loader.library.id";
    private static final int LOADER_BOOKS = 0x01;
    private static final int LOADER_LIBRARIES = 0x02;

    // fragment variables
    private long selectedBookID;
    private Intent loanData;
    BookAdapter books;
    SimpleCursorAdapter libraries;

    @InjectView(R.id.BooksBrowser_spinner_library)
    Spinner mSpinnerView;

    /**
     * the callback received when the user "sets" the date in the dialog
     */
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            loanBook(year, monthOfYear, dayOfMonth);
        }
    };

    public static BookListFragment getInstance() {
        return new BookListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, null);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
        mSpinnerView.setOnItemSelectedListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup the adapter
        books = new BookAdapter(getActivity(), null);
        setListAdapter(books);

        // specify what fields to map to what views
        String[] from = {PapyrusContentProvider.Libraries.FIELD_NAME};
        int[] to = {android.R.id.text1};
        libraries = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, null, from, to);
        libraries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerView.setAdapter(libraries);

        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(LOADER_BOOKS, null, this);
        getLoaderManager().initLoader(LOADER_LIBRARIES, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                // LOAN A BOOK
                case 1001:

                    // there are sufficient copies of the book to lend
                    if (canLoanBook()) {
                        loanData = data;

                        // set default due date
                        final Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 14));
                        int mYear = c.get(Calendar.YEAR);
                        int mMonth = c.get(Calendar.MONTH);
                        int mDay = c.get(Calendar.DAY_OF_MONTH);

                        // create the custom dialog title view block
                        LinearLayout linearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(
                                R.layout.datepickerdialog_customtitle_twoline, null);
                        TextView title = (TextView) linearLayout
                                .findViewById(R.id.DatePickerDialog_customTitle_twoline_title);
                        TextView titleDescription = (TextView) linearLayout
                                .findViewById(R.id.DatePickerDialog_customTitle_twoline_description);

                        // set the text
                        title.setText(R.string.AlertDialog_LoanReturnDateDialog_title);
                        titleDescription.setText(R.string.AlertDialog_LoanReturnDateDialog_titleDescription);

                        // create the dialog with the custom header and display it
                        DatePickerDialog dialog = new DatePickerDialog(getActivity(), mDateSetListener, mYear, mMonth, mDay);
                        dialog.setCustomTitle(linearLayout);
                        dialog.show();
                    } else {
                        // there are no more copies left in the library
                        Toast.makeText(getActivity(), getString(R.string.BooksBrowser_toast_allCopiesLentOut), Toast.LENGTH_LONG)
                                .show();
                    }

                    break;
            }

        } else {
            // gracefully handle failure
            // Log.w(DEBUG_TAG, "resultWarning: activity result not ok");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.
        // First, pick the base URI to use depending on whether we are currently filtering.
        switch (id) {
            case LOADER_BOOKS:
                if (args != null && args.containsKey(ARG_LOADER_LIBRARY_ID)) {
                    String selection = PapyrusContentProvider.Books.FIELD_LIBRARY_ID + "=?";
                    String[] selectionArgs = {Long.toString(id)};
                    return new CursorLoader(getActivity(), PapyrusContentProvider.Books.CONTENT_URI, null, selection, selectionArgs,
                            PapyrusContentProvider.Books.FIELD_TITLE);
                } else {
                    return new CursorLoader(getActivity(), PapyrusContentProvider.Books.CONTENT_URI, null, null, null,
                        PapyrusContentProvider.Books.FIELD_TITLE);
                }

            case LOADER_LIBRARIES:
                return new CursorLoader(getActivity(), PapyrusContentProvider.Libraries.CONTENT_URI, null, null, null,
                        PapyrusContentProvider.Libraries.FIELD_NAME);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the old cursor once we return.)
        switch (loader.getId()) {
            case LOADER_BOOKS:
                books.changeCursor(data);
                break;
            case LOADER_LIBRARIES:
                libraries.changeCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no longer using it.
        switch (loader.getId()) {
            case LOADER_BOOKS:
                books.changeCursor(null);
                break;

            case LOADER_LIBRARIES:
                libraries.changeCursor(null);
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
        String[] selectionArgs = {Long.toString(id)};

        // get the book from the database
        Cursor bookCursor = getActivity().getContentResolver().query(PapyrusContentProvider.Books.CONTENT_URI, projection, selection, selectionArgs, null);
        bookCursor.moveToFirst();

        // create the book model
        Book book = new Book(bookCursor.getString(0), bookCursor.getString(1), bookCursor.getString(2), bookCursor.getString(3));
        book.setPublisher(bookCursor.getString(4));
        book.setQuantity(bookCursor.getInt(5));
        book.setBookID(bookCursor.getInt(6));
        book.setLibraryID(bookCursor.getInt(7));

        // close the cursor
        bookCursor.close();

        // store the book as data to be passed
        Intent intent = new Intent(getActivity(), BookDetailsActivity.class);
        intent.putExtra("book", book);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // set the item id to a class variable
        this.selectedBookID = id;

        // setup the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.BooksBrowser_LongClickDialog_title));

        // create the dialog items
        final CharSequence[] items = {getString(R.string.BooksBrowser_LongClickDialog_delete),
                getString(R.string.BooksBrowser_LongClickDialog_lendTo)};

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
            // delete
            case 0:
                // delete the entry in the database
                Uri bookDelete = ContentUris.withAppendedId(PapyrusContentProvider.Books.CONTENT_URI, selectedBookID);
                getActivity().getContentResolver().delete(bookDelete, null, null);

                Toast.makeText(getActivity(), getString(R.string.BooksBrowser_toast_bookDeleted),
                        Toast.LENGTH_SHORT).show();
                break;
            // lend book to someone
            case 1:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 1001);
                break;
        }
    }

    /**
     * Creates the menu when the "menu" button is pressed.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.books_browser, menu);
    }

    /**
     * Handles the event when an option is selected from the option menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.BooksBrowser_menu_addBook:
                Cursor result = getActivity().getContentResolver().query(PapyrusContentProvider.Libraries.CONTENT_URI, null, null, null, null);
                if (result.getCount() > 0) {
                    startActivity(new Intent(getActivity(), AddBookActivity.class));
                } else {
                    startActivity(new Intent(getActivity(), AddLibraryActivity.class));
                }
                result.close();
                break;
            case R.id.BooksBrowser_Settings_menu:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapter, View selected, int position, long id) {
        switch (adapter.getId()) {
            case R.id.BooksBrowser_spinner_library:
                Bundle args = new Bundle(1);
                args.putLong(ARG_LOADER_LIBRARY_ID, id);
                getLoaderManager().restartLoader(LOADER_BOOKS, args, this);
                break;

            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    // helpers

    /**
     * Checks that there are enough books to loan out this copy
     */
    public boolean canLoanBook() {
        // Get the quantity of books stored
        Uri bookQuery = ContentUris.withAppendedId(PapyrusContentProvider.Books.CONTENT_URI, selectedBookID);
        String[] columns = {PapyrusContentProvider.Books.FIELD_QUANTITY};
        // store result of query
        Cursor result = getActivity().getContentResolver().query(bookQuery, columns, null, null, null);
        result.moveToFirst();
        int qty = result.getShort(0);

        String selection = PapyrusContentProvider.Loans.FIELD_BOOK_ID + " = ?";
        String[] selectionArgs = {Long.toString(selectedBookID)};
        columns[0] = PapyrusContentProvider.Loans.FIELD_ID;

        // store result of query
        result = getActivity().getContentResolver().query(PapyrusContentProvider.Loans.CONTENT_URI, columns, selection, selectionArgs, null);

        if (result.getCount() < qty) {
            result.close();
            return true;
        } else {
            result.close();
            return false;
        }
    }

    /**
     * Executes the query to loan out the book
     */
    private void loanBook(int mYear, int mMonth, int mDay) {
        // set the due date
        Calendar c = Calendar.getInstance();
        c.set(mYear, mMonth, mDay);

        // gets the uri path to the user selected
        Uri user = loanData.getData();

        // gets the user id
        String id = user.getLastPathSegment();

        // prepare the query
        ContentValues values = new ContentValues();
        values.put(PapyrusContentProvider.Loans.FIELD_BOOK_ID, selectedBookID);
        values.put(PapyrusContentProvider.Loans.FIELD_CONTACT_ID, id);
        values.put(PapyrusContentProvider.Loans.FIELD_LEND_DATE, System.currentTimeMillis());
        values.put(PapyrusContentProvider.Loans.FIELD_DUE_DATE, c.getTimeInMillis());

        // insert the entry in the database, and get the new loan id
        Uri newLoan = getActivity().getContentResolver().insert(PapyrusContentProvider.Loans.CONTENT_URI, values);
        int loanID = (int) ContentUris.parseId(newLoan);

        // Book book = new Book(isbn10, title, author);
        Loan loan = new Loan(loanID, values.getAsInteger(PapyrusContentProvider.Loans.FIELD_BOOK_ID),
                values.getAsInteger(PapyrusContentProvider.Loans.FIELD_CONTACT_ID),
                values.getAsLong(PapyrusContentProvider.Loans.FIELD_LEND_DATE),
                values.getAsLong(PapyrusContentProvider.Loans.FIELD_DUE_DATE));

        // get an alarm manager
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        // create the intent for the alarm
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);

        // put the loan object into the alarm receiver
        intent.putExtra("loan", loan);

        // create the pendingIntent to run when the alarm goes off and be handled by a receiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
        // set the repeating alarm
        am.set(AlarmManager.RTC, c.getTimeInMillis(), pendingIntent);

        Toast.makeText(getActivity(), getString(R.string.BooksBrowser_toast_loanSuccessful), Toast.LENGTH_LONG).show();
    }
}

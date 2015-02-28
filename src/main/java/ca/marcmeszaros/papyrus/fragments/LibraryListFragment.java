/**
 * Copyright 2011-2014 Marc Meszaros
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


import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.activities.SettingsActivity;
import ca.marcmeszaros.papyrus.adapters.LibraryAdapter;
import ca.marcmeszaros.papyrus.loaders.LibrariesLoader;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;
import timber.log.Timber;

public class LibraryListFragment extends ListFragment implements AdapterView.OnItemLongClickListener,
        DialogInterface.OnClickListener {

    // class variables
    private LibraryAdapter mAdapter;
    private long selectedLibraryID;
    private ContentResolver resolver;

    public static LibraryListFragment getInstance() {
        return new LibraryListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_list, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // get the content resolver
        this.resolver = getActivity().getContentResolver();

        // set listeners for list clicks and long clicks to this activity
        getListView().setOnItemLongClickListener(this);

        // set the mAdapter to the ListView to display the books
        mAdapter = new LibraryAdapter(getActivity(), null);
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, cursorLoaderCallbacks);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, final long id) {
        // set the item id to a class variable
        this.selectedLibraryID = id;

        // setup the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.LibrariesBrowser_LongClickDialog_title));

        // create the dialog items
        final CharSequence[] items = {getString(R.string.LibrariesBrowser_LongClickDialog_delete)};

        // handle a click in the dialog
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
                // setup the selection criteria
                String selection = PapyrusContentProvider.Libraries.FIELD_ID + "<>" + selectedLibraryID;
                String[] columns = {PapyrusContentProvider.Libraries.FIELD_ID, PapyrusContentProvider.Libraries.FIELD_NAME};

                // get all libraries
                final Cursor otherLibraries = resolver.query(PapyrusContentProvider.Libraries.CONTENT_URI, columns, selection, null, null);
                getActivity().startManagingCursor(otherLibraries);

                // make sure it is not the only library
                if (otherLibraries.getCount() > 0) {
                    CharSequence[] libraries = new CharSequence[otherLibraries.getCount()];

                    otherLibraries.move(-1);
                    for (int i = 0; i < otherLibraries.getCount(); i++) {
                        otherLibraries.moveToNext();
                        libraries[i] = otherLibraries.getString(otherLibraries.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_NAME));
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.LibrariesBrowser_alert_moveBooks_title));
                    builder.setItems(libraries, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            // the columns to return for the books that need to updated
                            String[] columns = {PapyrusContentProvider.Books.FIELD_ID};
                            // the selectedLibraryID still points to the one we want to delete
                            String selection = PapyrusContentProvider.Books.FIELD_LIBRARY_ID + "=" + selectedLibraryID;

                            // get all the books from the library we are deleting
                            Cursor books = resolver.query(PapyrusContentProvider.Books.CONTENT_URI, columns, selection, null, null);
                            getActivity().startManagingCursor(books);

                            Timber.i("Move to the new library in the cursor");
                            // get the library id to move books to
                            otherLibraries.moveToPosition(item);
                            Timber.i("Get the new library ID");
                            int newLibraryId = otherLibraries.getInt(otherLibraries.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_ID));

                            Timber.i("Setup update query");
                            // setup the update query
                            ContentValues values;
                            String whereClause = PapyrusContentProvider.Books.FIELD_ID + "=?";
                            String[] whereValues = new String[1];

                            Timber.i("Start looping through the books");
                            // loop through and update all books
                            for (int i = 0; i < books.getCount(); i++) {
                                books.moveToNext();
                                whereValues[0] = books.getString(0);
                                values = new ContentValues();
                                values.put(PapyrusContentProvider.Books.FIELD_LIBRARY_ID, newLibraryId);
                                resolver.update(PapyrusContentProvider.Books.CONTENT_URI, values, whereClause, whereValues);
                            }

                            // set the new default library if the one to be deleted is the default
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            String libID = pref.getString(SettingsFragment.DEFAULT_LIBRARY, "");
                            if (!libID.equals("") && Long.parseLong(libID) == selectedLibraryID) {
                                SharedPreferences.Editor prefEditor = pref.edit();
                                prefEditor.putString(SettingsFragment.DEFAULT_LIBRARY, Long.toString(newLibraryId));
                                prefEditor.apply();
                            }

                            // delete the old library entry in the database
                            Uri libraryToDelete = ContentUris.withAppendedId(PapyrusContentProvider.Libraries.CONTENT_URI, selectedLibraryID);
                            resolver.delete(libraryToDelete, null, null);

                            // tell the list we have new data
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(),
                                    getString(R.string.LibrariesBrowser_toast_libraryDeleted), Toast.LENGTH_SHORT).show();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Toast.makeText(getActivity(),
                            getString(R.string.LibrariesBrowser_toast_cantDeleteOnlyLibrary), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new LibrariesLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.changeCursor(null);
        }
    };

    /**
     * Creates the menu when the "menu" button is pressed.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.libraries_browser, menu);
    }

    /**
     * Handles the event when an option is selected from the option menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.LibrariesBrowser_Settings_menu:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
        }
        return false;
    }

    @OnClick(R.id.fragment_library_list__add_button)
    public void onClickAddLibrary(View v) {
        AddLibraryDialogFragment.getInstance().show(getFragmentManager(), AddLibraryDialogFragment.class.getCanonicalName());
    }

}

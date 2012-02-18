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
import ca.marcmeszaros.papyrus.Settings;
import ca.marcmeszaros.papyrus.database.AddLibrary;
import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class LibrariesBrowser extends ListActivity implements OnItemClickListener, OnItemLongClickListener,
		DialogInterface.OnClickListener {

	private static final String TAG = "LibrariesBrowser";

	// class variables
	private SimpleCursorAdapter adapter;
	private Cursor result;
	private long selectedLibraryID;
	private ContentResolver resolver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_libraries_browser);

		// get the content resolver
		this.resolver = getContentResolver();

		// set listeners for list clicks and long clicks to this activity
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);

		// run a query on the DB and get a Cursor (aka result)
		this.result = resolver.query(PapyrusContentProvider.Libraries.CONTENT_URI, null, null, null,
				PapyrusContentProvider.Libraries.FIELD_NAME);

		// specify what fields to map to what views
		String[] from = { PapyrusContentProvider.Libraries.FIELD_NAME };
		int[] to = { android.R.id.text1 };

		adapter = new SimpleCursorAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, result, from,
				to);

		// set the adapter to the ListView to display the books
		setListAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		// TODO Auto-generated method stub
		// Toast.makeText(this, "Library details not implemented yet.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, final long id) {
		// set the item id to a class variable
		this.selectedLibraryID = id;

		// setup the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.LibrariesBrowser_LongClickDialog_title));

		// create the dialog items
		final CharSequence[] items = { getString(R.string.LibrariesBrowser_LongClickDialog_delete) };

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
			String[] columns = { PapyrusContentProvider.Libraries.FIELD_ID, PapyrusContentProvider.Libraries.FIELD_NAME };

			// get all libraries
			final Cursor otherLibraries = resolver.query(PapyrusContentProvider.Libraries.CONTENT_URI, columns,
					selection, null, null);
			startManagingCursor(otherLibraries);

			// make sure it is not the only library
			if (otherLibraries.getCount() > 0) {
				CharSequence[] libraries = new CharSequence[otherLibraries.getCount()];

				otherLibraries.move(-1);
				for (int i = 0; i < otherLibraries.getCount(); i++) {
					otherLibraries.moveToNext();
					libraries[i] = otherLibraries.getString(otherLibraries
							.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_NAME));
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.LibrariesBrowser_alert_moveBooks_title));
				builder.setItems(libraries, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						// the columns to return for the books that need to updated
						String[] columns = { PapyrusContentProvider.Books.FIELD_ID };
						// the selectedLibraryID still points to the one we want to delete
						String selection = PapyrusContentProvider.Books.FIELD_LIBRARY_ID + "=" + selectedLibraryID;

						// get all the books from the library we are deleting
						Cursor books = resolver.query(PapyrusContentProvider.Books.CONTENT_URI, columns, selection,
								null, null);
						startManagingCursor(books);

						Log.i(TAG, "Move to the new library in the cursor");
						// get the library id to move books to
						otherLibraries.moveToPosition(item);
						Log.i(TAG, "Get the new library ID");
						int newLibraryId = otherLibraries.getInt(otherLibraries
								.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_ID));

						Log.i(TAG, "Setup update query");
						// setup the update query
						ContentValues values;
						String whereClause = PapyrusContentProvider.Books.FIELD_ID + "=?";
						String[] whereValues = new String[1];

						Log.i(TAG, "Start looping through the books");
						// loop through and update all books
						for (int i = 0; i < books.getCount(); i++) {
							books.moveToNext();
							whereValues[0] = books.getString(0);
							values = new ContentValues();
							values.put(DBHelper.BOOK_FIELD_LIBRARY_ID, newLibraryId);
							resolver.update(PapyrusContentProvider.Books.CONTENT_URI, values, whereClause, whereValues);
						}

						// set the new default library if the one to be deleted is the default
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						String libID = pref.getString(Settings.KEY_DEFAULT_LIBRARY, "");
						if (!libID.equals("") && Long.parseLong(libID) == selectedLibraryID) {
							SharedPreferences.Editor prefEditor = pref.edit();
							prefEditor.putString(Settings.KEY_DEFAULT_LIBRARY, Long.toString(newLibraryId));
							prefEditor.commit();
						}

						// delete the old library entry in the database
						Uri libraryToDelete = ContentUris.withAppendedId(PapyrusContentProvider.Libraries.CONTENT_URI, selectedLibraryID);
						resolver.delete(libraryToDelete, null, null);

						// requery the database
						result.requery();

						// tell the list we have new data
						adapter.notifyDataSetChanged();
						Toast.makeText(getApplicationContext(),
								getString(R.string.LibrariesBrowser_toast_libraryDeleted), Toast.LENGTH_SHORT).show();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.LibrariesBrowser_toast_cantDeleteOnlyLibrary), Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	/**
	 * Creates the menu when the "menu" button is pressed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.libraries_browser, menu);
		return true;
	}

	/**
	 * Handles the event when an option is selected from the option menu.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.LibrariesBrowser_menu_addLibrary:
			startActivity(new Intent(this, AddLibrary.class));
			break;
		case R.id.LibrariesBrowser_Settings_menu:
			startActivity(new Intent(this, Settings.class));
			break;
		}
		return false;
	}

}
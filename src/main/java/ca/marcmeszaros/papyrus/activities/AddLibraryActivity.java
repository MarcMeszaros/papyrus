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
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class AddLibraryActivity extends Activity implements OnClickListener {

	private ContentResolver resolver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_library);

		// get the content resolver
		resolver = getContentResolver();

		// set on click listeners
		findViewById(R.id.AddLibrary_button_addLibrary).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.AddLibrary_button_addLibrary:
			if (addLibrary()) {
				Toast.makeText(this, getString(R.string.AddLibrary_toast_libraryAdded), Toast.LENGTH_SHORT).show();
				finish();
			} else {
				Toast.makeText(this, getString(R.string.AddLibrary_toast_libraryNameMissing), Toast.LENGTH_SHORT)
						.show();
			}
			break;
		}
	}

	/**
	 * Adds the library to the database.
	 * 
	 * @return return true
	 */
	private boolean addLibrary() {

		boolean isFirstLibrary = false;

		// get the text field that has the name
		EditText libraryName = (EditText) findViewById(R.id.AddLibrary_field_name);

		// We need this because android 2.2 and earlier doesn't have java.lang.String.isEmpty()
		// because pre Android 2.3 Android was built with JDK 1.5. Android 2.3+ is built with
		// JDK 1.6 which include the String.isEmpty() method. Android has helper class with
		// a similar function.
		// TODO update this when the project is upgraded to Android 2.3+
		if (TextUtils.isEmpty(libraryName.getText())) {
			return false;
		}

		// get all the libraries
		Cursor result = resolver.query(PapyrusContentProvider.Libraries.CONTENT_URI, null, null, null, null);

		// check if it's the first one
		if (result.getCount() == 0) {
			isFirstLibrary = true;
		}
		// close the cursor
		result.close();

		// create the query
		ContentValues values = new ContentValues();
		values.put(PapyrusContentProvider.Libraries.FIELD_NAME, libraryName.getText().toString());

		// insert the values and save the resulting uri
		Uri newLibrary = resolver.insert(PapyrusContentProvider.Libraries.CONTENT_URI, values);

		// if it's the first library set it as the default
		if (isFirstLibrary) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor prefEditor = pref.edit();
			prefEditor.putString(SettingsActivity.KEY_DEFAULT_LIBRARY, Long.toString(ContentUris.parseId(newLibrary)));
			prefEditor.commit();
		}

		return true;
	}

}
package ca.marcmeszaros.papyrus.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import ca.marcmeszaros.papyrus.BuildConfig;
import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;


public class SettingsFragment extends PreferenceFragment {

    public static final String DEFAULT_LIBRARY = "settings__default_library";
    public static final String VERSION = "settings__version";

    public static SettingsFragment getInstance() {
        return new SettingsFragment();
    }

    // Required empty public constructor
    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(BuildConfig.APPLICATION_ID);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
        addPreferencesFromResource(R.xml.settings);

        // get all libraries
        String[] columns = { PapyrusContentProvider.Libraries.FIELD_ID, PapyrusContentProvider.Libraries.FIELD_NAME };
        Cursor result = getActivity().getContentResolver().query(PapyrusContentProvider.Libraries.CONTENT_URI, columns, null, null, PapyrusContentProvider.Libraries.FIELD_NAME);

        // create the list arrays of the right size
        CharSequence[] entries = new CharSequence[result.getCount()];
        CharSequence[] entryValues = new CharSequence[result.getCount()];

        // populate the spinner
        for (int i = 0; i < result.getCount(); i++) {
            result.moveToNext();
            entries[i] = result.getString(result.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_NAME));
            entryValues[i] = result.getString(result.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_ID));
        }
        // close the cursor
        result.close();

        // set the list and associated values
        ListPreference defaultLibrary = (ListPreference) findPreference(DEFAULT_LIBRARY);
        defaultLibrary.setEntries(entries);
        defaultLibrary.setEntryValues(entryValues);

        // set the versionName
        getPreferenceScreen().findPreference(VERSION).setSummary(BuildConfig.VERSION_NAME);

    }

}

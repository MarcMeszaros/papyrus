package ca.marcmeszaros.papyrus.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.activities.SettingsActivity;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;


public class AddLibraryDialogFragment extends DialogFragment {

    // injected views
    @InjectView(R.id.name)
    EditText mLibraryNameTextView;

    public static AddLibraryDialogFragment getInstance() {
        return new AddLibraryDialogFragment();
    }

    public AddLibraryDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_library_dialog, null);
        ButterKnife.inject(this, view);

        // setup the click listener
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    mLibraryNameTextView.setError(null); // clear any errors

                    // only add the library if there is more than 1 char
                    if (mLibraryNameTextView.getText().length() >= 1) {
                        addLibrary(mLibraryNameTextView.getText().toString());
                    } else {
                        mLibraryNameTextView.setError(getString(R.string.fragment_add_library_dialog__name_too_short));
                        mLibraryNameTextView.requestFocus();
                    }
                }
            }
        };
        // set the positive and negative buttons
        builder.setCancelable(true);
        // TODO figure out how to display the title without having the soft keyboard hide the buttons
        builder.setTitle(R.string.fragment_add_library_dialog__title);
        builder.setPositiveButton(android.R.string.ok, clickListener);
        builder.setNegativeButton(android.R.string.cancel, clickListener);

        // return the dialog
        builder.setView(view);
        return builder.create();
    }

    /**
     * Adds the library to the database.
     *
     * @return return true
     */
    private boolean addLibrary(String libraryName) {
        if (TextUtils.isEmpty(libraryName))
            return false;

        // get all the libraries, check if it's the first one, close the cursor
        Cursor result = getActivity().getContentResolver().query(PapyrusContentProvider.Libraries.CONTENT_URI, null, null, null, null);
        boolean isFirstLibrary = (result.getCount() == 0);
        result.close();

        // create the query
        ContentValues values = new ContentValues();
        values.put(PapyrusContentProvider.Libraries.FIELD_NAME, libraryName);

        // insert the values and save the resulting uri
        Uri newLibrary = getActivity().getContentResolver().insert(PapyrusContentProvider.Libraries.CONTENT_URI, values);

        // if it's the first library set it as the default
        if (isFirstLibrary) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putString(SettingsFragment.DEFAULT_LIBRARY, Long.toString(ContentUris.parseId(newLibrary)));
            prefEditor.commit();
        }

        return true;
    }
}

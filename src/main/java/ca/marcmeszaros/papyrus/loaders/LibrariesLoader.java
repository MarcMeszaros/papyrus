package ca.marcmeszaros.papyrus.loaders;


import android.content.Context;
import android.content.CursorLoader;

import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;

public class LibrariesLoader extends CursorLoader {

    public LibrariesLoader(Context context) {
        this(context, null, null, null, null);
    }

    public LibrariesLoader(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, null, projection, selection, selectionArgs, sortOrder);
        setUri(PapyrusContentProvider.Libraries.CONTENT_URI);
    }

}

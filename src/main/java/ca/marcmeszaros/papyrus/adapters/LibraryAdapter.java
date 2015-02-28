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
package ca.marcmeszaros.papyrus.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;
import ca.marcmeszaros.papyrus.util.TNManager;
import timber.log.Timber;

public class LibraryAdapter extends CursorAdapter {

    OnClickListener mOnClickListener;

	public LibraryAdapter(Context context, Cursor c, OnClickListener clickListener) {
		super(context, c, false);
        mOnClickListener = clickListener;
	}

	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		TextView name = (TextView) view.findViewById(R.id.name);
        ImageButton delete = (ImageButton) view.findViewById(R.id.btn_delete);

		name.setText(cursor.getString(cursor.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_NAME)));
        delete.setTag(cursor.getLong(cursor.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_ID)));

        if (mOnClickListener != null)
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.onClick(v, (long) v.getTag());
                }
            });
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.list_item_library, parent, false);
		bindView(v, context, cursor);
		return v;
	}

    public interface OnClickListener {
        public void onClick(View v, long libraryId);
    }

}

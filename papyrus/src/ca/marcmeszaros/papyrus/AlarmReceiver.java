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
package ca.marcmeszaros.papyrus;

import ca.marcmeszaros.papyrus.browser.TabBrowser;
import ca.marcmeszaros.papyrus.database.Loan;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

public class AlarmReceiver extends BroadcastReceiver {

	private static int NOTIFICATION_ID = 1;

	@Override
    public void onReceive(Context context, Intent intent) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(R.drawable.icon, context.getString(R.string.Notification_teaser), System.currentTimeMillis());

		Intent notificationIntent = new Intent(context, TabBrowser.class);
		notificationIntent.putExtra("tab", 1);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, notificationIntent, 0);

		Bundle extras = intent.getExtras();
		Loan loan = extras.getParcelable("loan");
		String name = "";

		// retrieve contact information
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				int id = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts._ID));
				// we have a match, get the name, phone, and email
				if (loan.getContactID() == id) {
					// get name
					name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				}
			}
		}

		//here we get the title and description of our Notification
		notification.setLatestEventInfo(context, context.getString(R.string.Notification_title), context.getString(R.string.Notification_content, name), pendingIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		//notification.flags = Notification.FLAG_INSISTENT;
		//notification.defaults |= Notification.DEFAULT_SOUND;

		nm.notify(NOTIFICATION_ID++, notification);
	}

}

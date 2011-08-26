/*******************************************************************************
 * Copyright (c) 2011 - Marc Meszaros
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

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

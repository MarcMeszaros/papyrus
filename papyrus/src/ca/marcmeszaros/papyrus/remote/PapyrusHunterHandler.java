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
package ca.marcmeszaros.papyrus.remote;

import ca.marcmeszaros.papyrus.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class PapyrusHunterHandler extends Handler {
		
	Context context;
	ProgressDialog dialog;
	
	public PapyrusHunterHandler(Context context){
		this.context = context;
	}
	
	public void handleMessage(Message msg){
		switch (msg.what) {
		case -1:
			Log.i("network", "we got the message");
			dialog.cancel();		
			Toast.makeText(context, String.format(context.getString(R.string.PapyrusHunterHandler_toast_bookSaved), msg.obj.toString()), Toast.LENGTH_LONG).show();
			break;
		case 0:
			dialog.cancel();
			Toast.makeText(context, context.getString(R.string.PapyrusHunterHandler_toast_errorGetBookInfo), Toast.LENGTH_LONG).show();
			break;
		case 1:
			dialog.cancel();
			Toast.makeText(context, context.getString(R.string.PapyrusHunterHandler_toast_noBookInfo), Toast.LENGTH_LONG).show();
			break;
		}
		
	}
	
	public void setDialog(ProgressDialog dialog){
		this.dialog = dialog;
	}
	
}

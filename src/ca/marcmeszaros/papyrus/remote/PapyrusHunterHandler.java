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

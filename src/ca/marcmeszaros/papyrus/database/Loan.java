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

package ca.marcmeszaros.papyrus.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class: Loan
 * 
 *	@description: a class to loosely represent Loan objects.
 *		has variables for due date, loan date
 */
public class Loan implements Parcelable {
	
	/* 
	 * Class Variables
	 */
	private int loanID;
	private int bookID;
	private int contactID;
	private long dueDate;
	private long lendDate;

	/*
	 * Constructors
	 */
	public Loan() {
	}

	public Loan(long lendDate, long dueDate) {
		super();
		this.dueDate = dueDate;
		this.lendDate = lendDate;
	}
	
	public Loan(int loanID, int bookID, int contactID, long lendDate, long dueDate) {
		super();
		this.dueDate = dueDate;
		this.lendDate = lendDate;
		this.loanID = loanID;
		this.bookID = bookID;
		this.contactID =contactID;
	}
	
	/* 
	 * Getters
	 */	
	public long getLendDate() {
		return lendDate;
	}
	
	public long getDueDate() {
		return dueDate;
	}
	
	public int getLoanID() {
		return loanID;
	}
	
	public int getBookID() {
		return bookID;
	}
	
	public int getContactID() {
		return contactID;
	}
	
	/*
	 *  Parcelable
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(lendDate);
		dest.writeLong(dueDate);
		dest.writeInt(loanID);
		dest.writeInt(bookID);
		dest.writeInt(contactID);
	}

	public static final Parcelable.Creator<Loan> CREATOR = new Parcelable.Creator<Loan>() {
		public Loan createFromParcel(Parcel in) {
			return new Loan(in);
		}

		public Loan[] newArray(int size) {
			return new Loan[size];
		}
	};

	private Loan(Parcel in) {
		this.lendDate = in.readLong();
		this.dueDate = in.readLong();
		this.loanID = in.readInt();
		this.bookID = in.readInt();
		this.contactID = in.readInt();
	}
}

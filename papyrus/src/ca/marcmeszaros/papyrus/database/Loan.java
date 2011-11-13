/*******************************************************************************
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

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

package ca.marcmeszaros.papyrus.test;

import ca.marcmeszaros.papyrus.browser.BooksBrowser;
import android.test.ActivityInstrumentationTestCase2;

public class BooksBrowserTest extends ActivityInstrumentationTestCase2<BooksBrowser> {
    private BooksBrowser mActivity;  // the activity under test

    public BooksBrowserTest() {
    	super("ca.marcmeszaros.papyrus.browser", BooksBrowser.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
    }
    public void testPreconditions() {
    	assertNotNull(mActivity);
    }
}
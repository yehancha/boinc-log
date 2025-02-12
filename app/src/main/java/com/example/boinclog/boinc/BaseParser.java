/* 
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010, Pavol Michalec
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.example.boinclog.boinc;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class BaseParser extends DefaultHandler {

    protected StringBuilder mCurrentElement = new StringBuilder();
    protected boolean mElementStarted = false;
    protected boolean mUnauthorized = false;


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (mElementStarted) {
            // put it into StringBuilder
            if (mCurrentElement.length() == 0) {
                // still empty - trim leading white-spaces
                int newStart = start;
                int newLength = length;
                for ( ; newLength > 0; ++newStart, --newLength) {
                    if (!Character.isWhitespace(ch[newStart])) {
                        // First non-white-space character
                        mCurrentElement.append(ch, newStart, newLength);
                        break;
                    }
                }
            }
            else {
                // Non-empty - add everything
                mCurrentElement.append(ch, start, length);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equalsIgnoreCase("unauthorized")) {
            // We received <unauthorized/>
            mUnauthorized = true;
        }
    }

    protected void trimEnd() {
        int length = mCurrentElement.length();
        // Trim trailing spaces
        for (int i = length - 1; i >= 0; --i) {
            if (!Character.isWhitespace(mCurrentElement.charAt(i))) {
                // All trailing white-spaces are skipped, i is position of last character
                mCurrentElement.setLength(i+1);
                break;
            }
        }
    }
}

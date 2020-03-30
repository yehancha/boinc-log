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

import android.util.Log;
import android.util.Xml;

import com.example.boinclog.BuildConfig;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class VersionInfoParser extends BaseParser {
    private static final String TAG = "VersionInfoParser";
    private VersionInfo mVersionInfo = null;

    public final VersionInfo getVersionInfo() throws AuthorizationFailedException {
        if (mUnauthorized) throw new AuthorizationFailedException();
        // Null is also accepted (older clients do not support this operation)
        return mVersionInfo;
    }

    /**
     * Parse the RPC result (version_info)
     *
     * @param rpcResult String returned by RPC call of core client
     * @return VersionInfo (of core client)
     * @throws AuthorizationFailedException in case of unauthorized
     * @throws InvalidDataReceivedException in case XML cannot be parsed
     */
    public static VersionInfo parse(String rpcResult) throws AuthorizationFailedException, InvalidDataReceivedException {
        try {
            VersionInfoParser parser = new VersionInfoParser();
            Xml.parse(rpcResult, parser);
            return parser.getVersionInfo();
        }
        catch (SAXException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
            throw new InvalidDataReceivedException("Malformed XML while parsing <server_version>", e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("server_version")) {
            if (mVersionInfo != null) {
                // previous <server_version> not closed - dropping it!
                Log.i(TAG, "Dropping unfinished <server_version> data");
            }
            mVersionInfo = new VersionInfo();
        }
        else {
            // Another element, hopefully primitive and not constructor
            // (although unknown constructor does not hurt, because there will be primitive start anyway)
            mElementStarted = true;
            mCurrentElement.setLength(0);
        }
    }

    // Method characters(char[] ch, int start, int length) is implemented by BaseParser,
    // filling mCurrentElement (including stripping of leading whitespaces)
    //@Override
    //public void characters(char[] ch, int start, int length) throws SAXException { }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        try {
            if (mVersionInfo != null) {
                // we are inside <server_version>
                if (localName.equalsIgnoreCase("server_version")) {
                    // Closing tag of <server_version> - nothing to do at the moment
                }
                else {
                    // Not the closing tag - we decode possible inner tags
                    trimEnd();
                    if (localName.equalsIgnoreCase("major")) {
                        mVersionInfo.major = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("minor")) {
                        mVersionInfo.minor = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("release")) {
                        mVersionInfo.release = Integer.parseInt(mCurrentElement.toString());
                    }
                }
            }
        }
        catch (NumberFormatException e) {
            Log.i(TAG, "Exception when decoding " + localName);
        }
        mElementStarted = false; // to be clean for next one
    }
}

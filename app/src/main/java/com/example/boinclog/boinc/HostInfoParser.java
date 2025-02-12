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


public class HostInfoParser extends BaseParser {
    private static final String TAG = "HostInfoParser";

    private HostInfo mHostInfo = null;
    private boolean  mInCoprocX = false;

    public final HostInfo getHostInfo() throws AuthorizationFailedException, InvalidDataReceivedException {
        if (mUnauthorized) throw new AuthorizationFailedException();
        if (null == mHostInfo) throw new InvalidDataReceivedException();
        return mHostInfo;
    }

    /**
     * Parse the RPC result (host_info)
     *
     * @param rpcResult String returned by RPC call of core client
     * @return HostInfo
     * @throws AuthorizationFailedException in case of unauthorized
     * @throws InvalidDataReceivedException in case XML cannot be parsed
     *     or does not contain valid {@code <host_info>} tag
     */
    public static HostInfo parse(String rpcResult) throws AuthorizationFailedException, InvalidDataReceivedException {
        try {
            HostInfoParser parser = new HostInfoParser();
            Xml.parse(rpcResult, parser);
            return parser.getHostInfo();
        }
        catch (SAXException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Malformed XML:\n" + rpcResult);
            throw new InvalidDataReceivedException("Malformed XML while parsing <host_info>", e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (localName.equalsIgnoreCase("host_info")) {
            if (mHostInfo != null) {
                // previous <host_info> not closed - dropping it!
                Log.i(TAG, "Dropping unfinished <host_info> data");
            }
            mHostInfo = new HostInfo();
        }
        else if (localName.startsWith("coproc_")) {
            mInCoprocX = true;
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
            if (mHostInfo != null) {
                // we are inside <host_info>
                if (localName.equalsIgnoreCase("host_info")) {
                    // Closing tag of <host_info> - nothing to do at the moment
                }
                else if (mInCoprocX) {
                    if (localName.startsWith("coproc_")) {
                        mInCoprocX = false;
                    }
                    else if (localName.equalsIgnoreCase("count")) {
                        mHostInfo.g_ngpus += Integer.parseInt(mCurrentElement.toString());
                    }
                }
                else {
                    // Not the closing tag - we decode possible inner tags
                    trimEnd();
                    if (localName.equalsIgnoreCase("timezone")) {
                        mHostInfo.timezone = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("domain_name")) {
                        mHostInfo.domain_name = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("ip_addr")) {
                        mHostInfo.ip_addr = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("host_cpid")) {
                        mHostInfo.host_cpid = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("p_ncpus")) {
                        mHostInfo.p_ncpus = Integer.parseInt(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("p_vendor")) {
                        mHostInfo.p_vendor = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("p_model")) {
                        mHostInfo.p_model = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("p_features")) {
                        mHostInfo.p_features = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("p_fpops")) {
                        mHostInfo.p_fpops = Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("p_iops")) {
                        mHostInfo.p_iops = Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("p_membw")) {
                        mHostInfo.p_membw = Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("p_calculated")) {
                        mHostInfo.p_calculated = (long)Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("m_nbytes")) {
                        mHostInfo.m_nbytes = Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("m_cache")) {
                        mHostInfo.m_cache = Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("m_swap")) {
                        mHostInfo.m_swap = Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("d_total")) {
                        mHostInfo.d_total = Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("d_free")) {
                        mHostInfo.d_free = Double.parseDouble(mCurrentElement.toString());
                    }
                    else if (localName.equalsIgnoreCase("os_name")) {
                        mHostInfo.os_name = mCurrentElement.toString();
                    }
                    else if (localName.equalsIgnoreCase("os_version")) {
                        mHostInfo.os_version = mCurrentElement.toString();
                    }
                }
            }
        }
        catch (NumberFormatException e) {
            Log.i(TAG, "Exception when decoding " + localName);
        }
        mElementStarted = false;
    }
}

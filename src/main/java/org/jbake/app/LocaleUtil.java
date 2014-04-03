/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jbake.app;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides locale utilities
 * 
 * @author asemelit
 */
public class LocaleUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(LocaleUtil.class);
    
    public final static String LOCALE_SEPARATOR = "_";
    
    public static String getPathLocalePart(String locale) {
        return locale != null ? "/" + locale : "";
    }
    
    public static String getLocaleSuffix(String locale) {
        return locale != null ? locale + LOCALE_SEPARATOR : "";
    }

    public static String stripLocaleSuffix(String stringToStrip, String locale) {
        LOGGER.info("Stripping {} from {}", getLocaleSuffix(locale), stringToStrip);
        return locale != null ? stringToStrip.replace(getLocaleSuffix(locale), "") : stringToStrip;
    }
    
    public static String getLocaleFromDocType(String docType) {
        return docType.contains(LOCALE_SEPARATOR) ? docType.substring(0, docType.indexOf(LOCALE_SEPARATOR)) : null ; //assuming convention for locales like en_;ru_ etc. prefix of a docType
    }

    public static String getLocaleFromUri(String uri) {
        LOGGER.info("Looking for locale candidate in {}", uri);
        if (uri.contains(LOCALE_SEPARATOR)) {
            int lastBackSlash = uri.lastIndexOf("/") + 1;
            int firstLocaleSeparator = uri.indexOf(LOCALE_SEPARATOR, lastBackSlash);
            return firstLocaleSeparator > -1 ? uri.substring(lastBackSlash, firstLocaleSeparator) : null ; //assuming convention for locales like en_;ru_ etc. prefix of a docType
        }
        else {
            return null;
        }
    }

}

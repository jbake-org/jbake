package org.jbake.util;

import java.net.URI;
import java.net.URISyntaxException;

public class PagingHelper {
    private static final String URI_SEPARATOR = "/";
    private long totalDocuments;
    private int postsPerPage;

    public PagingHelper(long totalDocuments, int postsPerPage) {
        this.totalDocuments = totalDocuments;
        this.postsPerPage = postsPerPage;
    }

    public int getNumberOfPages() {
        return (int) Math.ceil((totalDocuments * 1.0) / (postsPerPage * 1.0));
    }

    public String getNextFileName(int currentPageNumber) throws URISyntaxException {
        if (currentPageNumber < getNumberOfPages()) {
            return new URI((currentPageNumber + 1) + URI_SEPARATOR).toString();
        } else {
            return null;
        }
    }

    public String getPreviousFileName(int currentPageNumber) throws URISyntaxException {

        if (isFirstPage(currentPageNumber)) {
            return null;
        } else {
            if ( currentPageNumber == 2 ) {
                // Returning to first page, return empty string which when prefixed with content.rootpath should get to root of the site.
                return "";
            }
            else {
                return new URI((currentPageNumber - 1) + URI_SEPARATOR).toString();
            }
        }
    }

    private boolean isFirstPage(int page) {
        return page == 1;
    }

    public String getCurrentFileName(int page, String fileName) throws URISyntaxException {
        if ( isFirstPage(page) ) {
            return fileName;
        }
        else {
            return new URI(page + URI_SEPARATOR + fileName).toString();
        }
    }

}

package org.jbake.util;

import java.io.File;

public class PagingHelper {
    long totalDocuments;
    int postsPerPage;

    public PagingHelper(long totalDocuments, int postsPerPage) {
        this.totalDocuments = totalDocuments;
        this.postsPerPage = postsPerPage;
    }

    public int getNumberOfPages() {
        return (int) Math.ceil((totalDocuments * 1.0) / (postsPerPage * 1.0));
    }

    public String getNextFileName(int currentPageNumber, String fileName) {
        if (currentPageNumber < getNumberOfPages()) {
            return (currentPageNumber + 1) + File.separator;
        } else {
            return null;
        }
    }

    public String getPreviousFileName(int currentPageNumber, String fileName) {

        if (isFirstPage(currentPageNumber)) {
            return null;
        } else {
            if ( currentPageNumber == 2 ) {
            	// Returning to first page, return empty string which when prefixed with content.rootpath should get to root of the site.
                return "";
            }
            else {
                return (currentPageNumber - 1) + File.separator;
            }
        }
    }

    private boolean isFirstPage(int page) {
        return page == 1;
    }

    public String getCurrentFileName(int page, String fileName) {
        if ( isFirstPage(page) ) {
            return fileName;
        }
        else {
            return page + File.separator + fileName;
        }
    }

}

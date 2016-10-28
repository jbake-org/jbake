package org.jbake.util;

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
            int index = fileName.lastIndexOf(".");
            return fileName.substring(0, index) + (currentPageNumber + 1) +
                    fileName.substring(index);
        } else {
            return null;
        }
    }

    public String getPreviousFileName(int currentPageNumber, String fileName) {

        if (isFirstPage(currentPageNumber)) {
            return null;
        } else {
            int index = fileName.lastIndexOf(".");
            return fileName.substring(0, index) + (currentPageNumber > 2 ? currentPageNumber - 1 : "") +
                    fileName.substring(index);
        }
    }

    private boolean isFirstPage(int page) {
        return page == 1;
    }

    public String getCurrentFileName(int page, String fileName) {
        int index = fileName.lastIndexOf(".");
        return fileName.substring(0, index) + (page > 1 ? page : "") +
                fileName.substring(index);
    }

}

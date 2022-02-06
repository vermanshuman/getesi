package it.nexera.ris.web.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListPaginator {
    private Integer rowCount;
    private Integer rowsPerPage;
    private Integer totalPages;
    private String pageNavigationStart;
    private String pageNavigationEnd;
    private String paginatorString;
    private Integer currentPageNumber;
    private Integer tablePage;
    private String tableSortOrder;
    private String tableSortColumn;

    public ListPaginator(int rowsPerPage, Integer totalPages, Integer currentPageNumber, Integer tablePage,
                         String tableSortOrder, String tableSortColumn) {
        this.rowsPerPage =  rowsPerPage;
        this.totalPages = totalPages;
        this.currentPageNumber = currentPageNumber;
        this.tablePage = tablePage;
        this.tableSortOrder = tableSortOrder;
        this.tableSortColumn = tableSortColumn;

        StringBuilder builder = new StringBuilder();
        builder.append("<a href=\"#\" class=\"ui-paginator-first ui-state-default ui-corner-all ui-state-disabled");
        builder.append(" tabindex=\"-1\">\n");
        builder.append("<span class=\"ui-icon ui-icon-seek-first\">F</span>\n</a>\n");
        builder.append("<a href=\"#\" onclick=\"previousPage(event)\"");
        builder.append(" class=\"ui-paginator-prev ui-corner-all ui-state-disabled\" tabindex=\"-1\">\n");
        builder.append("<span class=\"ui-icon ui-icon-seek-prev\">P</span>\n</a>\n");
        setPageNavigationStart(builder.toString());
        builder.setLength(0);
        builder.append("<a href=\"#\" class=\"ui-paginator-next ui-state-default ui-corner-all\"  onclick=\"nextPage(event)\"");
        builder.append(" tabindex=\"0\">\n");
        builder.append("<span class=\"ui-icon ui-icon-seek-next\">N</span>\n</a>\n");
        builder.append("<a href=\"#\"");
        builder.append(" class=\"ui-paginator-last ui-state-default ui-corner-all\" tabindex=\"-1\" onclick=\"lastPage(event)\">\n");
        builder.append("<span class=\"ui-icon ui-icon-seek-end\">E</span>\n</a>\n");
        setPageNavigationEnd(builder.toString());
    }

    public void setPage(Integer currentPageNumber){
        StringBuilder builder = new StringBuilder();
        if(currentPageNumber == getTotalPages()){
            builder.setLength(0);
            Integer pageEnd = getTotalPages();
            Integer pageStart = getTotalPages() > 10 ? (getTotalPages() - 10) : 1;
            for(int i = pageStart; i <= pageEnd;i++ ){
                builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
                builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ",event)\">" + i +"</a>");
            }
        }else if(currentPageNumber > 10 && currentPageNumber != null && (currentPageNumber -1)% 10 == 0){
            builder.setLength(0);
            Integer pageEnd = currentPageNumber+9;
            if(pageEnd > getTotalPages())
                pageEnd = getTotalPages();
            for(int i = currentPageNumber; i <= pageEnd;i++ ){
                builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
                builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ",event)\">" + i +"</a>");
            }
        }else {
            int pageStart = 1;
            if(currentPageNumber > 10){
                if(currentPageNumber %10 == 0){
                    pageStart = 10*((currentPageNumber-1)/10) + 1;
                }else {
                    pageStart = 10*(currentPageNumber/10) + 1;
                }
            }

            int page;

            if((pageStart + 10) > getTotalPages())
                page = getTotalPages();
            else
                page = pageStart + 9;

            for(int i = pageStart; i <=page;i++ ){
                builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
                builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ",event)\">" + i +"</a>");
            }

        }
        setPaginatorString(builder.toString());
        System.out.println(getPaginatorString());
    }

}

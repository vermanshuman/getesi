package it.nexera.ris.web.beans.pages;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "rowValue")
@SessionScoped
public class RowPageBean {

	private int rowsPerPage = 20;
	
	 public int getRowsPerPage() {
			return rowsPerPage;
		}
		public void setRowsPerPage(int rowsPerPage) {
			this.rowsPerPage = rowsPerPage;
		}
}

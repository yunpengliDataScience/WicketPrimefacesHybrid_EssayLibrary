package com.library.essay.reports.dataSource;

import java.io.Serializable;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;

public abstract class JasperReportAbstractDataSource<T> implements
		JRDataSource, Serializable {

	private String reportTitle;
	private List<T> reportObjects;
	private int index = -1;

	public JasperReportAbstractDataSource(String reportTitle,
			List<T> reportObjects) {
		this.reportTitle = reportTitle;
		this.reportObjects = reportObjects;
	}

	@Override
	public boolean next() throws JRException {
		index++;

		boolean hasNext = (index < reportObjects.size());
		if (!hasNext) {
			index = -1;
		}
		return hasNext;
	}

	public String getReportTitle() {
		return reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

	public List<T> getReportObjects() {
		return reportObjects;
	}

	public void setReportObjects(List<T> reportObjects) {
		this.reportObjects = reportObjects;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}

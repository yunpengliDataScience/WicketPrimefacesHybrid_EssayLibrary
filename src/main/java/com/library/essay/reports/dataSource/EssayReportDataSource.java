package com.library.essay.reports.dataSource;

import java.util.List;
import com.library.essay.persistence.entities.Essay;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class EssayReportDataSource extends
		JasperReportAbstractDataSource<Essay> {

	private static final long serialVersionUID = 1L;

	public EssayReportDataSource(String reportTitle, List<Essay> essayList) {
		super(reportTitle, essayList);
	}

	@Override
	public Object getFieldValue(JRField jrField) throws JRException {
		Object value = null;

		String fieldName = jrField.getName();

		Essay essay = this.getReportObjects().get(this.getIndex());

		if ("id".equals(fieldName)) {
			value = essay.getId();
		} else if ("title".equals(fieldName)) {
			value = essay.getTitle();
		} else if ("author".equals(fieldName)) {
			value = essay.getAuthor();
		} else if ("content".equals(fieldName)) {
			value = essay.getContent();
		} else if ("reportTitle".equals(fieldName)) {
			value = this.getReportTitle();
		}

		return value;
	}

}

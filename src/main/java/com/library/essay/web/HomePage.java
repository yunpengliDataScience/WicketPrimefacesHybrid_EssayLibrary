package com.library.essay.web;

import java.util.List;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.library.essay.persistence.entities.Essay;
import com.library.essay.reports.dataSource.EssayReportDataSource;
import com.library.essay.reports.resource.JasperReportGenerationResource;
import com.library.essay.reports.wicket.component.ReportPopUpLink;
import com.library.essay.services.EssayService;

public class HomePage extends WebPage {

	// private static final long serialVersionUID = -7665112860696096485L;
	private static final Logger log = LoggerFactory.getLogger(HomePage.class);

	@SpringBean
	EssayService essayService;

	public HomePage() {

		add(new Label("message", "Welcome to Essay Library!"));

		addEssayListLink("essayListLink");

		addEssayListReportLink("report");
	}

	private void addEssayListLink(String id) {
		Link<Void> link = new Link<Void>(id) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(EssayListPage.class);
			}
		};

		add(link);
	}

	private void addEssayListReportLink(String id) {
		ReportPopUpLink<Essay> reportLink = new ReportPopUpLink<Essay>(id, null) {

			private static final long serialVersionUID = -1L;

			@Override
			protected IResource getReportGenerationResource() {

				List<Essay> essayList = essayService.getEssays();

				EssayReportDataSource essayReportDataSource = new EssayReportDataSource(
						"Essay List Report", essayList);

				JasperReportGenerationResource<Essay> reportGenerationResource = new JasperReportGenerationResource<Essay>(
						"essayReport.jrxml", "application/pdf",
						essayReportDataSource);

				return reportGenerationResource;
			}
		};

		add(reportLink);
	}

}

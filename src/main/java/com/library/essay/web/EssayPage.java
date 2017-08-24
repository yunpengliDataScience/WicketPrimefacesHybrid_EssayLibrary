package com.library.essay.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.contrib.tinymce.TinyMceBehavior;
import wicket.contrib.tinymce.settings.TinyMCESettings;

import com.library.essay.persistence.entities.Essay;
import com.library.essay.reports.dataSource.EssayReportDataSource;
import com.library.essay.reports.resource.ITextPdfGenerationResource;
import com.library.essay.reports.resource.JasperReportGenerationResource;
import com.library.essay.reports.wicket.component.ReportPopUpLink;
import com.library.essay.services.EssayService;

public class EssayPage extends WebPage {

	private static final Logger logger = LoggerFactory
			.getLogger(EssayPage.class);

	@SpringBean
	private EssayService essayService;

	public EssayPage(){}
	
	public EssayPage(Essay essay) {

		addHomeLink("home");
		addEssayListLink("essayListLink");

		add(new FeedbackPanel("feedback"));
		addEssayForm("essayForm", essay);
	}

	private void addHomeLink(String id) {
		Link<Void> link = new Link<Void>(id) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(HomePage.class);
			}
		};

		add(link);
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

	private void addEssayForm(String id, Essay essay) {

		Essay currentEssay = essay;

		if (currentEssay == null) {
			currentEssay = new Essay();
		} else { // Retrieve from database
			Long essayId = essay.getId();

			if (essayId != null && essayId > 0) {
				currentEssay = essayService.getEssay(essayId);
			}
		}

		CompoundPropertyModel<Essay> essayCompoundPropertyModel = new CompoundPropertyModel<Essay>(
				currentEssay);

		final Form<Essay> essayForm = new Form<Essay>(id,
				essayCompoundPropertyModel) {

			private static final long serialVersionUID = 1L;

			private Essay essay = this.getModelObject();

			@Override
			protected void onSubmit() {

				logger.debug("==================================================================");
				logger.debug("Form is submitted!!!!!!!!!!!!!!!!!!");
				logger.debug("==================================================================");

				essayService.saveOrUpdate(essay);

				setResponsePage(new EssayListPage());
			}
		};

		addFieldsToForm(essayForm);

		addSaveButtonToForm(essayForm);

		addDeleteButtonToForm(essayForm);

		addReportButtonToForm(essayForm, "essayReport");

		addReportButtonToFormUsingResourceLink(essayForm, "essayReport2");

		addItextReportButtonToForm(essayForm, "iTextEssayReport");

		add(essayForm);
	}

	/**
	 * ReportPopUpLink customized approach
	 * 
	 * @param essayForm
	 * @param id
	 */
	private void addReportButtonToForm(final Form<Essay> essayForm, String id) {

		ReportPopUpLink<Essay> reportLink = new ReportPopUpLink<Essay>(id, null) {

			private static final long serialVersionUID = -1L;

			@Override
			protected IResource getReportGenerationResource() {
				Essay essay = essayForm.getModelObject();

				List<Essay> essayList = new ArrayList<Essay>();
				essayList.add(essay);

				EssayReportDataSource essayReportDataSource = new EssayReportDataSource(
						"Essay Report", essayList);

				JasperReportGenerationResource<Essay> reportGenerationResource = new JasperReportGenerationResource<Essay>(
						"essayReport.jrxml", "application/pdf",
						essayReportDataSource);

				return reportGenerationResource;
			}
		};

		essayForm.add(reportLink);
	}

	/**
	 * ResourceLink approach
	 * 
	 * @param essayForm
	 * @param id
	 */
	private void addReportButtonToFormUsingResourceLink(
			final Form<Essay> essayForm, String id) {

		Essay essay = essayForm.getModelObject();

		List<Essay> essayList = new ArrayList<Essay>();
		essayList.add(essay);

		EssayReportDataSource essayReportDataSource = new EssayReportDataSource(
				"Essay Report", essayList);

		JasperReportGenerationResource<Essay> reportGenerationResource = new JasperReportGenerationResource<Essay>(
				"essayReport.jrxml", "application/pdf", essayReportDataSource);

		ResourceLink<Void> resourceLink = new ResourceLink<Void>(id,
				reportGenerationResource);

		resourceLink.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE
				| PopupSettings.SCROLLBARS));

		essayForm.add(resourceLink);
	}

	private void addItextReportButtonToForm(final Form<Essay> essayForm,
			String id) {

		Essay essay = essayForm.getModelObject();

		ITextPdfGenerationResource reportGenerationResource = new ITextPdfGenerationResource(
				"richTextTemplate.vm", "application/pdf", essay);

		ResourceLink<Void> resourceLink = new ResourceLink<Void>(id,
				reportGenerationResource);

		resourceLink.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE
				| PopupSettings.SCROLLBARS));

		essayForm.add(resourceLink);
	}

	private void addDeleteButtonToForm(final Form<Essay> essayForm) {
		// Delete button
		Button deleteButton = new Button("delete", Model.of("Delete")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {

				logger.debug("==================================================================");
				logger.debug("Delete Button is clicked!!!!!!!!!!!!!!!!!!");
				logger.debug("==================================================================");

				Essay essay = essayForm.getModelObject();

				essayService.delete(essay);

				setResponsePage(new EssayListPage());
			}
		};
		deleteButton.setDefaultFormProcessing(false);
		essayForm.add(deleteButton);
	}

	private void addSaveButtonToForm(final Form<Essay> essayForm) {
		// Save button
		Button saveButton = new Button("save", Model.of("Save")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				logger.debug("==================================================================");
				logger.debug("Save Button is clicked!!!!!!!!!!!!!!!!!!");
				logger.debug("==================================================================");

			}
		};
		saveButton.setDefaultFormProcessing(true);
		essayForm.add(saveButton);
	}

	private void addFieldsToForm(final Form<Essay> essayForm) {
		TextField<String> titleField = new TextField<String>("title");
		titleField.setRequired(true);

		TextField<String> authorField = new TextField<String>("author");
		authorField.setRequired(true);

		TextArea<String> contentField = new TextArea<String>("content");
		contentField.setRequired(true);

		addRichTextEditor(contentField);

		essayForm.add(titleField);
		essayForm.add(authorField);
		essayForm.add(contentField);
	}

	private void addRichTextEditor(TextArea<String> contentField) {
		TinyMCESettings settings = new TinyMCESettings(
				TinyMCESettings.Theme.advanced);

		// first toolbar
		settings.add(wicket.contrib.tinymce.settings.Button.newdocument,
				TinyMCESettings.Toolbar.first, TinyMCESettings.Position.before);
		settings.add(wicket.contrib.tinymce.settings.Button.separator,
				TinyMCESettings.Toolbar.first, TinyMCESettings.Position.before);
		settings.add(wicket.contrib.tinymce.settings.Button.fontselect,
				TinyMCESettings.Toolbar.first, TinyMCESettings.Position.after);

		// other settings
		settings.setToolbarAlign(TinyMCESettings.Align.left);
		settings.setToolbarLocation(TinyMCESettings.Location.top);
		settings.setStatusbarLocation(TinyMCESettings.Location.bottom);
		settings.setResizing(true);

		settings.addCustomSetting("theme_advanced_buttons3_add : \"spellchecker\"");
		settings.addCustomSetting("selector: \"textarea.jazzy\"");
		settings.addCustomSetting("plugins: \"spellchecker\"");
		settings.addCustomSetting("spellchecker_languages : \"+English=en-us\"");

		String contextPath = getRequestCycle().getRequest().getContextPath();

		settings.addCustomSetting("spellchecker_rpc_url : \"" + contextPath
				+ "/servlet/jazzy-spellchecker\"");
		contentField.add(new TinyMceBehavior(settings));
	}

}

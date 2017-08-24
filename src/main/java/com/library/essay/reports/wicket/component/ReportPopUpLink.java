package com.library.essay.reports.wicket.component;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.IResource.Attributes;

import com.library.essay.reports.dataSource.JasperReportAbstractDataSource;
import com.library.essay.reports.resource.JasperReportGenerationResource;

/**
 * A Jasper report popup link, which is similar to ResourceLink.
 * 
 * @author yunpeng.li
 * 
 * @param <T>
 */
public class ReportPopUpLink<T> extends Link<T> implements IResourceListener {

	private static final long serialVersionUID = 1L;
	private IResource resource;

	public ReportPopUpLink(String id, IResource resource,
			PopupSettings popupSettings) {
		super(id);
		this.resource = resource;

		PopupSettings settings = popupSettings;

		if (settings == null) {
			settings = new PopupSettings(PopupSettings.RESIZABLE
					| PopupSettings.SCROLLBARS) {

				private static final long serialVersionUID = 1L;

				@Override
				public String getPopupJavaScript() {
					return super.getPopupJavaScript().replace(
							"if(w.blur) w.focus();", "");
				}
			};
			settings.setWidth(1000);
		}
		setPopupSettings(settings);
	}

	public ReportPopUpLink(String id, PopupSettings popupSettings) {
		this(id, null, popupSettings);
	}

	public ReportPopUpLink(String id, String reportTemplate,
			String contentType, JasperReportAbstractDataSource<T> dataSource,
			PopupSettings popupSettings) {
		this(id, new JasperReportGenerationResource<T>(reportTemplate,
				contentType, dataSource), popupSettings);
	}

	@Override
	public final void onResourceRequested() {
		resource = getReportGenerationResource();
		Attributes a = new Attributes(RequestCycle.get().getRequest(),
				RequestCycle.get().getResponse(), null);
		resource.respond(a);
	}

	protected IResource getReportGenerationResource() {
		return resource;
	}

	@Override
	public void onClick() {

	}

	@Override
	protected final CharSequence getURL() {
		return urlFor(IResourceListener.INTERFACE, new PageParameters());
	}
}

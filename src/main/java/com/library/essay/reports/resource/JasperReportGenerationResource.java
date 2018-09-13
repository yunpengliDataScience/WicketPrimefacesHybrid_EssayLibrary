package com.library.essay.reports.resource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.time.Duration;

import com.library.essay.reports.dataSource.JasperReportAbstractDataSource;

/**
 * This is used with ReportLink to generate reports. It implements IResource.
 * @param <T> 
 */
public class JasperReportGenerationResource<T> extends AbstractResource {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger
			.getLogger(JasperReportGenerationResource.class);

	private String reportTemplate;

	private String contentType;
	private JasperReportAbstractDataSource<T> dataSource;

	public JasperReportGenerationResource(String reportTemplate,
			String contentType, JasperReportAbstractDataSource<T> dataSource) {

		this.reportTemplate = reportTemplate;
		this.contentType = contentType;

		this.dataSource = dataSource;
	}

	protected JRAbstractExporter getExporter() {

		JRAbstractExporter exporter = null;
		if ("text/html".equals(contentType)) {
			exporter = new JRXhtmlExporter();
		} else {
			// Default is pdf
			exporter = new JRPdfExporter();
		}

		return exporter;
	}

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		ResourceResponse resourceResponse = new ResourceResponse();
		resourceResponse.setContentType(contentType);

		resourceResponse.setContentDisposition(ContentDisposition.INLINE);
		// This is a weird solution, but ok for now. Actual caching will
		// prevent this from working properly and no caching causes weird
		// errors.
		resourceResponse.setCacheDuration(Duration.ONE_SECOND);
		resourceResponse.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) {
				try {

					long start = System.currentTimeMillis();

					Response response = attributes.getResponse();

					JasperPrint print = getFilledJasperPrint();
					JRAbstractExporter exporter = getExporter();
					exporter.setParameter(JRExporterParameter.JASPER_PRINT,
							print);
					exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
							response.getOutputStream());

					System.out.println("Start exporting report...");
					exporter.exportReport();

					long end = System.currentTimeMillis();

					System.out.println("Generating report takes time : "
							+ ((end - start) / 1000) + " seconds.");
					logger.info("Generating report takes time : "
							+ ((end - start) / 1000) + " seconds.");

				} catch (JRException e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
				}
			}
		});
		return resourceResponse;
	}

	private JasperPrint getFilledJasperPrint() throws JRException {
		ServletContext context = ((WebApplication) Application.get())
				.getServletContext();
		final File templateFile = new File(context.getRealPath("/reports/"
				+ reportTemplate));
		final String subreportDir = context.getRealPath("/reports/");

		JasperPrint print = null;
		JasperDesign design;
		JasperReport report = null;

		try {

			design = JRXmlLoader.load(templateFile);

			report = JasperCompileManager.compileReport(design);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("SUBREPORT_DIR", subreportDir + "/");

			// Use file swap and JRSwapFileVirtualizer to improve big report
			// generation performance
			// and reduce chance of out-of-memory error.

			String swapFileDir = context.getRealPath("/temp/fileSwap/");
			JRSwapFile swapFile = new JRSwapFile(swapFileDir, 2048, 1024);
			JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(30,
					swapFile, true);

			params.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			System.out.println("Start filling report...");
			print = JasperFillManager.fillReport(report, params, dataSource);

		} catch (JRException e) {
			logger.error(e.getMessage(), e);
		}
		return print;
	}
}
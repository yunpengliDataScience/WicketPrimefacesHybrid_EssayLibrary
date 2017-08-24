package com.library.essay.reports.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.time.Duration;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.library.essay.persistence.entities.Essay;

public class ITextPdfGenerationResource extends AbstractResource {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger
			.getLogger(JasperReportGenerationResource.class);

	private String velocityTemplateName;
	private Essay essay;
	private String contentType;

	public ITextPdfGenerationResource(String velocityTemplateName,
			String contentType, Essay essay) {

		this.velocityTemplateName = velocityTemplateName;
		this.contentType = contentType;
		this.essay = essay;
	}

	private void constructHtml(Writer writer) {

		// First, initialize
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		ve.init();

		// Replace parameters with actual data
		VelocityContext context = new VelocityContext();

		context.put("TITLE", essay.getTitle());
		context.put("AUTHOR", essay.getAuthor());
		context.put("CONTENT", essay.getContent());

		// Get the Template
		Template t = ve.getTemplate(velocityTemplateName);

		// Render the template into a Writer
		// StringWriter writer = new StringWriter();
		t.merge(context, writer);

		try {
			writer.close();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}

	}

	private void generatePdf(OutputStream outputStream, String htmlFilePath)
			throws DocumentException, IOException {

		// step 1
		Document document = new Document();
		// step 2
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);
		// step 3
		document.open();
		// document.add( new Chunk("testing"));
		// step 4
		XMLWorkerHelper.getInstance().parseXHtml(writer, document,
				new FileInputStream(htmlFilePath));
		// step 5
		document.close();

		writer.close();
		System.out.println("PDF Created!");
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

				long start = System.currentTimeMillis();

				Response response = attributes.getResponse();

				System.out.println("Start exporting report...");

				// Exporting

				ServletContext servletContext = WebApplication.get()
						.getServletContext();

				String htmlFilePath = servletContext.getRealPath(File.separator
						+ "generatedHtmls" + File.separator
						+ "essayReport.html");

				try {
					Writer writer = new FileWriter(new File(htmlFilePath));

					// TODO
					//String text = "<h2>Human Evolution</h2> <img src=\"http://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Ape_skeletons.png/500px-Ape_skeletons.png\" alt=\"Mountain View\" style=\"width:304px;height:228px\"></img>";

					constructHtml(writer);

					generatePdf(response.getOutputStream(), htmlFilePath);

				} catch (IOException e) {
					logger.error(e);
					e.printStackTrace();
				} catch (DocumentException e) {
					logger.error(e);
					e.printStackTrace();
				}

				long end = System.currentTimeMillis();

				System.out.println("Generating report takes time : "
						+ ((end - start) / 1000) + " seconds.");
				logger.info("Generating report takes time : "
						+ ((end - start) / 1000) + " seconds.");

			}
		});
		return resourceResponse;
	}
}

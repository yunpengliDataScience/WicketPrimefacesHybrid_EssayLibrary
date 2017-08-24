package com.library.essay.web.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.library.essay.persistence.entities.Essay;
import com.library.essay.web.EssayPage;


public class EssayIdLinkPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public EssayIdLinkPanel(String id, IModel<Essay> model) {
		super(id, model);

		Link<Void> link = new Link<Void>("essayIdLink") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				Essay essay = (Essay) EssayIdLinkPanel.this
						.getDefaultModelObject();

				setResponsePage(new EssayPage(essay));
			}
		};

		add(link);

		//Add the id in the panel
		link.add(new Label("id", model.getObject().getId()));
		
	}

}

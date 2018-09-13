package com.library.essay.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.library.essay.persistence.entities.Essay;
import com.library.essay.services.EssayService;
import com.library.essay.utils.MyUtil;
import com.library.essay.web.components.EssayIdLinkPanel;
import com.library.essay.wicket.models.ReloadingEssayModel;

public class EssayListPage extends WebPage {

	@SpringBean
	private EssayService essayService;

	private final Logger logger = LoggerFactory.getLogger(EssayListPage.class);

	public EssayListPage() {

		addHomeLink("home");
		addEssayListDataTable("essayListDataTable");
		addCreateEssayLink("createNew");

		MyUtil.exploreHttpServeletRequest(logger);
	}

	private void addCreateEssayLink(String id) {
		Link<Void> link = new Link<Void>(id) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new EssayPage(null));
			}
		};

		add(link);
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

	private void addEssayListDataTable(String id) {

		ISortableDataProvider<Essay, String> employeeDataProvider = new SortableDataProvider<Essay, String>() {

			private static final long serialVersionUID = 1L;

			public Iterator<? extends Essay> iterator(long first, long count) {

				String sortProperty = "id";
				boolean isAsc = true;

				SortParam<String> sortParam = getSort();

				if (sortParam != null) {

					sortProperty = sortParam.getProperty();
					isAsc = sortParam.isAscending();

					if (sortProperty == null) {
						sortProperty = "id";
					}
				}

				return essayService.getEssays((int) first / (int) count, (int) count, sortProperty, isAsc).iterator();
			}

			public long size() {

				return essayService.getEssays().size();
			}

			public IModel<Essay> model(Essay object) {

				return new ReloadingEssayModel(essayService, object);
			}

		};

		List<IColumn<Essay, String>> columns = new ArrayList<IColumn<Essay, String>>();

		// Add link to the id
		columns.add(new AbstractColumn<Essay, String>(new Model<String>("ID"), "id") {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<Essay>> cellItem, String componentId, IModel<Essay> rowModel) {
				cellItem.add(new EssayIdLinkPanel(componentId, rowModel));

			}

		});

		columns.add(new PropertyColumn<Essay, String>(new Model<String>("Title"), "title", "title"));
		columns.add(new PropertyColumn<Essay, String>(new Model<String>("Author"), "author", "author"));

		DefaultDataTable<Essay, String> dt = new DefaultDataTable<Essay, String>(id, columns, employeeDataProvider, 10);
		add(dt);
	}
}

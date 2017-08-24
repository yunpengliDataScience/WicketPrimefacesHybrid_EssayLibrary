package com.library.essay.wicket.models;

import org.apache.wicket.model.LoadableDetachableModel;

import com.library.essay.persistence.entities.Essay;
import com.library.essay.services.EssayService;

public class ReloadingEssayModel extends LoadableDetachableModel<Essay> {

	private static final long serialVersionUID = 1L;

	private EssayService essayService;
	private Essay essay;

	public ReloadingEssayModel(EssayService essayService, Essay essay) {
		super(essay);
		this.essayService = essayService;
		this.essay = essay;

	}

	@Override
	protected Essay load() {
		return essayService.getEssay(essay.getId());
	}
}
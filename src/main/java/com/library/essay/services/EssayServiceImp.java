package com.library.essay.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.library.essay.persistence.entities.Essay;
import com.library.essay.persistence.repositories.EssayRepository;

@Service(value = "essayService")
@Transactional
public class EssayServiceImp implements EssayService {

	@Autowired
	private EssayRepository essayRepository;

	@Override
	public Essay getEssay(long id) {

		return essayRepository.findOne(id);
	}

	@Override
	public List<Essay> getEssays() {

		return essayRepository.findAll();
	}

	@Override
	public List<Essay> getEssays(String sortProperty, boolean isAsc) {

		Direction direction = null;

		if (isAsc) {
			direction = Sort.Direction.ASC;
		} else {
			direction = Sort.Direction.DESC;
		}

		Sort sort = new Sort(new Sort.Order(direction, sortProperty));

		return essayRepository.findAll(sort);

	}

	@Override
	public List<Essay> getEssays(int pageIndex, int pageSize,
			String sortProperty, boolean isAsc) {

		Pageable pageSpecification = buildPageSpecification(pageIndex,
				pageSize, sortProperty, isAsc);

		Page<Essay> page = essayRepository.findAll(pageSpecification);

		return page.getContent();
	}

	// Pagination
	private Pageable buildPageSpecification(int pageIndex, int pageSize,
			String sortProperty, boolean isAsc) {

		Sort sortSpec = getSort(sortProperty, isAsc);

		return new PageRequest(pageIndex, pageSize, sortSpec);
	}

	private Sort getSort(String sortProperty, boolean isAsc) {
		Direction direction = null;

		if (isAsc) {
			direction = Sort.Direction.ASC;
		} else {
			direction = Sort.Direction.DESC;
		}

		Sort sort = new Sort(new Sort.Order(direction, sortProperty));

		return sort;
	}

	@Override
	public Essay saveOrUpdate(Essay essay) {

		return essayRepository.save(essay);
	}

	@Override
	public void delete(Essay essay) {
		essayRepository.delete(essay);
	}

}

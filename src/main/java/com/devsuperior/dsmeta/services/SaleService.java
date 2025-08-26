package com.devsuperior.dsmeta.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import com.devsuperior.dsmeta.dto.SaleReportDTO;
import com.devsuperior.dsmeta.dto.SaleSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.devsuperior.dsmeta.dto.SaleMinDTO;
import com.devsuperior.dsmeta.entities.Sale;
import com.devsuperior.dsmeta.repositories.SaleRepository;

@Service
public class SaleService {

	@Autowired
	private SaleRepository repository;
	
	public SaleMinDTO findById(Long id) {
		Optional<Sale> result = repository.findById(id);
		Sale entity = result.get();
		return new SaleMinDTO(entity);
	}

	public Page<SaleReportDTO> report(String minDate, String maxDate, String name, Pageable pageable) {
		LocalDate max = resolveMax(maxDate);
		LocalDate min = resolveMin(minDate, max);
		String nameParam = normalizeName(name);

		Page<Sale> page = repository.searchReport(min, max, nameParam, pageable);

		return page.map(s -> new SaleReportDTO(
				s.getId(),
				s.getDate(),
				s.getAmount(),
				s.getSeller().getName()
		));
	}

	public List<SaleSummaryDTO> summary(String minDate, String maxDate) {
		LocalDate max = resolveMax(maxDate);
		LocalDate min = resolveMin(minDate, max);

		List<Object[]> list = repository.searchSummary(min, max);

		return list.stream().map(l -> new SaleSummaryDTO(
				(String) l[0],
				((Number) l[1]).doubleValue()
		)).toList();
	}

	private LocalDate today() {
		return LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault());
	}

	private LocalDate resolveMax(String maxDateStr) {
		if (isBlank(maxDateStr)) return today();
		return LocalDate.parse(maxDateStr);
	}

	private LocalDate resolveMin(String minDateStr, LocalDate max) {
		if (isBlank(minDateStr)) return max.minusYears(1);
		return LocalDate.parse(minDateStr);
	}

	private String normalizeName(String name) {
		return isBlank(name) ? "" : name.trim();
	}

	private static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}
}

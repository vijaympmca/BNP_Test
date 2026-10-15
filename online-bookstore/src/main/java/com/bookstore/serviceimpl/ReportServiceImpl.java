package com.bookstore.serviceimpl;

import com.bookstore.dto.OrderSummaryDto;
import com.bookstore.dto.TopSellingBookDto;
import com.bookstore.exception.BadRequestException;
import com.bookstore.repository.OrderItemRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.service.ReportService;
import com.bookstore.utility.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    @Override
    public List<TopSellingBookDto> getTopSellingBooks(int limit) {
        if (limit < 1 || limit > AppConstants.MAX_TOP_BOOKS_LIMIT) {
            throw new BadRequestException("limit must be between 1 and " + AppConstants.MAX_TOP_BOOKS_LIMIT);
        }
        return orderItemRepository.findTopSellingBooks(PageRequest.of(0, limit));
    }

    @Override
    public List<OrderSummaryDto> getOrderSummary() {
        return orderRepository.summarizeByStatus();
    }
}

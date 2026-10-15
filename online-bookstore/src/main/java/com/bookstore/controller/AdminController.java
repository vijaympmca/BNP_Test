package com.bookstore.controller;

import com.bookstore.object.request.UpdateUserRoleRequest;
import com.bookstore.object.request.UpdateUserStatusRequest;
import com.bookstore.object.response.ApiResponse;
import com.bookstore.object.response.OrderResponse;
import com.bookstore.object.response.PageResponse;
import com.bookstore.object.response.UserResponse;
import com.bookstore.service.AdminService;
import com.bookstore.service.OrderService;
import com.bookstore.utility.AppConstants;
import com.bookstore.utility.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Administrator-only endpoints: manage users and see every order. */
@Tag(name = "Administration (ADMIN)", description = "User management and all orders")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    @Operation(summary = "List all users")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", adminService.getUsers(page, size)));
    }

    @Operation(summary = "Get one user")
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User retrieved", adminService.getUser(id)));
    }

    @Operation(summary = "Change a user's role (USER / ADMIN)")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Role updated",
                adminService.changeRole(SecurityUtils.getCurrentUsername(), id, request.role())));
    }

    @Operation(summary = "Enable or disable a user account")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> changeStatus(@PathVariable Long id,
                                                                  @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                adminService.changeStatus(SecurityUtils.getCurrentUsername(), id, request.enabled())));
    }

    @Operation(summary = "All orders of all users, newest first")
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success("All orders retrieved", orderService.getAllOrders(page, size)));
    }
}

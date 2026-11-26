package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.security.CurrentUserService;
import ua.lpnu.studentrecords.service.AccountService;
import ua.lpnu.studentrecords.web.dto.CurrentUserResponse;

@RestController
@RequestMapping("/api/me")
@Tag(name = "Автентифікація", description = "Вхід у систему та отримання JWT")
public class MeController {

    private final CurrentUserService currentUser;
    private final AccountService accountService;

    public MeController(CurrentUserService currentUser, AccountService accountService) {
        this.currentUser = currentUser;
        this.accountService = accountService;
    }

    @GetMapping
    @Operation(summary = "Поточний користувач", description = "Роль, ПІБ та ідентифікатор студента або викладача")
    public CurrentUserResponse me() {
        return accountService.describe(currentUser.require().getId());
    }
}

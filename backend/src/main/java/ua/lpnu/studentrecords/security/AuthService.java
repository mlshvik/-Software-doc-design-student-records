package ua.lpnu.studentrecords.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ua.lpnu.studentrecords.service.AccountService;
import ua.lpnu.studentrecords.web.dto.LoginRequest;
import ua.lpnu.studentrecords.web.dto.LoginResponse;

/**
 * Вхід у систему: перевірка логіна і пароля (BCrypt) та видача JWT.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountService accountService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    /**
     * @throws org.springframework.security.authentication.BadCredentialsException неправильний логін або пароль
     * @throws org.springframework.security.authentication.DisabledException       обліковий запис заблоковано
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username().trim(), request.password()));
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        IssuedToken token = jwtService.issue(user);
        return new LoginResponse(token.token(), "Bearer", token.expiresAt(), accountService.describe(user.getId()));
    }
}

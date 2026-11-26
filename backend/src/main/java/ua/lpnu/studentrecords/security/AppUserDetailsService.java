package ua.lpnu.studentrecords.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.lpnu.studentrecords.repository.AppUserRepository;

/**
 * Завантаження користувача з таблиці {@code app_user} для Spring Security.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(AuthenticatedUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено: " + username));
    }
}

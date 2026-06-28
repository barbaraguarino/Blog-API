package br.com.blog.core.security;

import br.com.blog.modules.user.repositories.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    String errorMessage = messageSource.getMessage(
                            "error.auth.user_not_found",
                            new Object[]{email},
                            LocaleContextHolder.getLocale()
                    );
                    return new UsernameNotFoundException(errorMessage);
                });
    }
}

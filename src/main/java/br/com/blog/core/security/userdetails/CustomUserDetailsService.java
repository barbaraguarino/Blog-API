package br.com.blog.core.security.userdetails;

import br.com.blog.modules.user.domain.models.User;
import br.com.blog.modules.user.infrastructure.persistence.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String login) throws UsernameNotFoundException {

        Optional<User> userOptional = login.contains("@")
                ? userRepository.findByEmail(login)
                : userRepository.findByNickname(login);

        User user = userOptional.orElseThrow(() -> {
            String errorMessage = messageSource.getMessage(
                    "error.auth.user_not_found",
                    new Object[]{login},
                    LocaleContextHolder.getLocale()
            );
            return new UsernameNotFoundException(errorMessage);
        });

        return new CustomUserDetails(user);
    }
}

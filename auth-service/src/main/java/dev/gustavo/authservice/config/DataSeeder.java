package dev.gustavo.authservice.config;

import dev.gustavo.authservice.model.User;
import dev.gustavo.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
           if (userRepository.count() == 0) {
               userRepository.save(new User("gustavo", passwordEncoder.encode("senha123")));
               userRepository.save(new User("teste", passwordEncoder.encode("teste123")));
           }
    }
}

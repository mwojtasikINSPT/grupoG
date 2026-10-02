package prog2.policia_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PoliciaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoliciaBackendApplication.class, args);
        //System.out.println(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("1234"));
        
    }

}

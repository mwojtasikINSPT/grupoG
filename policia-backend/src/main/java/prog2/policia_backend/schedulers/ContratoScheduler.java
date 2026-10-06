package prog2.policia_backend.schedulers;

import jakarta.annotation.PostConstruct; // Importante
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;

@Component
public class ContratoScheduler {

    private final ContratoVigilanciaRepository repository;

    public ContratoScheduler(ContratoVigilanciaRepository repository) {
        this.repository = repository;
    }

    // Se ejecuta al levantar la app
    @PostConstruct
    // 0 a las 00.00 si queda corriendo
    @Scheduled(cron = "0 0 0 * * *") 
    public void marcarContratosVencidosComoInactivos() {
        repository.desactivarContratosVencidos(LocalDate.now());
    }
}
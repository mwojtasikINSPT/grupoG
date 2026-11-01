package prog2.policia_backend.schedulers;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;

@Component
@RequiredArgsConstructor
public class ContratoScheduler {

    private final ContratoVigilanciaRepository repository;

    @Scheduled(cron = "0 0 0 * * *")
    public void marcarContratosVencidosComoInactivos() {
        repository.desactivarContratosVencidos(LocalDate.now());
    }
}

package ua.lpnu.studentrecords.pattern.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Видавець подій про нові оцінки.
 * <p>
 * <b>Шаблон Observer (роль Subject).</b> Зберігає список підписників і сповіщає їх
 * про кожну нову оцінку. Усі Spring-біни, що реалізують {@link GradeEventListener},
 * підписуються автоматично через впровадження списку в конструктор; додатково
 * спостерігачів можна підписувати й відписувати під час роботи.
 * <p>
 * Спостерігачі виконуються синхронно в транзакції збереження оцінки: якщо котрийсь
 * із них завершиться помилкою, транзакція відкотиться разом з оцінкою.
 */
@Component
public class GradeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(GradeEventPublisher.class);

    private final List<GradeEventListener> listeners = new CopyOnWriteArrayList<>();

    public GradeEventPublisher(List<GradeEventListener> listeners) {
        for (GradeEventListener listener : listeners) {
            if (listener != null && !this.listeners.contains(listener)) {
                this.listeners.add(listener);
            }
        }
    }

    public void subscribe(GradeEventListener listener) {
        Objects.requireNonNull(listener, "listener");
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(GradeEventListener listener) {
        listeners.remove(listener);
    }

    /** Поточні підписники (лише для читання). */
    public List<GradeEventListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public void notifyListeners(GradeRecordedEvent event) {
        log.debug("Подія про оцінку студента {} передається {} спостерігачам",
                event.student().getId(), listeners.size());
        for (GradeEventListener listener : listeners) {
            listener.onGradeRecorded(event);
        }
    }
}

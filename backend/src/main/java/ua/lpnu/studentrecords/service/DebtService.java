package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.AcademicDebt;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.DebtStatus;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.AcademicDebtRepository;

import java.util.List;
import java.util.Optional;

/**
 * Облік академічних заборгованостей.
 * Відкриття і закриття боргів ініціює {@code AcademicDebtObserver} після виставлення оцінки.
 */
@Service
@Transactional(readOnly = true)
public class DebtService {

    private final AcademicDebtRepository debtRepository;

    public DebtService(AcademicDebtRepository debtRepository) {
        this.debtRepository = debtRepository;
    }

    /** Усі заборгованості студента (відкриті й закриті), новіші першими. */
    public List<AcademicDebt> getStudentDebts(Long studentId) {
        return debtRepository.findByStudentIdOrderByOpenedAtDesc(studentId);
    }

    /** Заборгованості з певним статусом (для звітів і адміністратора). */
    public List<AcademicDebt> getDebts(DebtStatus status) {
        return debtRepository.findByStatus(status);
    }

    public long countOpenDebts(Long studentId) {
        return debtRepository.countByStudentIdAndStatus(studentId, DebtStatus.OPEN);
    }

    public AcademicDebt getById(Long id) {
        return debtRepository.findById(id).orElseThrow(() -> new NotFoundException("Заборгованість", id));
    }

    /**
     * Відкриває заборгованість з дисципліни, якщо відкритої ще немає.
     *
     * @return відкрита заборгованість (нова або вже наявна)
     */
    @Transactional
    public AcademicDebt openDebt(Student student, TeachingAssignment assignment) {
        return findOpen(student, assignment)
                .orElseGet(() -> debtRepository.save(new AcademicDebt(student, assignment)));
    }

    /**
     * Закриває відкриту заборгованість з дисципліни, якщо вона є.
     *
     * @return закрита заборгованість або порожнє значення, якщо боргу не було
     */
    @Transactional
    public Optional<AcademicDebt> closeDebt(Student student, TeachingAssignment assignment) {
        Optional<AcademicDebt> open = findOpen(student, assignment);
        open.ifPresent(debt -> {
            debt.close();
            debtRepository.save(debt);
        });
        return open;
    }

    private Optional<AcademicDebt> findOpen(Student student, TeachingAssignment assignment) {
        return debtRepository.findFirstByStudentIdAndAssignmentIdAndStatus(
                student.getId(), assignment.getId(), DebtStatus.OPEN);
    }
}

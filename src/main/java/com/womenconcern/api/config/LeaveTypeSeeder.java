package com.womenconcern.api.config;

import com.womenconcern.api.leave.entity.LeaveType;
import com.womenconcern.api.leave.leaveEnum.LeaveEligibility;
import com.womenconcern.api.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveTypeSeeder implements ApplicationRunner {

    private final LeaveTypeRepository leaveTypeRepository;

    @Override
    public void run(ApplicationArguments args) {

        seedLeaveType(
                "Annual Leave",
                "Yearly paid vacation leave for all employees",
                30,
                false,
                true,
                LeaveEligibility.ALL,
                true,
                10
        );

        seedLeaveType(
                "Maternity Leave",
                "Leave granted to female employees for childbirth and recovery",
                90,
                true,
                true,
                LeaveEligibility.FEMALE_ONLY,
                false,
                null
        );

        seedLeaveType(
                "Paternity Leave",
                "Leave granted to male employees after childbirth",
                7,
                false,
                true,
                LeaveEligibility.MALE_ONLY,
                false,
                null
        );

        seedLeaveType(
                "Sick Leave",
                "Medical leave for illness or hospital visits",
                14,
                true,
                true,
                LeaveEligibility.ALL,
                false,
                null
        );

        seedLeaveType(
                "Unpaid Leave",
                "Leave without salary payment",
                60,
                false,
                false,
                LeaveEligibility.ALL,
                false,
                null
        );

        log.info("✅ Leave types seeding completed");
    }

    private void seedLeaveType(
            String name,
            String description,
            Integer maxDays,
            boolean requiresAttachment,
            boolean isPaid,
            LeaveEligibility eligibility,
            Boolean allowCarryForward,
            Integer maxCarryForwardDays
    ) {

        if (leaveTypeRepository.existsByName(name)) {
            return;
        }

        LeaveType type = new LeaveType();
        type.setName(name);
        type.setDescription(description);
        type.setMaxDaysPerYear(maxDays);
        type.setRequiresAttachment(requiresAttachment);
        type.setIsPaid(isPaid);
        type.setEligibility(eligibility);
        type.setAllowCarryForward(allowCarryForward);
        type.setMaxCarryForwardDays(maxCarryForwardDays);

        leaveTypeRepository.save(type);

        log.info("✔ Seeded leave type: {}", name);
    }
}
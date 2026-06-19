package com.womenconcern.api.leave.service;

import com.womenconcern.api.jooq.generated.Tables;
import com.womenconcern.api.leave.entity.LeaveType;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveService {
    @Autowired
    private DSLContext dsl;

    public List<LeaveType> getLeaveTypes() {
        return dsl
                .selectFrom(Tables.LEAVE_TYPES)
                .fetchInto(LeaveType.class);
    }
}

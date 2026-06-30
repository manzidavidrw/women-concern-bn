package com.womenconcern.api.project.service.impl;

import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.repository.UserRepository;
import com.womenconcern.api.project.entity.*;
import com.womenconcern.api.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ProjectExportService {

    private final ProjectRepository projectRepository;
    private final GoalRepository goalRepository;
    private final OutcomeRepository outcomeRepository;
    private final ResultRepository resultRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());

    private static final String COLOR_NAVY      = "1F3864";
    private static final String COLOR_BLUE      = "2E75B6";
    private static final String COLOR_DARK_GREY = "404040";
    private static final String COLOR_MID_GREY  = "595959";
    private static final String COLOR_TABLE_ALT = "EBF3FB";
    private static final String COLOR_WHITE     = "FFFFFF";

    // ─────────────────────────────────────────────────────────────────────────
    // Public entry point
    // ─────────────────────────────────────────────────────────────────────────

    public byte[] exportProjectToDocx(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            setPageMargins(doc);

            // 1. Title
            addCenteredText(doc, "WOMEN CONCERN", 11, COLOR_BLUE,  true,  60,  0);
            addCenteredText(doc, "PROJECT DOCUMENT", 20, COLOR_NAVY, true, 0,  60);
            addCenteredText(doc, project.getName(),  28, COLOR_NAVY, true, 0, 120);
            addDivider(doc);
            addBlankLine(doc);

            // 2. Overview
            addHeading(doc, "1. Project Overview", 1);
            addProjectMetaTable(doc, project);
            addBlankLine(doc);

            // 3. Description
            if (hasText(project.getDescription())) {
                addHeading(doc, "2. Description", 1);
                addBody(doc, project.getDescription());
                addBlankLine(doc);
            }

            // 4. Approval History
            addHeading(doc, "3. Approval History", 1);
            addApprovalTable(doc, project);
            addBlankLine(doc);

            // 5. Hierarchy
            addHeading(doc, "4. Project Hierarchy", 1);
            List<Goal> goals = goalRepository.findByProjectId(projectId);
            if (goals.isEmpty()) {
                addBody(doc, "No goals have been defined for this project.");
            } else {
                int gi = 1;
                for (Goal goal : goals) {
                    addHeading(doc, "Goal " + gi + ": " + goal.getTitle(), 2);
                    addLabelValue(doc, "Description", goal.getDescription());
                    addLabelValue(doc, "Budget",      formatAmount(goal.getTotalBudget()));

                    List<Outcome> outcomes = outcomeRepository.findByGoalId(goal.getId());
                    int oi = 1;
                    for (Outcome outcome : outcomes) {
                        addHeading(doc, "Outcome " + gi + "." + oi + ": " + outcome.getTitle(), 3);
                        addLabelValue(doc, "Description", outcome.getDescription());
                        addLabelValue(doc, "Budget",      formatAmount(outcome.getTotalBudget()));

                        List<Result> results = resultRepository.findByOutcomeId(outcome.getId());
                        int ri = 1;
                        for (Result result : results) {
                            addHeading(doc, "Result " + gi + "." + oi + "." + ri + ": " + result.getTitle(), 4);
                            addLabelValue(doc, "Description", result.getDescription());
                            addLabelValue(doc, "Budget",      formatAmount(result.getTotalBudget()));

                            List<Activity> activities = activityRepository.findByResultId(result.getId());
                            if (!activities.isEmpty()) {
                                addActivitiesTable(doc, activities, gi, oi, ri);
                            }
                            ri++;
                        }
                        oi++;
                    }
                    gi++;
                    addBlankLine(doc);
                }
            }

            // 6. Budget Summary
            addPageBreak(doc);
            addHeading(doc, "5. Budget Summary", 1);
            addBudgetSummaryTable(doc, project, goals);

            // Footer on every page
            addFooter(doc, project.getName());

            doc.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate DOCX: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tables
    // ─────────────────────────────────────────────────────────────────────────

    private void addProjectMetaTable(XWPFDocument doc, Project project) {
        String pmName = resolveUserName(project.getProjectManagerId());

        String[][] rows = {
                {"Project Name",    project.getName()},
                {"Status",          project.getStatus() != null ? project.getStatus().name() : "-"},
                {"Total Budget",    formatAmount(project.getTotalBudget())},
                {"Approved Budget", formatAmount(project.getApprovedBudget())},
                {"Project Manager", pmName},
        };

        XWPFTable table = doc.createTable(rows.length, 2);
        setTableBorders(table);
        setTableWidth(table, 9360);

        for (int i = 0; i < rows.length; i++) {
            String bg = i % 2 == 0 ? "F2F7FC" : COLOR_WHITE;
            XWPFTableRow row = table.getRow(i);
            styleCell(row.getCell(0), rows[i][0], true,  bg, 4680);
            styleCell(row.getCell(1), rows[i][1], false, bg, 4680);
        }
    }

    private void addApprovalTable(XWPFDocument doc, Project project) {
        // Header + 3 data rows (PM, Finance, Executive)
        XWPFTable table = doc.createTable(4, 4);
        setTableBorders(table);
        setTableWidth(table, 9360);

        int[] widths = {2500, 2000, 2860, 2000};

        // Header
        String[] headers = {"Stage", "Status", "Notes", "Approved At"};
        for (int c = 0; c < headers.length; c++) {
            styleHeaderCell(table.getRow(0).getCell(c), headers[c], widths[c]);
        }

        // PM row
        Object[] pmRow = buildApprovalRow(
                "Project Manager",
                project.getProjectManagerApprovalStatus(),
                project.getProjectManagerApprovalNotes(),
                project.getProjectManagerApprovedAt(),
                project.getProjectManagerApprovedBy()
        );
        fillApprovalRow(table.getRow(1), pmRow, widths, "F2F7FC");

        // Finance row
        Object[] finRow = buildApprovalRow(
                "Finance",
                project.getFinanceApprovalStatus(),
                project.getFinanceApprovalNotes(),
                project.getFinanceApprovedAt(),
                project.getFinanceApprovedBy()
        );
        fillApprovalRow(table.getRow(2), finRow, widths, COLOR_WHITE);

        // Executive row
        Object[] exRow = buildApprovalRow(
                "Executive Director",
                project.getExecutiveApprovalStatus(),
                project.getExecutiveApprovalNotes(),
                project.getExecutiveApprovedAt(),
                project.getExecutiveApprovedBy()
        );
        fillApprovalRow(table.getRow(3), exRow, widths, "F2F7FC");
    }

    private Object[] buildApprovalRow(String stage, Object status, String notes,
                                      Instant approvedAt, UUID approvedBy) {
        return new Object[]{
                stage,
                status != null ? status.toString() : "PENDING",
                hasText(notes) ? notes : "-",
                approvedAt != null ? DATE_FMT.format(approvedAt) : "-"
        };
    }

    private void fillApprovalRow(XWPFTableRow row, Object[] data, int[] widths, String bg) {
        for (int c = 0; c < data.length; c++) {
            styleCell(row.getCell(c), data[c].toString(), false, bg, widths[c]);
        }
    }

    private void addActivitiesTable(XWPFDocument doc, List<Activity> activities,
                                    int gi, int oi, int ri) {
        addLabelValue(doc, "Activities", null);
        int[] widths = {700, 3500, 2000, 3160};
        XWPFTable table = doc.createTable(activities.size() + 1, 4);
        setTableBorders(table);
        setTableWidth(table, 9360);

        String[] headers = {"#", "Activity Title", "Cost Estimate", "Field Officer"};
        for (int c = 0; c < headers.length; c++) {
            styleHeaderCell(table.getRow(0).getCell(c), headers[c], widths[c]);
        }

        for (int i = 0; i < activities.size(); i++) {
            Activity a  = activities.get(i);
            String bg   = i % 2 == 0 ? COLOR_WHITE : COLOR_TABLE_ALT;
            String ref  = gi + "." + oi + "." + ri + "." + (i + 1);
            String fo   = a.getFieldOfficerId() != null
                    ? resolveUserName(a.getFieldOfficerId())
                    : "Unassigned";

            styleCell(table.getRow(i + 1).getCell(0), ref,                          false, bg, widths[0]);
            styleCell(table.getRow(i + 1).getCell(1), a.getTitle(),                  false, bg, widths[1]);
            styleCell(table.getRow(i + 1).getCell(2), formatAmount(a.getCostEstimate()), false, bg, widths[2]);
            styleCell(table.getRow(i + 1).getCell(3), fo,                            false, bg, widths[3]);
        }
        addBlankLine(doc);
    }

    private void addBudgetSummaryTable(XWPFDocument doc, Project project, List<Goal> goals) {
        int[] widths = {5360, 2500, 1500};
        XWPFTable table = doc.createTable(goals.size() + 2, 3);
        setTableBorders(table);
        setTableWidth(table, 9360);

        String[] headers = {"Goal", "Budget (USD)", "% of Total"};
        for (int c = 0; c < headers.length; c++) {
            styleHeaderCell(table.getRow(0).getCell(c), headers[c], widths[c]);
        }

        double total = project.getTotalBudget() != null
                ? project.getTotalBudget().doubleValue() : 0;

        for (int i = 0; i < goals.size(); i++) {
            Goal g   = goals.get(i);
            String bg = i % 2 == 0 ? COLOR_WHITE : COLOR_TABLE_ALT;
            double gb = g.getTotalBudget() != null ? g.getTotalBudget().doubleValue() : 0;
            String pct = total > 0 ? String.format("%.1f%%", (gb / total) * 100) : "-";

            styleCell(table.getRow(i + 1).getCell(0), g.getTitle(),               false, bg, widths[0]);
            styleCell(table.getRow(i + 1).getCell(1), formatAmount(g.getTotalBudget()), false, bg, widths[1]);
            styleCell(table.getRow(i + 1).getCell(2), pct,                        false, bg, widths[2]);
        }

        // Total row
        int last = goals.size() + 1;
        styleCell(table.getRow(last).getCell(0), "TOTAL",                           true, "D5E8F0", widths[0]);
        styleCell(table.getRow(last).getCell(1), formatAmount(project.getTotalBudget()), true, "D5E8F0", widths[1]);
        styleCell(table.getRow(last).getCell(2), "100%",                            true, "D5E8F0", widths[2]);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Document building primitives
    // ─────────────────────────────────────────────────────────────────────────

    private void setPageMargins(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageMar mar   = sectPr.addNewPgMar();
        mar.setTop(BigInteger.valueOf(1080));
        mar.setBottom(BigInteger.valueOf(1080));
        mar.setLeft(BigInteger.valueOf(1260));
        mar.setRight(BigInteger.valueOf(1260));
    }

    private void addCenteredText(XWPFDocument doc, String text, int fontSize,
                                 String color, boolean bold, int before, int after) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingBefore(before);
        p.setSpacingAfter(after);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setFontSize(fontSize);
        run.setColor(color);
        run.setBold(bold);
        run.setFontFamily("Arial");
    }

    private void addDivider(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(40);
        p.setSpacingAfter(40);
        CTPPr ppr  = p.getCTP().addNewPPr();
        CTPBdr bdr = ppr.addNewPBdr();
        CTBorder b = bdr.addNewBottom();
        b.setVal(STBorder.SINGLE);
        b.setSz(BigInteger.valueOf(12));
        b.setColor(COLOR_BLUE);
    }

    private void addHeading(XWPFDocument doc, String text, int level) {
        XWPFParagraph p = doc.createParagraph();
        int fontSize;
        String color;
        switch (level) {
            case 1  -> { p.setSpacingBefore(240); p.setSpacingAfter(120); fontSize = 14; color = COLOR_BLUE; }
            case 2  -> { p.setSpacingBefore(200); p.setSpacingAfter(80);  fontSize = 12; color = COLOR_BLUE; }
            case 3  -> { p.setSpacingBefore(160); p.setSpacingAfter(60);  fontSize = 11; color = COLOR_DARK_GREY; }
            default -> { p.setSpacingBefore(120); p.setSpacingAfter(40);  fontSize = 11; color = COLOR_DARK_GREY; }
        }
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontFamily("Arial");
        run.setFontSize(fontSize);
        run.setColor(color);
        if (level <= 2) {
            // underline for top-level headings
            run.setUnderline(UnderlinePatterns.SINGLE);
        }
    }

    private void addBody(XWPFDocument doc, String text) {
        if (!hasText(text)) return;
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(80);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setFontFamily("Arial");
        run.setFontSize(11);
        run.setColor("000000");
    }

    private void addLabelValue(XWPFDocument doc, String label, String value) {
        // If value is null this renders just the bold label as a sub-section marker
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(60);
        XWPFRun labelRun = p.createRun();
        labelRun.setText(label + (value != null ? ": " : ""));
        labelRun.setBold(true);
        labelRun.setFontFamily("Arial");
        labelRun.setFontSize(11);
        labelRun.setColor(COLOR_MID_GREY);
        if (hasText(value)) {
            XWPFRun valueRun = p.createRun();
            valueRun.setText(value);
            valueRun.setFontFamily("Arial");
            valueRun.setFontSize(11);
            valueRun.setColor("000000");
        }
    }

    private void addBlankLine(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(0);
        p.setSpacingAfter(0);
        p.createRun().setText("");
    }

    private void addPageBreak(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setPageBreak(true);
    }

    private void addFooter(XWPFDocument doc, String projectName) {
        XWPFHeaderFooterPolicy policy = doc.createHeaderFooterPolicy();
        XWPFFooter footer = policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);
        XWPFParagraph p   = footer.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setText("Women Concern Management System  |  " + projectName);
        run.setFontFamily("Arial");
        run.setFontSize(9);
        run.setColor("808080");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cell / table styling
    // ─────────────────────────────────────────────────────────────────────────

    private void styleHeaderCell(XWPFTableCell cell, String text, int widthDxa) {
        cell.setColor(COLOR_BLUE);
        setCellWidth(cell, widthDxa);
        setCellPadding(cell);
        XWPFParagraph p = cell.getParagraphs().get(0);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(true);
        run.setColor(COLOR_WHITE);
        run.setFontFamily("Arial");
        run.setFontSize(11);
    }

    private void styleCell(XWPFTableCell cell, String text, boolean bold,
                           String bgColor, int widthDxa) {
        cell.setColor(bgColor);
        setCellWidth(cell, widthDxa);
        setCellPadding(cell);
        XWPFParagraph p = cell.getParagraphs().get(0);
        XWPFRun run = p.createRun();
        run.setText(hasText(text) ? text : "-");
        run.setBold(bold);
        run.setFontFamily("Arial");
        run.setFontSize(11);
        run.setColor("000000");
    }

    private void setCellWidth(XWPFTableCell cell, int widthDxa) {
        CTTcPr pr = cell.getCTTc().isSetTcPr()
                ? cell.getCTTc().getTcPr()
                : cell.getCTTc().addNewTcPr();
        CTTblWidth w = pr.isSetTcW() ? pr.getTcW() : pr.addNewTcW();
        w.setW(BigInteger.valueOf(widthDxa));
        w.setType(STTblWidth.DXA);
    }

    private void setCellPadding(XWPFTableCell cell) {
        CTTcPr pr = cell.getCTTc().isSetTcPr()
                ? cell.getCTTc().getTcPr()
                : cell.getCTTc().addNewTcPr();
        CTTcMar mar = pr.isSetTcMar() ? pr.getTcMar() : pr.addNewTcMar();

        CTTblWidth top    = mar.isSetTop()    ? mar.getTop()    : mar.addNewTop();
        CTTblWidth bottom = mar.isSetBottom() ? mar.getBottom() : mar.addNewBottom();
        CTTblWidth left   = mar.isSetLeft()   ? mar.getLeft()   : mar.addNewLeft();
        CTTblWidth right  = mar.isSetRight()  ? mar.getRight()  : mar.addNewRight();

        for (CTTblWidth w : List.of(top, bottom, left, right)) {
            w.setW(BigInteger.valueOf(100));
            w.setType(STTblWidth.DXA);
        }
    }

    private void setTableWidth(XWPFTable table, int widthDxa) {
        CTTbl tbl     = table.getCTTbl();
        CTTblPr tblPr = tbl.getTblPr() != null ? tbl.getTblPr() : tbl.addNewTblPr();
        CTTblWidth w  = tblPr.getTblW() != null ? tblPr.getTblW() : tblPr.addNewTblW();
        w.setW(BigInteger.valueOf(widthDxa));
        w.setType(STTblWidth.DXA);
    }

    private void setTableBorders(XWPFTable table) {
        CTTbl tbl        = table.getCTTbl();
        CTTblPr tblPr    = tbl.getTblPr() != null ? tbl.getTblPr() : tbl.addNewTblPr();
        CTTblBorders bdr = tblPr.getTblBorders() != null
                ? tblPr.getTblBorders()
                : tblPr.addNewTblBorders();

        CTBorder top     = bdr.getTop()     != null ? bdr.getTop()     : bdr.addNewTop();
        CTBorder bottom  = bdr.getBottom()  != null ? bdr.getBottom()  : bdr.addNewBottom();
        CTBorder left    = bdr.getLeft()    != null ? bdr.getLeft()     : bdr.addNewLeft();
        CTBorder right   = bdr.getRight()   != null ? bdr.getRight()   : bdr.addNewRight();
        CTBorder insideH = bdr.getInsideH() != null ? bdr.getInsideH() : bdr.addNewInsideH();
        CTBorder insideV = bdr.getInsideV() != null ? bdr.getInsideV() : bdr.addNewInsideV();

        for (CTBorder b : List.of(top, bottom, left, right, insideH, insideV)) {
            b.setVal(STBorder.SINGLE);
            b.setSz(BigInteger.valueOf(4));
            b.setColor("CCCCCC");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────

    private String resolveUserName(UUID userId) {
        if (userId == null) return "-";
        return userRepository.findById(userId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Unknown");
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "-";
        return String.format("USD %,.2f", amount);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
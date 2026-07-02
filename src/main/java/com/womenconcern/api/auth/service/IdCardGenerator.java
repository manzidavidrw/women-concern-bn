package com.womenconcern.api.auth.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.womenconcern.api.auth.entity.User;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

/**
 * Generates a CR80-sized (85.6 × 54 mm) two-sided PDF ID card
 * for Women Concern employees.
 *
 * Colours
 *   Primary green  #0f442d   (dark forest green)
 *   Accent gold    #c9943f
 *   White          #ffffff
 *
 * Dependencies (pom.xml)
 *   com.github.librepdf : openpdf       : 1.3.43
 *   com.google.zxing    : core          : 3.5.2
 *   com.google.zxing    : javase        : 3.5.2
 *
 * Logo file
 *   Place Upper-Logo.png at:
 *   src/main/resources/static/images/Upper-Logo.png
 */
@Component
public class IdCardGenerator {

    // ── CR80 card dimensions in points (1 pt = 1/72 inch) ─────────────
    private static final float CARD_W = 242f;   // 85.6 mm
    private static final float CARD_H = 153f;   // 54 mm

    // ── Brand colours ──────────────────────────────────────────────────
    private static final Color GREEN  = new Color(0x0f, 0x44, 0x2d);   // #0f442d
    private static final Color GOLD   = new Color(0xc9, 0x94, 0x3f);   // #c9943f
    private static final Color WHITE  = Color.WHITE;
    private static final Color LIGHT  = new Color(0xf2, 0xf5, 0xf0);   // off-white fill
    private static final Color MUTED  = new Color(0x88, 0x99, 0x88);   // subtle text

    // ── Layout constants ───────────────────────────────────────────────
    private static final float HEADER_H   = 38f;
    private static final float FOOTER_H   = 18f;
    private static final float AVATAR_X   = 10f;
    private static final float AVATAR_Y   = FOOTER_H + 12f;
    private static final float AVATAR_W   = 58f;
    private static final float AVATAR_H   = CARD_H - HEADER_H - FOOTER_H - 20f;
    private static final float INFO_X     = AVATAR_X + AVATAR_W + 9f;
    private static final float INFO_W     = CARD_W - INFO_X - 8f;

    // ── Fonts ──────────────────────────────────────────────────────────
    private static final Font F_ORG_NAME  = new Font(Font.HELVETICA, 8f,  Font.BOLD,   WHITE);
    private static final Font F_TAGLINE   = new Font(Font.HELVETICA, 5f,  Font.NORMAL, GOLD);
    private static final Font F_DRC       = new Font(Font.HELVETICA, 6f,  Font.BOLD,   GOLD);
    private static final Font F_NAME      = new Font(Font.HELVETICA, 9f,  Font.BOLD,   GREEN);
    private static final Font F_POSITION  = new Font(Font.HELVETICA, 7f,  Font.BOLD,   GOLD);
    private static final Font F_LABEL     = new Font(Font.HELVETICA, 5.5f,Font.BOLD,   MUTED);
    private static final Font F_VALUE     = new Font(Font.HELVETICA, 7f,  Font.NORMAL, new Color(0x22, 0x22, 0x22));
    private static final Font F_FOOTER    = new Font(Font.HELVETICA, 5f,  Font.NORMAL, new Color(0xc9, 0xd9, 0xa0));
    private static final Font F_VALID     = new Font(Font.HELVETICA, 5f,  Font.NORMAL, new Color(0xc9, 0xd9, 0xa0));
    private static final Font F_QR_LBL    = new Font(Font.HELVETICA, 5.5f,Font.NORMAL, MUTED);
    private static final Font F_BACK_ORG  = new Font(Font.HELVETICA, 8f,  Font.BOLD,   new Color(0xc9, 0xd9, 0xa0));
    private static final Font F_BACK_SUB  = new Font(Font.HELVETICA, 5.5f,Font.NORMAL, new Color(0xff,0xff,0xff,120));
    private static final Font F_SITE      = new Font(Font.HELVETICA, 7f,  Font.BOLD,   GREEN);
    private static final Font F_NOTICE    = new Font(Font.HELVETICA, 5f,  Font.NORMAL, MUTED);
    private static final Font F_BACK_FT   = new Font(Font.HELVETICA, 5f,  Font.BOLD,   new Color(0x0f, 0x44, 0x2d));

    // ── Public API ─────────────────────────────────────────────────────

    /**
     * Generates a two-page PDF: page 1 = front, page 2 = back.
     */
    public byte[] generate(User user) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document doc = new Document(new Rectangle(CARD_W, CARD_H), 0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();

        PdfContentByte cb = writer.getDirectContent();

        // ── Page 1: Front ──────────────────────────────────────────────
        drawFront(cb, user, writer);

        // ── Page 2: Back ───────────────────────────────────────────────
        doc.newPage();
        cb = writer.getDirectContent();
        drawBack(cb, user);

        doc.close();
        return out.toByteArray();
    }

    // ── Front ──────────────────────────────────────────────────────────

    private void drawFront(PdfContentByte cb, User user, PdfWriter writer) throws Exception {

        // White card base
        fillRect(cb, WHITE, 0, 0, CARD_W, CARD_H);

        // ── Green header ────────────────────────────────────────────
        fillRect(cb, GREEN, 0, CARD_H - HEADER_H, CARD_W, HEADER_H);

        // Logo (loaded from classpath)
        try {
            InputStream logoStream = getClass().getResourceAsStream("/static/images/Upper-Logo.png");
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                Image logo = Image.getInstance(logoBytes);
                float logoH = HEADER_H - 6f;
                float logoW = logo.getWidth() * (logoH / logo.getHeight());
                logo.setAbsolutePosition(8f, CARD_H - HEADER_H + 3f);
                logo.scaleToFit(logoW, logoH);
                writer.getDirectContent().addImage(logo);
            }
        } catch (Exception ignored) {
            // Fallback: draw text name if logo cannot be loaded
            text(cb, "WOMEN CONCERN", CARD_W / 2f, CARD_H - HEADER_H + 14f, F_ORG_NAME, Element.ALIGN_CENTER);
            text(cb, "Dignifying women's life", CARD_W / 2f, CARD_H - HEADER_H + 6f, F_TAGLINE, Element.ALIGN_CENTER);
        }

        // DRC country badge (top-right)
        float badgeX = CARD_W - 28f;
        float badgeY = CARD_H - HEADER_H + 6f;
        fillRoundRect(cb, new Color(0, 60, 0, 140), badgeX, badgeY, 24f, 14f, 3f);
        text(cb, "DRC", badgeX + 12f, badgeY + 4f, F_DRC, Element.ALIGN_CENTER);

        // Gold left accent stripe
        fillRect(cb, GOLD, 0, FOOTER_H, 3f, CARD_H - HEADER_H - FOOTER_H);

        // ── Avatar area ──────────────────────────────────────────────
        fillRoundRect(cb, LIGHT, AVATAR_X + 3f, AVATAR_Y, AVATAR_W, AVATAR_H, 5f);
        strokeRoundRect(cb, GOLD, 0.8f, AVATAR_X + 3f, AVATAR_Y, AVATAR_W, AVATAR_H, 5f);

        // Silhouette placeholder (circle head + body arc)
        float cx = AVATAR_X + 3f + AVATAR_W / 2f;
        float avatarTop = AVATAR_Y + AVATAR_H;
        cb.setColorFill(MUTED);
        cb.circle(cx, avatarTop - 13f, 7f);
        cb.fill();
        cb.arc(cx - 11f, AVATAR_Y + 4f, cx + 11f, AVATAR_Y + 26f, 0, 180);
        cb.fill();

        // "Photo" micro label
        text(cb, "PHOTO", cx, AVATAR_Y + 1.5f, F_LABEL, Element.ALIGN_CENTER);

        // Vertical divider
        cb.setColorStroke(new Color(0xdd, 0xdd, 0xdd));
        cb.setLineWidth(0.5f);
        cb.moveTo(INFO_X - 4f, FOOTER_H + 6f);
        cb.lineTo(INFO_X - 4f, CARD_H - HEADER_H - 4f);
        cb.stroke();

        // ── Info column ──────────────────────────────────────────────
        String fullName = user.getFirstName() + " " + user.getLastName();
        float fy = CARD_H - HEADER_H - 10f;

        text(cb, fullName,             INFO_X, fy,       F_NAME,     Element.ALIGN_LEFT);
        fy -= 9f;
        text(cb, formatRole(user.getRole().name()), INFO_X, fy, F_POSITION, Element.ALIGN_LEFT);
        fy -= 5f;

        // Divider
        cb.setColorStroke(new Color(0xe0, 0xe0, 0xdc));
        cb.setLineWidth(0.4f);
        cb.moveTo(INFO_X, fy);
        cb.lineTo(INFO_X + INFO_W, fy);
        cb.stroke();
        fy -= 7f;

        // Phone
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            labeledRow(cb, "PHONE", user.getPhoneNumber(), INFO_X, fy);
            fy -= 9f;
        }

        // Email (truncate if too long)
        String email = user.getEmail();
        if (email.length() > 26) email = email.substring(0, 25) + "…";
        labeledRow(cb, "EMAIL", email, INFO_X, fy);
        fy -= 9f;

        // Website
        labeledRow(cb, "WEB", "www.womenconcern.org", INFO_X, fy);
        fy -= 9f;

        // Joined date
        if (user.getJoinedAt() != null) {
            String joined = user.getJoinedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
            labeledRow(cb, "JOINED", joined, INFO_X, fy);
        }

        // ── Gold footer ──────────────────────────────────────────────
        fillRect(cb, GREEN, 0, 0, CARD_W, FOOTER_H);

        // Barcode strips (decorative)
        int[] widths = {1, 2, 1, 3, 1, 2, 1, 1, 3, 1, 2, 1, 3, 2, 1};
        float bx = 8f;
        cb.setColorFill(new Color(0xff, 0xff, 0xff, 100));
        for (int w : widths) {
            cb.rectangle(bx, 3f, w, 11f);
            cb.fill();
            bx += w + 1.5f;
        }

        text(cb, "Dignifying women's life",  CARD_W / 2f, 6f,  F_FOOTER, Element.ALIGN_CENTER);
        text(cb, "Valid: Dec 2026",           CARD_W - 8f, 6f,  F_VALID,  Element.ALIGN_RIGHT);
    }

    // ── Back ───────────────────────────────────────────────────────────

    private void drawBack(PdfContentByte cb, User user) throws Exception {

        // White base
        fillRect(cb, WHITE, 0, 0, CARD_W, CARD_H);

        // Green header
        fillRect(cb, GREEN, 0, CARD_H - HEADER_H, CARD_W, HEADER_H);
        text(cb, "WOMEN CONCERN",     12f, CARD_H - HEADER_H + 14f, F_BACK_ORG, Element.ALIGN_LEFT);
        text(cb, "Staff Identification Card", 12f, CARD_H - HEADER_H + 6f,  F_BACK_SUB, Element.ALIGN_LEFT);

        // Gold footer
        fillRect(cb, GOLD, 0, 0, CARD_W, FOOTER_H);
        text(cb, "Confidential — not transferable", CARD_W / 2f, 5.5f, F_BACK_FT, Element.ALIGN_CENTER);

        // Body area boundaries
        float bodyTop = CARD_H - HEADER_H - 4f;
        float bodyBot = FOOTER_H + 4f;

        // ── QR section (left column) ─────────────────────────────────
        float qrColW = 110f;
        float qrSize = 72f;
        float qrX = (qrColW - qrSize) / 2f;
        float qrY = bodyBot + (bodyTop - bodyBot - qrSize) / 2f - 6f;

        // QR code
        String qrContent = "https://womenconcern.org/verify/staff/" + user.getId();
        byte[] qrBytes = generateQrCode(qrContent, Math.round(qrSize * 2));
        Image qrImg = Image.getInstance(qrBytes);
        qrImg.setAbsolutePosition(qrX, qrY);
        qrImg.scaleToFit(qrSize, qrSize);
        cb.addImage(qrImg);

        // QR border
        strokeRoundRect(cb, GREEN, 1f, qrX - 2f, qrY - 2f, qrSize + 4f, qrSize + 4f, 4f);

        // "Scan to verify" label
        text(cb, "Scan to verify", qrColW / 2f, qrY - 8f, F_QR_LBL, Element.ALIGN_CENTER);

        // Vertical divider between QR and info
        cb.setColorStroke(new Color(0xe0, 0xdc, 0xd8));
        cb.setLineWidth(0.5f);
        cb.moveTo(qrColW, bodyBot + 2f);
        cb.lineTo(qrColW, bodyTop - 2f);
        cb.stroke();

        // ── Info column (right) ──────────────────────────────────────
        float ix = qrColW + 10f;
        float iy = bodyTop - 4f;

        iy -= backField(cb, "Full name",    user.getFirstName() + " " + user.getLastName(), ix, iy);
        iy -= 3f;
        iy -= backField(cb, "Position",     formatRole(user.getRole().name()), ix, iy);
        iy -= 3f;

        // Organisation + country
        String org = "Women Concern — DRC";
        if (user.getAddress() != null && !user.getAddress().isBlank()) {
            org = user.getAddress() + ", DRC";
        }
        iy -= backField(cb, "Organisation", org, ix, iy);
        iy -= 5f;

        // Divider
        cb.setColorStroke(new Color(0xec, 0xe8, 0xe2));
        cb.setLineWidth(0.4f);
        cb.moveTo(ix, iy);
        cb.lineTo(CARD_W - 8f, iy);
        cb.stroke();
        iy -= 7f;

        // Website pill
        float pillW = CARD_W - 8f - ix;
        float pillH = 14f;
        fillRoundRect(cb, new Color(0xf0, 0xf5, 0xe8), ix, iy - pillH, pillW, pillH, 4f);
        text(cb, "www.womenconcern.org", ix + 6f, iy - pillH + 4f, F_SITE, Element.ALIGN_LEFT);
        iy -= pillH + 4f;

        // Notice
        text(cb, "If found, please return to Women Concern.", ix, iy, F_NOTICE, Element.ALIGN_LEFT);
        iy -= 6f;
        text(cb, "This card is property of the organisation.", ix, iy, F_NOTICE, Element.ALIGN_LEFT);
    }

    // ── Drawing helpers ────────────────────────────────────────────────

    private void fillRect(PdfContentByte cb, Color c, float x, float y, float w, float h) {
        cb.setColorFill(c);
        cb.rectangle(x, y, w, h);
        cb.fill();
    }

    private void fillRoundRect(PdfContentByte cb, Color c, float x, float y, float w, float h, float r) {
        cb.setColorFill(c);
        cb.roundRectangle(x, y, w, h, r);
        cb.fill();
    }

    private void strokeRoundRect(PdfContentByte cb, Color c, float lw, float x, float y, float w, float h, float r) {
        cb.setColorStroke(c);
        cb.setLineWidth(lw);
        cb.roundRectangle(x, y, w, h, r);
        cb.stroke();
    }

    private void text(PdfContentByte cb, String s, float x, float y, Font font, int align) throws Exception {
        com.lowagie.text.pdf.ColumnText.showTextAligned(cb, align, new Phrase(s, font), x, y, 0);
    }

    /** Renders a small label + value row (label above value). */
    private void labeledRow(PdfContentByte cb, String label, String value,
                            float x, float y) throws Exception {
        text(cb, label, x, y + 4f, F_LABEL, Element.ALIGN_LEFT);
        text(cb, value, x + 28f, y + 4f, F_VALUE, Element.ALIGN_LEFT);
    }

    /**
     * Renders a back-of-card info field (label then value stacked).
     * Returns the total height consumed (so caller can decrement iy).
     */
    private float backField(PdfContentByte cb, String label, String value,
                            float x, float y) throws Exception {
        Font lf = new Font(Font.HELVETICA, 5.5f, Font.BOLD, MUTED);
        Font vf = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, new Color(0x1a, 0x1a, 0x18));
        text(cb, label.toUpperCase(), x, y,      lf, Element.ALIGN_LEFT);
        text(cb, value,               x, y - 7f, vf, Element.ALIGN_LEFT);
        return 14f;
    }

    // ── QR code ────────────────────────────────────────────────────────

    private byte[] generateQrCode(String content, int size) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);

        // Custom colours: green modules on white
        int onColor  = GREEN.getRGB();
        int offColor = WHITE.getRGB();
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                img.setRGB(x, y, matrix.get(x, y) ? onColor : offColor);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", out);
        return out.toByteArray();
    }

    // ── Role formatter ─────────────────────────────────────────────────

    private String formatRole(String role) {
        return switch (role) {
            case "ADMIN"              -> "System Administrator";
            case "EXECUTIVE_DIRECTOR" -> "Executive Director";
            case "PROJECT_MANAGER"    -> "Project Manager";
            case "FINANCE"            -> "Finance Officer";
            case "FIELD_OFFICER"      -> "Field Officer";
            default                   -> role;
        };
    }
}
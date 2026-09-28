package com.dogfood.certificate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
public class CertificateGenerator {
    public byte[] generate(String recipientName, String eventName, String track, String rank, UUID certId) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(FontName.HELVETICA_BOLD), 24);
                stream.newLineAtOffset(100, 700);
                String title = (rank != null && !rank.isEmpty()) ? "Certificate of Achievement" : "Certificate of Participation";
                stream.showText(title);
                stream.endText();
                
                stream.beginText();
                stream.setFont(new PDType1Font(FontName.HELVETICA), 16);
                stream.newLineAtOffset(100, 650);
                stream.showText("Awarded to: " + recipientName);
                stream.newLineAtOffset(0, -30);
                stream.showText("For event: " + eventName);
                stream.newLineAtOffset(0, -30);
                stream.showText("Track: " + track);
                if (rank != null && !rank.isEmpty()) {
                    stream.newLineAtOffset(0, -30);
                    stream.showText("Rank: " + rank);
                }
                stream.newLineAtOffset(0, -50);
                stream.showText("Certificate ID: " + certId.toString());
                stream.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}

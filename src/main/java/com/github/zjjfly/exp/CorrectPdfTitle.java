package com.github.zjjfly.exp;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

/**
 * @author Zi JunJie <a href="https://github.com/zjjfly"/>github</a>
 */
public class CorrectPdfTitle {

    public static void main(String[] args) throws IOException {
        Path desktop = Paths.get("/Users/zijunjie/Desktop");

        Path bookDir = desktop.resolve("books");
        Path targetDir = desktop.resolve("corrected-books");
        if (!Files.exists(targetDir)) {
            Files.createDirectory(targetDir);
        }
        try (Stream<Path> files = Files.list(bookDir)) {
            files.forEach(bookPath -> {
                File file = bookPath.toFile();
                String name = file.getName();
                if (!name.endsWith(".pdf")) {
                    return;
                }
                Path target = targetDir.resolve(name);
                try (PDDocument document = Loader.loadPDF(file)) {
                    PDDocumentInformation information = document.getDocumentInformation();
                    name = name.substring(0, name.lastIndexOf(".pdf"));
                    if (name.equals(information.getTitle()) && name.endsWith(
                        information.getSubject())) {
                        return;
                    }
                    information.setTitle(name);
                    information.setSubject(name);
                    document.setDocumentInformation(information);
                    document.setAllSecurityToBeRemoved(true);
                    BufferedOutputStream outputStream = new BufferedOutputStream(
                        Files.newOutputStream(target,
                            StandardOpenOption.WRITE, StandardOpenOption.CREATE), 1024 * 1024);
                    document.save(outputStream);
                    outputStream.close();
                } catch (Exception e) {
                    System.out.println("something wrong in file: " + file.getName());
                }
            });
        }
    }

}

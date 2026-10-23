package com.github.zjjfly.exp;

import net.openhft.chronicle.bytes.MappedBytesStore;
import net.openhft.chronicle.bytes.MappedFile;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.ReferenceOwner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Zi JunJie <a href="https://github.com/zjjfly"/>github</a>
 */
public class MemoryMappedFile {

    public static void main(String[] args) throws IOException {
        final File tempFile = Files.createTempFile("chronicle", "q").toFile();
        try (MappedFile mappedFile = MappedFile.mappedFile(tempFile, 1 << 10, OS.pageSize())) {
            MappedBytesStore mappedBytesStore = mappedFile.acquireByteStore(ReferenceOwner.temporary("test"), 0);
            mappedBytesStore.writeUtf8(0L, "test");
            StringBuilder sb = new StringBuilder();
            long read = mappedBytesStore.readUtf8( 0, sb);
            System.out.println(read);
            System.out.println(sb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

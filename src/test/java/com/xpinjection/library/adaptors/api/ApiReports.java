package com.xpinjection.library.adaptors.api;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Value
@Slf4j
@Builder
public class ApiReports {
    Path coveragePath;
    String changesPath;
    String exportedSpecPath;

    public void clean() {
        try {
            deleteIfExists(changesPath);
            deleteIfExists(exportedSpecPath);
            LOG.info("Delete report {}", coveragePath);
            PathUtils.delete(coveragePath);
        } catch (IOException e) {
            throw new IllegalStateException("Can't clean up API coverage reports", e);
        }
    }

    private void deleteIfExists(String path) throws IOException {
        var file = new File(path);
        if (file.exists()) {
            LOG.info("Delete report {}", path);
            FileUtils.delete(file);
        }
    }
}

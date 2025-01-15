package org.example;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassScanner {

    public static void scanPackage(String packageName, ClassConsumer consumer) {
        String path = packageName.replace('.', '/');
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            if (resource != null) {
                File directory = new File(resource.toURI());
                if (directory.exists()) {
                    for (File file : directory.listFiles()) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = packageName + '.' + file.getName().replace(".class", "");
                            Class<?> cls = Class.forName(className);
                            consumer.accept(cls);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package: " + packageName, e);
        }
    }

    @FunctionalInterface
    public interface ClassConsumer {
        void accept(Class<?> cls);
    }
}

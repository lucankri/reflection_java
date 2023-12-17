package edu.school21.services;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModelsScanner implements Supplier<List<Class<?>>> {
    private final String packageName;

    public ModelsScanner(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public List<Class<?>> get() {
        String directory = packageName.replaceAll("\\.", "/");
        return getClasses(packageName, directory);
    }

    protected List<Class<?>> getClasses(String packageName, String directory) {
        List<Class<?>> result = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = directory.replace("/", ".");
            Enumeration<URL> resources = classLoader.getResources(directory);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                if (resource.getProtocol().equals("file")) {
                    // Обработка для IDE (работающей с файловой системой)
                    File folder = new File(resource.toURI());
                    File[] files = folder.listFiles();

                    if (files != null) {
                        for (File file : files) {
                            if (file.isDirectory()) {
                                String subPackage = packageName + "." + file.getName();
                                String subDirectory = directory + "/" + file.getName();
                                getClasses(subPackage, subDirectory);
                            } else if (file.getName().endsWith(".class")) {
                                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                                Class<?> clazz = Class.forName(className);
                                result.add(clazz);
                            }
                        }
                    }
                } else if (resource.getProtocol().equals("jar")) {
                    // Обработка для .jar файла
                    JarURLConnection connection = (JarURLConnection) resource.openConnection();
                    JarFile jarFile = connection.getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (entryName.endsWith(".class") && entryName.startsWith(directory) && !entryName.contains("$")) {
                            String className = entryName.substring(0, entryName.length() - 6).replace("/", ".");
                            Class<?> clazz = classLoader.loadClass(className);
                            result.add(clazz);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

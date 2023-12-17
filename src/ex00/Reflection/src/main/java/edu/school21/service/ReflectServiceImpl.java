package edu.school21.service;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReflectServiceImpl implements ReflectService {
    Map<String, Class<?>> classes;
    Object object;
    ClassInfo infoObject;

    public ReflectServiceImpl() {
        classes = new HashMap<>();
        String packageName = "edu.school21.classes";
        String path = packageName.replace(".", "/");
        addClassesInDirectory(packageName, path);
    }

    private void addClassesInDirectory(String packageName, String directory) {
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
                                addClassesInDirectory(subPackage, subDirectory);
                            } else if (file.getName().endsWith(".class")) {
                                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                                Class<?> clazz = Class.forName(className);
                                classes.put(className.split("\\.")[className.split("\\.").length - 1], clazz);
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
                            classes.put(className.substring(className.lastIndexOf('.') + 1), clazz);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }













    @Override
    public List<String> getClassNames() {
        List<String> classNames = new ArrayList<>();
        for (String key : classes.keySet()) {
            classNames.add(classes.get(key).getSimpleName());
        }
        return classNames;
    }

    @Override
    public ClassInfo getClassInfo(String className) {
        Class<?> clazz = classes.get(className);
        if (clazz == null) return null;
        ClassInfo classInfo = new ClassInfo();
        List<Field> fields = new ArrayList<>();
        java.lang.reflect.Field[] classFields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field f : classFields) {
            Field field = new Field();
            field.type = f.getType().getSimpleName();
            field.name = f.getName();
            fields.add(field);
        }
        classInfo.fields = fields;

        List<Method> methods = new LinkedList<>();
        List<java.lang.reflect.Method> classMethods = new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()));
        List<java.lang.reflect.Method> superClassMethods = new ArrayList<>(Arrays.asList(clazz.getSuperclass().getMethods()));
        for (java.lang.reflect.Method superClassMethod : superClassMethods) {
            for (java.lang.reflect.Method classMethod : classMethods) {
                if (classMethod.getName().equals(superClassMethod.getName()) &&
                        Arrays.equals(classMethod.getParameterTypes(), superClassMethod.getParameterTypes())) {
                    classMethods.remove(classMethod);
                    break;
                }
            }
        }
        for (java.lang.reflect.Method m : classMethods) {
                Method method = new Method();
                method.returnType = m.getReturnType().getSimpleName();
                method.name = m.getName();
                List<String> parameters = new ArrayList<>();
                StringBuilder signatureNormalized = new StringBuilder(method.returnType + " " + method.name + "(");
                for (Class<?> parameter : m.getParameterTypes()) {
                    parameters.add(parameter.getSimpleName());
                    signatureNormalized.append(parameter.getSimpleName()).append(", ");
                }
                signatureNormalized.append(")");
                int lastIndex = signatureNormalized.lastIndexOf(", ");
                if (lastIndex != -1) {
                    signatureNormalized.delete(lastIndex, lastIndex + 2);
                }
                method.signatureNormalized = signatureNormalized.toString();
                method.argumentTypes = parameters;
                method.method = m;
                methods.add(method);
        }
        classInfo.methods = methods;

        return classInfo;
    }

    @Override
    public Object createObject(String objectName) {
        Class<?> clazz = classes.get(objectName);
        if (clazz == null) return null;
        infoObject = getClassInfo(clazz.getSimpleName());
        Object newObject;
        try {
            newObject = clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
        object = newObject;
        return newObject;
    }

    @Override
    public List<String> getFieldNames() {
        if (infoObject == null) return null;
        List<String> result = new ArrayList<>();
        for (Field field : infoObject.fields) {
            result.add(field.name);
        }
        return result;
    }

    @Override
    public void setFieldValue(String name, String value) throws NumberFormatException, NoSuchFieldException {
        if (object == null) return;
        java.lang.reflect.Field fieldObject = object.getClass().getDeclaredField(name);
        fieldObject.setAccessible(true);
        Class<?> fieldObjectType = fieldObject.getType();
        Object fieldValue = convertFromString(value, fieldObjectType);
        try {
            fieldObject.set(object, fieldValue);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private Object convertFromString(String str, Class<?> type) {
        Object fieldValue = str;
        if (type == int.class || type == Integer.class) {
            fieldValue = Integer.parseInt(str);
        } else if (type == double.class || type == Double.class) {
            fieldValue = Double.parseDouble(str);
        } else if (type == long.class || type == Long.class) {
            fieldValue = Long.parseLong(str);
        } else if (type == boolean.class || type == Boolean.class) {
            fieldValue = Boolean.parseBoolean(str);
        }
        return fieldValue;
    }

    @Override
    public Object callMethod(String methodString, String... values) throws NoSuchMethodException, NumberFormatException {
        Matcher ma = Pattern.compile("\\s*(\\w+)\\s*\\(\\s*(.*)\\s*\\)\\s*").matcher(methodString);
        if (!ma.matches()) {
            throw new NoSuchMethodException("Does not match the format of the method!");
        }
        String methodName = ma.group(1);
        String paramsStr = ma.group(2);
        List<String> params = Arrays.asList(paramsStr.split("\\s*,\\s*"));
        for (Method m : infoObject.methods) {
            if (m.name.equals(methodName)) {
                if (m.argumentTypes.equals(params)) {
                    List<Object> valuesObject = new ArrayList<>();
                    Class<?>[] types = m.method.getParameterTypes();
                    for (int i = 0; i < types.length; i++) {
                        convertFromString(values[i], types[i]);
                        valuesObject.add(convertFromString(values[i], types[i]));
                    }
                    try {
                        return m.method.invoke(object, valuesObject.toArray());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        throw new NoSuchMethodException("There is no such method");
    }

    @Override
    public Method getMethod(String methodSignature) {
        ReflectService.Method result = null;
        for (ReflectService.Method m : infoObject.methods) {
            int indexReturnType = m.signatureNormalized.indexOf(" ");
            String tmpMethod = (m.signatureNormalized.substring(0, indexReturnType + 1) + methodSignature).replaceAll("\\s", "");
            String tmpSignature = m.signatureNormalized.replaceAll("\\s", "");
            if (tmpSignature.equals(tmpMethod)) {
                result = m;
                break;
            }
        }
        return result;
    }

    @Override
    public Field getField(String methodName) {
        for (Field field : infoObject.fields) {
            if (field.name.equals(methodName)) {
                return field;
            }
        }
        return null;
    }
}

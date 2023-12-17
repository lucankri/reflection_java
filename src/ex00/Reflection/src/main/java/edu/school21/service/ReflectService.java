package edu.school21.service;

import java.util.List;


public interface ReflectService {

//    class NoSuchMethodException extends ReflectiveOperationException {
//        public NoSuchMethodException(String err) {
//            super(err);
//        }
//    }

    class Field {
        public String type;
        public String name;
    }

    class Method {
        public String returnType;
        public String name;
        public List<String> argumentTypes;
        public String signatureNormalized;
        public java.lang.reflect.Method method;
    }

    class ClassInfo {
        public List<Field> fields;
        public List<Method> methods;
    }

    List<String> getClassNames();
    ClassInfo getClassInfo(String className);
    Object createObject(String objectName);
    List<String> getFieldNames();
    void setFieldValue(String name, String value) throws NumberFormatException, NoSuchFieldException;
    Object callMethod(String methodString, String... values) throws NoSuchMethodException, NumberFormatException;
    Method getMethod(String methodSignature);
    Field getField(String methodName);
}

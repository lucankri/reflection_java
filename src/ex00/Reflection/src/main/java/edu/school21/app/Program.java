package edu.school21.app;

import edu.school21.service.ReflectService;
import edu.school21.service.ReflectServiceImpl;

import java.util.Scanner;

public class Program {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ReflectService reflectService = new ReflectServiceImpl();

    public static void main(String[] args) {
        System.out.println("Classes:");
        for (String clazz : reflectService.getClassNames()) {
            System.out.println("\t- " + clazz);
        }
        System.out.println("---------------------");

        System.out.println("Enter class name:");
        String selectClassName;
        ReflectService.ClassInfo infoClasses;
        while (true) {
            System.out.print("-> ");
            selectClassName = scanner.nextLine();
            infoClasses = reflectService.getClassInfo(selectClassName);
            if (infoClasses != null) {
                break;
            }
            System.out.println("Try again:");
        }
        System.out.println("fields:");
        for (ReflectService.Field fields : infoClasses.fields) {
            System.out.println("\t" + fields.type + " " + fields.name);
        }

        System.out.println("methods:");
        for (ReflectService.Method method : infoClasses.methods) {
            System.out.println("\t" + method.signatureNormalized);
        }
        System.out.println("---------------------");

        System.out.println("Let’s create an object.");
        Object object = reflectService.createObject(selectClassName);
        for (ReflectService.Field field : infoClasses.fields) {
            System.out.print(field.name + ":\n-> ");
            String value;
            value = scanner.nextLine();
            try {
                reflectService.setFieldValue(field.name, value);
            } catch (NoSuchFieldException | NumberFormatException e) {
                while (true) {
                    System.out.println(e.getMessage());
                    System.out.print("Try again:\n-> ");
                    value = scanner.nextLine();
                    try {
                        reflectService.setFieldValue(field.name, value);
                        break;
                    } catch (NoSuchFieldException | NumberFormatException ignore) {}
                }
            }
        }
        System.out.println("Object created: " + object);
        System.out.println("---------------------");

        System.out.print("Enter name of the field for changing:\n-> ");
        while (true) {
            String nameFieldStr = scanner.nextLine();
            ReflectService.Field field = reflectService.getField(nameFieldStr);
            if (field != null) {
                System.out.print("Enter " + field.type + " value:\n-> ");
                String value = scanner.nextLine();
                try {
                    reflectService.setFieldValue(nameFieldStr, value);
                    break;
                } catch (NoSuchFieldException | NumberFormatException e) {
                    System.out.println(e.getMessage());
                    System.out.print("Try again:\n-> ");
                }
            } else {
                System.out.println("There is no such field!");
                System.out.print("Try again:\n-> ");
            }
        }
        System.out.println("Object updated: " + object);
        System.out.println("---------------------");

        System.out.print("Enter name of the method for call:\n-> ");
        boolean flagLoop = true;
        String methodStr = null;
        ReflectService.Method methodService = null;
        while(flagLoop) {
            methodStr = scanner.nextLine();
            methodService = reflectService.getMethod(methodStr);
            if (methodService == null) {
                System.out.print("Try again:\n-> ");
            } else {
                flagLoop = false;
            }
        }
        System.out.print("Enter ");
        for (int i = 0; i < methodService.argumentTypes.size(); i++) {
            if (i == methodService.argumentTypes.size() - 1) {
                System.out.print(methodService.argumentTypes.get(i) + " ");
            } else {
                System.out.print(methodService.argumentTypes.get(i) + ", ");
            }
        }
        System.out.print("value:\n->");
        while(true) {
            String valuesStr = scanner.nextLine();
            String[] values = (valuesStr.replaceAll("\\s", "")).split(",");
            try {
                Object returnMethod = reflectService.callMethod(methodStr, values);
                if (returnMethod != null) {
                    System.out.println("Method returned:\n" + returnMethod);
                }
                break;
            } catch (NoSuchMethodException | NumberFormatException e) {
                System.out.println(e.getMessage());
                System.out.print("Try again:\n-> ");
            }
        }
    }
}

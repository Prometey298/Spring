package org.example;

/**
 * Основной класс приложения, который запускает программу.
 */
public class Main {
    public static void main(String[] args) {
        IntensiveContext context = new IntensiveContext("org.example");
        SomeClass1 class1 = context.getObject(SomeClass1.class);
        class1.run(); // Должен вывести сообщение в консоль
    }
}

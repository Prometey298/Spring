package org.example;

import java.lang.reflect.*;
import java.util.*;

/**
 * Контекст для управления зависимостями и инстанцированием классов.
 */
public class IntensiveContext {
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final String packageName;

    /**
     * Конструктор, инициализирующий контекст с указанным именем пакета.
     *
     * @param packageName имя пакета для сканирования компонентов
     */
    protected IntensiveContext(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Получает объект указанного типа из контекста.
     *
     * @param type класс типа объекта, который нужно получить
     * @param <T> тип объекта
     * @return экземпляр объекта указанного типа
     */
    public <T> T getObject(Class<T> type) {
        if (!instances.containsKey(type)) {
            List<Class<?>> implementations = findImplementations(type);
            if (implementations.isEmpty()) {
                throw new IllegalStateException("No implementations found for interface: " + type.getName());
            }
            if (implementations.size() > 1) {
                throw new IllegalStateException("Multiple implementations found for interface: " + type.getName());
            }

            Class<?> implementationClass = implementations.get(0);
            T instance = (T) createInstance(implementationClass);
            instances.put(type, instance);
        }
        return (T) instances.get(type);
    }

    /**
     * Находит реализации указанного типа в заданном пакете.
     *
     * @param type класс интерфейса для поиска реализаций
     * @return список классов, реализующих указанный интерфейс
     */
    private List<Class<?>> findImplementations(Class<?> type) {
        List<Class<?>> implementations = new ArrayList<>();
        ClassScanner.scanPackage(packageName, cls -> {
            if (type.isAssignableFrom(cls) && cls.isAnnotationPresent(IntensiveComponent.class)) {
                implementations.add(cls);
            }
        });
        return implementations;
    }

    /**
     * Создает экземпляр указанного класса и выполняет инъекцию зависимостей.
     *
     * @param clazz класс для создания экземпляра
     * @return созданный экземпляр объекта
     */
    private Object createInstance(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            injectDependencies(instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    /**
     * Выполняет инъекцию зависимостей в указанный экземпляр.
     *
     * @param instance объект, в который нужно выполнить инъекцию зависимостей
     */
    private void injectDependencies(Object instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(IntensiveComponent.class)) {
                Class<?> fieldType = field.getType();
                Object dependency = getObject(fieldType);
                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency into " + instance.getClass().getName(), e);
                }
            }
        }
    }
}

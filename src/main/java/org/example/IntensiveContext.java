package org.example;

import java.lang.reflect.*;
import java.util.*;

public class IntensiveContext {
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final String packageName;

    public IntensiveContext(String packageName) {
        this.packageName = packageName;
    }

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

    private List<Class<?>> findImplementations(Class<?> type) {
        List<Class<?>> implementations = new ArrayList<>();
        ClassScanner.scanPackage(packageName, cls -> {
            if (type.isAssignableFrom(cls) && cls.isAnnotationPresent(IntensiveComponent.class)) {
                implementations.add(cls);
            }
        });
        return implementations;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            injectDependencies(instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

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

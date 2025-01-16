package org.example;

import java.lang.annotation.*;

/**
 * Аннотация для пометки классов как компонентов,
 * которые могут быть инстанцированы через контекст.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IntensiveComponent {
}

package cn.myforever.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加了这个的属性，会自动根据首字母小写注入对象
 * @author forever
 *
 */
@Target(value= {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Source {

}

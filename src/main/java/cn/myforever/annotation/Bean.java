package cn.myforever.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 加入了这个注解的将会自动初始化bean
 * @author forever
 *
 */
//在类，接口enum上声明
@Target(value= {ElementType.TYPE})
//运行期有效
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

}

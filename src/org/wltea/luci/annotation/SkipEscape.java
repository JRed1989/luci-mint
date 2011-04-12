/**
 * 
 */
package org.wltea.luci.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * lucene索引字段Annotation 
 * 标注Bean的字段不需要进行XML Escape字符过滤
 * @author linliangyi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SkipEscape {

}

package com.matyrobbrt.dataobjects.api.registry;

import org.objectweb.asm.Type;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a class (with a no-arg public constructor)
 * that is an instance of {@link ObjectFactory}
 * or a static field whose underlying type is an {@link ObjectFactory} with
 * this annotation in order to register the factory.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface BindFactory {
    Type TYPE = Type.getType(BindFactory.class);

    /**
     * The name of the factory. <br>
     * It is <strong><i>strongly</i></strong> recommended
     * that the name of your factory is prefixed with
     * your mod id ({@code examplemod.exampleitem}).
     *
     * @return the name of the factory
     */
    String value();

    /**
     * The {@link DataObjectRegistry#getResourceKey() key} of the registry. <br>
     * Example: {@link net.minecraft.core.Registry#ITEM} -> minecraft:item
     *
     * @return the key of the registry
     */
    String registry();
}

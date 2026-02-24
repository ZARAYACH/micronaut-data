/*
 * Copyright 2017-2026 original authors
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package io.micronaut.data.processor.jpa.metamodel;

import io.micronaut.core.naming.NameUtils;
import io.micronaut.inject.ast.PropertyElement;
import io.micronaut.sourcegen.model.*;
import jakarta.annotation.Generated;

import javax.lang.model.element.Modifier;
import javax.persistence.metamodel.*;
import java.util.List;

/**
 *
 */
public class JpaMetamodelProcessor {
    /**
     * JPA meta model class def generator .
     *
     * @param packageName
     * @param elementType
     * @param properties
     * @return Jpa metamodel class definition builder .
     */
    public static ClassDef.ClassDefBuilder createJpaMetaModelClassDefBuilder(String packageName,
                                                                             ClassTypeDef elementType,
                                                                             List<PropertyElement> properties) {
        String localBinaryName = elementType.getName().startsWith(packageName + ".")
            ? elementType.getName().substring(packageName.isEmpty() ? 0 : packageName.length() + 1)
            : elementType.getName();
        String baseName = elementType.isInner() ? localBinaryName.replace("$", "") : elementType.getSimpleName();
        String metaModelClassSimpleName = baseName + "_";
        String metaModelClassName = packageName + "." + metaModelClassSimpleName;

//        List<TypeDef.TypeVariable> typeArguments = List.of();
//        if (elementType instanceof ClassTypeDef.Parameterized parameterizedType) {
//            typeArguments = parameterizedType.typeArguments()
//                .stream().filter(td -> td instanceof TypeDef.TypeVariable)
//                .map(TypeDef.TypeVariable.class::cast)
//                .toList();
//        }
//
//        ClassTypeDef metaModelClassType;
//
//        if (typeArguments.isEmpty()) {
//            metaModelClassType = ClassTypeDef.of(metaModelClassName);
//        } else {
//            metaModelClassType = TypeDef.parameterized(
//                ClassTypeDef.of(metaModelClassName),
//                typeArguments.toArray(TypeDef[]::new)
//            );
//        }

        ClassDef.ClassDefBuilder classDefBuilder = ClassDef.builder(metaModelClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(AnnotationDef.builder(Generated.class)
                .addMember("value", JpaMetamodelProcessor.class).build())
            .addAnnotation(AnnotationDef.builder(StaticMetamodel.class)
                .addMember("value", elementType.getName() + ".class").build());

        for (PropertyElement beanProperty : properties) {
            classDefBuilder.addField(createConstantPropertyName(beanProperty));
            classDefBuilder.addField(createAttributeField(beanProperty));
        }
        classDefBuilder.addField(createEntityTypeField(elementType));
        classDefBuilder.addMethod(MethodDef.constructor().build());
        return classDefBuilder;
    }

    /**
     * @param elementType
     * @return FieldDef
     */
    private static FieldDef createEntityTypeField(ClassTypeDef elementType) {
        return FieldDef.builder("class_")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
            .ofType(TypeDef.parameterized(
                ClassTypeDef.of(EntityType.class),
                elementType)).build();
    }

    /**
     * @param beanProperty
     * @return FieldDef
     */
    private static FieldDef createConstantPropertyName(PropertyElement beanProperty) {
        return FieldDef.builder(NameUtils.underscoreSeparate(beanProperty.getSimpleName()))
            .ofType(TypeDef.STRING)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
            .initializer(ExpressionDef.constant(beanProperty.getSimpleName()))
            .build();
    }

    /**
     * @param beanProperty
     * @return FieldDef
     */
    private static FieldDef createAttributeField(PropertyElement beanProperty) {
        FieldDef.FieldDefBuilder attributeDefBuilder = FieldDef.builder(beanProperty.getName())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.VOLATILE);

        TypeDef typeDef = switch (beanProperty.getType().getName()) {
            case "java.util.Collection" -> TypeDef.parameterized(
                ClassTypeDef.of(CollectionAttribute.class),
                TypeDef.of(beanProperty.getType()), TypeDef.of(beanProperty.getType().getTypeArguments().get("E")));
            case "java.util.Set" -> TypeDef.parameterized(
                ClassTypeDef.of(SetAttribute.class),
                TypeDef.of(beanProperty.getType()), TypeDef.of(beanProperty.getType().getTypeArguments().get("E")));
            case "java.util.List" -> TypeDef.parameterized(
                ClassTypeDef.of(ListAttribute.class),
                TypeDef.of(beanProperty.getType()), TypeDef.of(beanProperty.getType().getTypeArguments().get("E")));
            case "java.util.Map" -> TypeDef.parameterized(
                ClassTypeDef.of(MapAttribute.class),
                TypeDef.of(beanProperty.getType()),
                TypeDef.of(beanProperty.getType().getTypeArguments().get("K")), TypeDef.of(beanProperty.getType().getTypeArguments().get("V")));
            default -> TypeDef.parameterized(
                ClassTypeDef.of(SingularAttribute.class),
                TypeDef.of(beanProperty.getType()));
        };
        return attributeDefBuilder.ofType(typeDef).build();
    }
}

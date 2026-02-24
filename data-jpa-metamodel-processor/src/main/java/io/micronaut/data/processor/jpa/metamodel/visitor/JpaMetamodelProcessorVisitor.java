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
package io.micronaut.data.processor.jpa.metamodel.visitor;


import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.processor.jpa.metamodel.JpaMetamodelProcessor;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.PropertyElement;
import io.micronaut.inject.processing.ProcessingException;
import io.micronaut.inject.visitor.TypeElementQuery;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.sourcegen.generator.SourceGenerator;
import io.micronaut.sourcegen.generator.SourceGenerators;
import io.micronaut.sourcegen.model.ClassDef;
import io.micronaut.sourcegen.model.ClassTypeDef;

import javax.persistence.metamodel.StaticMetamodel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Internal
public final class JpaMetamodelProcessorVisitor implements TypeElementVisitor<Object, Object> {

    private Set<String> processed = new HashSet<>();

    public JpaMetamodelProcessorVisitor() {
    }

    /**
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationNames() {
        return new HashSet<>(Arrays.asList("jakarta.persistence.Entity",
            "jakarta.persistence.MappedSuperClass",
            "jakarta.persistence.Embeddable"));
    }

    /**
     * @param element
     * @param context
     */
    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        if (processed.contains(element.getName())) {
            return;
        }
        try {
            List<PropertyElement> properties = element.getBeanProperties();
            ClassTypeDef elementType = ClassTypeDef.of(element);

            ClassDef.ClassDefBuilder builder = JpaMetamodelProcessor.createJpaMetaModelClassDefBuilder(
                element.getPackageName(),
                elementType,
                properties);
            ClassDef builderDef = builder.build();
            SourceGenerator sourceGenerator = SourceGenerators.findByLanguage(context.getLanguage()).orElse(null);
            if (sourceGenerator == null) {
                return;
            }
            processed.add(element.getName());
            sourceGenerator.write(builderDef, context, element);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            SourceGenerators.handleFatalException(
                element,
                StaticMetamodel.class,
                e,
                exception -> {
                    processed.remove(element.getName());
                    throw exception;
                }
            );
        }
    }

    /**
     * @param visitorContext
     */
    @Override
    public void start(VisitorContext visitorContext) {
        this.processed = new HashSet<>();
    }

    /**
     * @return
     */
    @Override
    public @NonNull VisitorKind getVisitorKind() {
        return VisitorKind.ISOLATING;
    }

    /**
     * @return
     */
    @Override
    public TypeElementQuery query() {
        return TypeElementQuery.onlyClass();
    }

    /**
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}

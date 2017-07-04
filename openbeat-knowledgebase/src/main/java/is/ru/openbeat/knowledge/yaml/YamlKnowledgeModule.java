/*
 * OpenBEAT
 *
 * Arni Hermann Reynisson     arnir06@ru.is
 * Eirikur Ari Petursson      eirikurp06@ru.is
 * Gudleifur Kristjansson     gudleifur05@ru.is
 * Hannes Hogni Vilhjalmsson  hannes@ru.is
 *
 * Copyright(c) 2009 Center for Analysis and Design of Intelligent Agents
 *                   Reykjavik University
 *                   All rights reserved
 *
 *                   http://cadia.ru.is/
 *
 * Based on BEAT, Copyright(c) 2000-2001 by MIT Media Lab,
 * developed by Hannes Vilhjalmsson, Timothy Bickmore, Yang Gao and Justine Cassell
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of its copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package is.ru.openbeat.knowledge.yaml;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import is.ru.openbeat.behavior.Arm;
import is.ru.openbeat.behavior.GestureBehavior;
import is.ru.openbeat.knowledge.*;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * Class <tt>YamlKnowledgeModule</tt> handles initialation of the knowledge base with a Yaml input
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public class YamlKnowledgeModule extends AbstractModule {
    protected void configure() {
        bind(IKnowledgeBase.class).to(KnowledgeBase.class);
    }

    @Named("yaml.reader")
    @Provides
    @Singleton
    public Reader createReader(@Named("kb.file") String filename) throws FileNotFoundException {
        return new FileReader(filename);
    }

    @Provides
    @Singleton
    public KnowledgeBaseHolder createHolder(@Named("yaml.reader") Reader reader) {
        final Constructor constructor = new Constructor(KnowledgeBaseHolder.class);
        final TypeDescription holderDescription = new TypeDescription(KnowledgeBaseHolder.class);
        holderDescription.putListPropertyType("types", KnowledgeType.class);
        holderDescription.putListPropertyType("instances", KnowledgeInstance.class);
        holderDescription.putListPropertyType("scenes", KnowledgeScene.class);
        holderDescription.putListPropertyType("gestures", GestureBehavior.class);
        constructor.addTypeDescription(holderDescription);

        final TypeDescription instanceDescription = new TypeDescription(KnowledgeScene.class);
        instanceDescription.putMapPropertyType("attributes", String.class, String.class);
        constructor.addTypeDescription(instanceDescription);

        final TypeDescription sceneDescription = new TypeDescription(KnowledgeScene.class);
        sceneDescription.putListPropertyType("persons", KnowledgePerson.class);
        constructor.addTypeDescription(sceneDescription);

        final TypeDescription typeDescription = new TypeDescription(KnowledgeType.class);
        typeDescription.putListPropertyType("features", KnowledgeFeature.class);
        constructor.addTypeDescription(typeDescription);

        final TypeDescription gestureDescription = new TypeDescription(GestureBehavior.class);
        gestureDescription.putListPropertyType("arms", Arm.class);
        constructor.addTypeDescription(gestureDescription);

        final Yaml yaml = new Yaml(new Loader(constructor));
        return (KnowledgeBaseHolder) yaml.load(reader);
    }
} 

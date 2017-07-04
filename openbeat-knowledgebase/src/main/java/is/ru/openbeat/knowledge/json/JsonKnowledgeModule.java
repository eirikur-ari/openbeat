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

package is.ru.openbeat.knowledge.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import is.ru.openbeat.behavior.Arm;
import is.ru.openbeat.behavior.GestureBehavior;
import is.ru.openbeat.knowledge.*;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Class <tt>JsonKnowledgeModule</tt> handles initialation of the knowledge base with a Json input
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public class JsonKnowledgeModule extends AbstractModule {
    protected void configure() {
        bind(IKnowledgeBase.class).to(KnowledgeBase.class);
    }

    @Provides
    @Singleton
    public KnowledgeBaseHolder createHolder(@Named("json.reader") Reader reader) {
        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(IKnowledgeFeature.class, new JsonDeserializer<IKnowledgeFeature>() {
                public IKnowledgeFeature deserialize(JsonElement json, Type typeOfT,
                                                     JsonDeserializationContext context) throws JsonParseException {
                    return context.deserialize(json, KnowledgeFeature.class);
                }
            })
            .registerTypeAdapter(IKnowledgeInstance.class, new JsonDeserializer<IKnowledgeInstance>() {
                public IKnowledgeInstance deserialize(JsonElement json, Type typeOfT,
                                                      JsonDeserializationContext context) throws JsonParseException {
                    return context.deserialize(json, KnowledgeInstance.class);
                }
            })
            .registerTypeAdapter(IKnowledgeScene.class, new JsonDeserializer<IKnowledgeScene>() {
                public IKnowledgeScene deserialize(JsonElement json, Type typeOfT,
                                                      JsonDeserializationContext context) throws JsonParseException {
                    return context.deserialize(json, KnowledgeScene.class);
                }
            })
            .registerTypeAdapter(IKnowledgePerson.class, new JsonDeserializer<IKnowledgePerson>() {
                public IKnowledgePerson deserialize(JsonElement json, Type typeOfT,
                                                      JsonDeserializationContext context) throws JsonParseException {
                    return context.deserialize(json, KnowledgePerson.class);
                }
            })
            .registerTypeAdapter(IKnowledgeType.class, new JsonDeserializer<IKnowledgeType>() {
                public IKnowledgeType deserialize(JsonElement json, Type typeOfT,
                                                  JsonDeserializationContext context) throws JsonParseException {
                    return context.deserialize(json, KnowledgeType.class);
                }
            })
            .registerTypeAdapter(IGesture.class, new JsonDeserializer<IGesture>() {
                public IGesture deserialize(JsonElement json, Type typeOfT,
                                            JsonDeserializationContext context) throws JsonParseException {
                    return context.deserialize(json, GestureBehavior.class);
                }
            })
            .registerTypeAdapter(IArm.class, new JsonDeserializer<IArm>() {
                public IArm deserialize(JsonElement json, Type typeOfT,
                                            JsonDeserializationContext context) throws JsonParseException {
                    return context.deserialize(json, Arm.class);
                }
            })
            .create();

        return gson.fromJson(reader, KnowledgeBaseHolder.class);
    }
}

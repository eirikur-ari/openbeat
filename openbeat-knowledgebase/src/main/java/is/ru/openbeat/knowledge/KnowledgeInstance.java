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

package is.ru.openbeat.knowledge;

import static com.google.common.collect.Maps.newHashMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import com.google.common.base.Function;
import com.google.common.base.Nullable;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class <tt>KnowledgeInstance</tt> withholds attributes of instances in the knowledge base
 *
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
public class KnowledgeInstance implements IKnowledgeInstance {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeInstance.class);

    private String instanceOf;
    private String id;
    private Map<String, String> attributes = newHashMap();

    public KnowledgeInstance() {
    }

    public KnowledgeInstance(String instanceOf, String id, Map<String, String> attributes) {
        this.instanceOf = instanceOf;
        this.id = id;
        this.attributes = attributes;
    }

    public String getInstanceOf() {
        return instanceOf;
    }

    public void setInstanceOf(String instanceOf) {
        this.instanceOf = instanceOf;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    // Hack needed for YAML deserialization which doesn't seem to read all attributes as Strings (e.g. some are Integers)
    public String getValue(String feature) {
        if (attributes.get(feature) == null) {
            return null;
        } else {
            return String.valueOf(attributes.get(feature));
        }
    }

    // Hack needed for YAML deserialization which doesn't seem to read all attributes as Strings (e.g. some are Integers)
    public Iterable<String> getAllValues() {
        return transform(newArrayList(attributes.values()), new Function<Object, String>() {
            public String apply(@Nullable Object from) {
                if (from == null) {
                    return "";
                } else {
                    log.trace("Mapping value: {}", String.valueOf(from));
                    return String.valueOf(from);
                }
            }
        });
    }

    /**
     * Returns the first value that is not typical for an instance of this type
     *
     * @param knowledgeBase the knowledge base used
     */
    public String getSurprisingValue(IKnowledgeBase knowledgeBase) {
        for (IKnowledgeType type : knowledgeBase.getAllTypes()) {
            if (type.getName().equals(instanceOf)) {
                for (IKnowledgeFeature feature : type.getFeatures()) {
                    if (!feature.isTypical(getValue(feature.getName()))) {
                        return getValue(feature.getName());
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KnowledgeInstance that = (KnowledgeInstance) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (instanceOf != null ? !instanceOf.equals(that.instanceOf) : that.instanceOf != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = instanceOf != null ? instanceOf.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KnowledgeInstance{" +
            "instanceOf='" + instanceOf + '\'' +
            ", id='" + id + '\'' +
            ", attributes=" + attributes +
            '}';
    }
}
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

package is.ru.openbeat.model;

import static com.google.common.collect.Lists.newArrayList;
import is.ru.openbeat.behavior.IBehavior;
import is.ru.openbeat.behavior.IBehaviorContainer;

import java.util.List;

/**
 * Class <tt>Constituent</tt> is a group of words with special syntax meaning.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public class Constituent implements IFeatureStructure, IBehaviorContainer {
    private final IConstituentAttribute type;
    private final List<IFeatureStructure> features = newArrayList();
    private final List<IBehavior> behaviors = newArrayList();

    public Constituent(IConstituentAttribute type) {
        this.type = type;
    }

    public IConstituentAttribute getType() {
        return type;
    }

    public void addFeature(IFeatureStructure constituent) {
        features.add(constituent);
    }

    public Iterable<IFeatureStructure> getFeatures() {
        return features;
    }

    public void addBehavior(IBehavior behavior) {
        behaviors.add(behavior);
    }

    public void removeBehavior(IBehavior behavior) {
        behaviors.remove(behavior);
    }

    public List<IBehavior> getBehaviors() {
        return behaviors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Constituent that = (Constituent) o;

        if (behaviors != null ? !behaviors.equals(that.behaviors) : that.behaviors != null) return false;
        if (features != null ? !features.equals(that.features) : that.features != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (features != null ? features.hashCode() : 0);
        result = 31 * result + (behaviors != null ? behaviors.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "type='" + type + '\'' +
            ", features=" + features +
            ", behaviors=" + behaviors +
            '}';
    }

    public static interface ConstituentBuilder extends Builder<Constituent> {
        ConstituentBuilder features(IFeatureStructure... featureStructure);

        ConstituentBuilder behaviors(IBehavior... behavior);
    }

    public static ConstituentBuilder with(IConstituentAttribute type) {
        return BuilderFactory.create(new Constituent(type), ConstituentBuilder.class);
    }
}

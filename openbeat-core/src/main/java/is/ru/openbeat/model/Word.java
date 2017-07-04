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

import com.google.common.base.Nullable;
import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import is.ru.openbeat.behavior.IBehavior;
import is.ru.openbeat.behavior.IBehaviorContainer;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Class <tt>Word</tt> holds information about a word in an {@link Utterance utterance}. More specifically, the token
 * read from the {@link is.ru.openbeat.pipeline.INlpSource nlp source}, the attributes which have been normalized in the
 * nlp source as well and the lemma from an {@link is.ru.openbeat.discourse.IDictionarySource dictionary source}.
 * <p/>
 * Additional attributes are begin and end time which are read from an {@link is.ru.openbeat.pipeline.ITimingSource
 * speech timing source}, contrast words and the behaviors associated with it.
 * <p/>
 * Note: word comparing words, referential equality (infix == operator) should be used to compare specifically two
 * different instances of being different words in an utterance, while {@link Word#equals(Object)} and {@link
 * Word#hashCode()} can be used for comparison based on the token, lemma and attributes alone.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public class Word implements IFeatureStructure, IBehaviorContainer {
    private final String token;
    private final Set<IWordAttribute> attributes;
    private final List<IBehavior> behaviors = newArrayList();
    private Set<Word> contrasts = newHashSet();
    private String lemma;

    private Float beginTime;
    private Float endTime;

    public Word(String token, Set<IWordAttribute> attributes) {
        this.token = token;
        this.attributes = attributes;
    }

    public Word(String token, IWordAttribute... attribute) {
        this.token = token;
        this.attributes = newHashSet(attribute);
    }

    public String getToken() {
        return token;
    }

    public Set<Word> getContrasts() {
        return contrasts;
    }

    public void addContrast(Word contrast) {
        contrasts.add(contrast);
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public Float getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Float beginTime) {
        this.beginTime = beginTime;
    }

    public Float getEndTime() {
        return endTime;
    }

    public void setEndTime(Float endTime) {
        this.endTime = endTime;
    }

    public void addAttribute(IWordAttribute attribute) {
        attributes.add(attribute);
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

    public boolean is(IWordAttribute attribute) {
        return attributes.contains(attribute);
    }

    public boolean matches(Word word) {
        String thisWord = lemma != null ? lemma : token;
        String thatWord = word.getLemma() != null ? word.getLemma() : word.getToken();

        IWordAttribute thisGender = findAttributeOf(WordAttributeType.Gender.class);
        IWordAttribute thatGender = word.findAttributeOf(WordAttributeType.Gender.class);

        IWordAttribute thisNumber = findAttributeOf(WordAttributeType.Number.class);
        IWordAttribute thatNumber = word.findAttributeOf(WordAttributeType.Number.class);
        return thisWord.equals(thatWord) || (thisGender != null && thisNumber != null && thisGender.equals(
            thatGender) && thisNumber.equals(thatNumber));
    }

    public IWordAttribute findAttributeOf(final Class<? extends Annotation> klass) {
        try {
            return find(attributes, new Predicate<IWordAttribute>() {
                public boolean apply(@Nullable IWordAttribute input) {
                    return input.getClass().getAnnotation(klass) != null;
                }
            });
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Word word = (Word) o;

        if (attributes != null ? !attributes.equals(word.attributes) : word.attributes != null) return false;
        if (lemma != null ? !lemma.equals(word.lemma) : word.lemma != null) return false;
        if (token != null ? !token.equals(word.token) : word.token != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        result = 31 * result + (lemma != null ? lemma.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Word{" +
            "token='" + token + '\'' +
            ", attributes=" + attributes +
            ", behaviors=" + behaviors +
            ", contrasts=" + contrasts +
            ", lemma='" + lemma + '\'' +
            ", beginTime=" + beginTime +
            ", endTime=" + endTime +
            '}';
    }

    public static interface WordBuilder extends Builder<Word> {
        WordBuilder attributes(IWordAttribute... attribute);

        WordBuilder behaviors(IBehavior... behavior);

        WordBuilder contrasts(Word... contrast);

        WordBuilder lemma(String lemma);

        WordBuilder beginTime(Float beginTime);

        WordBuilder endTime(Float endTime);
    }

    public static WordBuilder with(String token) {
        return BuilderFactory.create(new Word(token), WordBuilder.class);
    }
}

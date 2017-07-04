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

package is.ru.openbeat.opennlp;

import static com.google.common.collect.Sets.newHashSet;
import com.google.inject.Singleton;
import is.ru.openbeat.discourse.ITagResolver;
import is.ru.openbeat.model.IWordAttribute;
import static is.ru.openbeat.model.NumberAttribute.PLURAL;
import static is.ru.openbeat.model.NumberAttribute.SINGULAR;
import static is.ru.openbeat.model.PersonAttribute.THIRD;
import is.ru.openbeat.model.WordClassAttribute;
import static is.ru.openbeat.model.WordClassAttribute.PUNCTUATION;

import java.util.Set;

@Singleton
class OpenNlpTagResolver implements ITagResolver {
    public Set<IWordAttribute> resolveAttributes(String tag) {
        final Set<IWordAttribute> attributes = newHashSet();
        final IWordAttribute wordClass = resolveWordClass(tag);
        final IWordAttribute seperator = resolveSeperator(tag);
        final IWordAttribute gender = resolveGender(tag);
        final IWordAttribute number = resolveNumber(tag);
        final IWordAttribute person = resolvePerson(tag);

        if (wordClass != null) {
            attributes.add(wordClass);
        }

        if (seperator != null) {
            attributes.add(seperator);
        }

        if (gender != null) {
            attributes.add(gender);
        }

        if (number != null) {
            attributes.add(number);
        }

        if (person != null) {
            attributes.add(person);
        }

        return attributes;
    }

    private IWordAttribute resolveSeperator(String tag) {
        if (tag.length() == 1 && !Character.isLetterOrDigit(tag.charAt(0))) {
            return PUNCTUATION;
        }
        return null;
    }

    private IWordAttribute resolveGender(String tag) {
        return null;
    }

    private IWordAttribute resolveNumber(String tag) {
        if (isNoun(tag)) {
            return tag.endsWith("S") ? PLURAL : SINGULAR;
        }
        return null;
    }

    private IWordAttribute resolvePerson(String tag) {
        if (isVerb(tag)) {
            return tag.endsWith("Z") ? THIRD : null;
        }
        return null;
    }

    private IWordAttribute resolveWordClass(String tag) {
        if (isNoun(tag)) {
            return WordClassAttribute.NOUN;
        } else if (isAdjective(tag)) {
            return WordClassAttribute.ADJECTIVE;
        } else if (isPronoun(tag)) {
            return WordClassAttribute.PRONOUN;
        } else if (isPersonalPronoun(tag)) {
            return WordClassAttribute.PERSONAL_PRONOUN;
        } else if (isVerb(tag)) {
            return WordClassAttribute.VERB;
        } else if (isAdverb(tag)) {
            return WordClassAttribute.ADVERB;
        } else if (isNumeral(tag)) {
            return WordClassAttribute.NUMERAL;
        } else if (isPreposition(tag)) {
            return WordClassAttribute.PREPOSITION;
        } else if (isDeterminer(tag)) {
            return WordClassAttribute.DETERMINER;
        }
        return null;
    }

    private boolean isAdjective(String tag) {
        return tag.startsWith("JJ");
    }

    private boolean isDeterminer(String tag) {
        return tag.startsWith("DT");
    }

    private boolean isNoun(String tag) {
        return tag.startsWith("NN") || tag.startsWith("NP");
    }

    private boolean isPronoun(String tag) {
        return tag.startsWith("WP");
    }

    private boolean isPersonalPronoun(String tag) {
        return tag.startsWith("PRP");
    }

    private boolean isVerb(String tag) {
        return tag.startsWith("VB");
    }

    private boolean isNumeral(String tag) {
        return tag.startsWith("CD");
    }

    private boolean isAdverb(String tag) {
        return tag.startsWith("RB");
    }

    private boolean isPreposition(String tag) {
        return tag.startsWith("IN");
    }
}

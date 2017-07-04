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

package is.ru.openbeat.icenlp;

import static com.google.common.collect.Sets.newHashSet;
import com.google.inject.Singleton;
import is.ru.openbeat.discourse.ITagResolver;
import static is.ru.openbeat.model.GenderAttribute.*;
import is.ru.openbeat.model.IWordAttribute;
import static is.ru.openbeat.model.NumberAttribute.PLURAL;
import static is.ru.openbeat.model.NumberAttribute.SINGULAR;
import static is.ru.openbeat.model.PersonAttribute.*;
import static is.ru.openbeat.model.WordClassAttribute.*;

import java.util.Set;

@Singleton
class IceNlpTagResolver implements ITagResolver {
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
        int startPos = 0;
        int stopPos = 0;
        if (isNoun(tag) || isAdjective(tag) || isArticle(tag)) {
            startPos = 1;
            stopPos = 2;
        } else if (isPronoun(tag) || isNumeral(tag)) {
            startPos = 2;
            stopPos = 3;
        } else if (isVerb(tag)) {
            startPos = 3;
            stopPos = 4;
        }

        //noinspection ConstantConditions
        if (startPos != 0 && stopPos != 0 && tag.length() > startPos) {
            String g = tag.substring(startPos, stopPos);
            if ("k".equals(g)) {
                return MASCULINE;
            } else if ("v".equals(g)) {
                return FEMININE;
            } else if ("h".equals(g)) {
                return NEUTER;
            }
        }
        return null;
    }

    private IWordAttribute resolveNumber(String tag) {
        int startPos = 0;
        int stopPos = 0;
        if (isNoun(tag) || isAdjective(tag) || isArticle(tag)) {
            startPos = 2;
            stopPos = 3;
        } else if (isPersonalPronoun(tag) || isNumeral(tag)) {
            startPos = 3;
            stopPos = 4;
        } else if (isVerb(tag)) {
            startPos = 4;
            stopPos = 5;
        }

        //noinspection ConstantConditions
        if (startPos != 0 && stopPos != 0 && tag.length() > startPos) {
            String n = tag.substring(startPos, stopPos);
            if ("e".equals(n)) {
                return SINGULAR;
            } else if ("f".equals(n)) {
                return PLURAL;
            }
        }
        return null;
    }

    private IWordAttribute resolvePerson(String tag) {
        int startPos = 0;
        int stopPos = 0;
        if (isPersonalPronoun(tag)) {
            startPos = 2;
            stopPos = 3;
        } else if (isVerb(tag) && !isPastParticleVerb(tag)) {
            startPos = 3;
            stopPos = 4;
        }

        //noinspection ConstantConditions
        if (startPos != 0 && stopPos != 0 && tag.length() > startPos) {
            String p = tag.substring(startPos, stopPos);
            if ("1".equals(p)) {
                return FIRST;
            } else if ("2".equals(p)) {
                return SECOND;
            } else if ("3".equals(p) || "k".equals(p) || "v".equals(p) || "h".equals(p)) {
                return THIRD;
            }
        }
        return null;
    }

    private IWordAttribute resolveWordClass(String tag) {
        if (isNoun(tag)) {
            return NOUN;
        } else if (isAdjective(tag)) {
            return ADJECTIVE;
        } else if (isArticle(tag)) {
            return ARTICLE;
        } else if (isNumeral(tag)) {
            return NUMERAL;
        } else if (isPersonalPronoun(tag)) {
            return PERSONAL_PRONOUN;
        } else if (isPronoun(tag)) {
            return PRONOUN;
        } else if (isVerb(tag)) {
            return VERB;
        } else if (isAdverbOrPreposition(tag)) {
            return ADVERB;
        }
        return null;
    }

    private boolean isAdjective(String tag) {
        return tag.startsWith("l");
    }

    private boolean isArticle(String tag) {
        return tag.startsWith("g");
    }

    private boolean isNoun(String tag) {
        return tag.startsWith("n");
    }

    private boolean isNumeral(String tag) {
        return tag.startsWith("t");
    }

    private boolean isPronoun(String tag) {
        return tag.startsWith("f");
    }

    private boolean isPersonalPronoun(String tag) {
        return tag.startsWith("fp");
    }

    private boolean isVerb(String tag) {
        return tag.startsWith("s");
    }

    private boolean isPastParticleVerb(String tag) {
        return tag.startsWith("sþ");
    }

    private boolean isAdverbOrPreposition(String tag) {
        return tag.startsWith("a");
    }
}


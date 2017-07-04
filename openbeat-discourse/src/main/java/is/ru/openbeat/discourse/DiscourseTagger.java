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

package is.ru.openbeat.discourse;

import static com.google.common.collect.Maps.newHashMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.openbeat.model.*;
import static is.ru.openbeat.model.PersonAttribute.*;
import static is.ru.openbeat.model.WordClassAttribute.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Class <tt>DiscourseTagger</tt> uses the {@link IDiscourseModel} to mark new entities according to the recency
 * method.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
@Singleton
public class DiscourseTagger implements IDiscourseTagger {
    private static final Logger log = LoggerFactory.getLogger(DiscourseTagger.class);

    private final Multimethod mm = new Multimethod(this);

    private final Map<String, Integer> entityMap = newHashMap();

    private final IDiscourseModel discourseModel;

    @Inject
    public DiscourseTagger(IDiscourseModel discourseModel) {
        this.discourseModel = discourseModel;
    }

    public void tag(Iterable<IFeatureStructure> structure) {
        for (IFeatureStructure featureStructure : structure) {
            mm.match(featureStructure);
        }

        log.debug("Tagged discourse model: {}", discourseModel);
    }

    public void clearState() {
        discourseModel.clearState();
        entityMap.clear();
    }

    @Multi
    private void handleAdjective(Constituent constituent, Word word) {
        if (word.is(ADJECTIVE)) {
            log.debug("Hit adjective {}", word);

            boolean matched = false;
            for (IDiscourseEntity entity : discourseModel.getEntities()) {
                if (entity.getWord().equals(word)) {
                    entity.addReferer(constituent);
                    discourseModel.refer(entity);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                final IDiscourseEntity entity = new DiscourseEntity(createIdentifier(word), word);
                entity.addReferer(constituent);
                discourseModel.addEntity(entity);
            }
        } else if (word.is(VERB)) {
            log.debug("Hit verb {}", word);

            boolean matched = false;
            for (IDiscourseEntity entity : discourseModel.getEntities()) {
                if (entity.getWord().equals(word)) {
                    entity.addReferer(constituent);
                    discourseModel.refer(entity);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                final IDiscourseEntity entity = new DiscourseEntity(createIdentifier(word), word);
                entity.addReferer(constituent);
                discourseModel.addEntity(entity);
            }
        }
    }

    /**
     * Checks if the word has a certain grammatical tag and adds it to the discourse structure as referer.
     *
     * @param nounPhrase the subject belonging to each word
     * @param word       the word which is being checked
     */
    @Multi
    private void handleNounPhraseWord(NounPhrase nounPhrase, Word word) {
        if (word.is(NOUN) || (word.is(PRONOUN) && !word.is(PERSONAL_PRONOUN)) || (word.is(PERSONAL_PRONOUN) && (word.is(
            FIRST) || word.is(SECOND)))) {

            // if word is new, add refering expression, else create entity
            boolean matched = false;
            log.debug("[Word] Hit noun/pronoun, creating refering expression: {}", word);
            for (IDiscourseEntity entity : discourseModel.getEntities()) {
                if (entity.matches(word)) {
                    entity.addReferer(nounPhrase);
                    discourseModel.refer(entity);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                log.trace("[Word] Hit noun/pronoun, creating entity: {}", word);
                final IDiscourseEntity entity = new DiscourseEntity(createIdentifier(word), word);
                entity.addReferer(nounPhrase);
                discourseModel.addEntity(entity);
            }
        } else if (word.is(PERSONAL_PRONOUN) && word.is(THIRD)) {
            for (IDiscourseEntity entity : discourseModel.getEntities()) {
                if (entity.matches(word)) {
                    log.debug("[Word] Hit personal pronoun, matches {}, adding referer: {}", entity, word);
                    entity.addReferer(nounPhrase);
                    discourseModel.refer(entity);
                    break;
                }
            }
        } else {
            log.trace("[Word] Word doesn't match any criteria: {}", word);
        }
    }

    @Multi
    private void handleNounPhraseConstituent(NounPhrase nounPhrase, Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(nounPhrase, featureStructure);
        }
    }

    @Multi
    private void handleNounPhrase(NounPhrase nounPhrase) {
        log.trace("Hit nounPhrase: {}", nounPhrase);

        for (IFeatureStructure featureStructure : nounPhrase.getFeatures()) {
            mm.match(nounPhrase, featureStructure);
        }
    }

    @Multi
    private void handleConstituent(Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(constituent, featureStructure);
        }
    }

    private String createIdentifier(Word word) {
        String ref = word.is(PERSONAL_PRONOUN) ? "producer" : word.getToken();
        ref = ref.toUpperCase();
        if (entityMap.containsKey(ref)) {
            Integer number = entityMap.get(ref);
            number += 1;
            entityMap.put(ref, number);
            return ref + Integer.toString(number);
        } else {
            entityMap.put(ref, 1);
            return ref + Integer.toString(1);
        }
    }
}

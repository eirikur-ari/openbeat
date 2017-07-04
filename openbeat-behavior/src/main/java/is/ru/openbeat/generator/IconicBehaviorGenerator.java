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

package is.ru.openbeat.generator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.openbeat.discourse.IDiscourseModel;
import is.ru.openbeat.knowledge.IGesture;
import is.ru.openbeat.knowledge.IKnowledgeBase;
import is.ru.openbeat.knowledge.IKnowledgeInstance;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Collect;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.multimethod.Produce;
import is.ru.openbeat.pipeline.IBehaviorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <tt>IconicBehaviorGenerator</tt> Generates iconic gestures for the appropriate {@link NounPhrase} and {@link
 * VerbPhrase}. The general rule is that iconic gestures are generated for {@link NounPhrase} and {@link VerbPhrase}
 * that are contained within a {@link Rheme} and also contains a word which is marked as <tt>new</tt> in the {@link
 * IDiscourseModel discourse model}.
 * <p/>
 * The class collects all rhemes and constituents that contain new word, it then produces iconic gestures for valid
 * subjects and actions.
 *
 * @author eirikurp06@ru.is (Eiríkur A. Pétursson)
 */
@Singleton
public class IconicBehaviorGenerator implements IBehaviorGenerator {
    private static final Logger log = LoggerFactory.getLogger(IconicBehaviorGenerator.class);

    private final Multimethod c = new Multimethod(this, Collect.class);
    private final Multimethod p = new Multimethod(this, Produce.class);

    private final IDiscourseModel discourseModel;
    private final IKnowledgeBase knowledgeBase;

    @Inject
    public IconicBehaviorGenerator(IDiscourseModel discourseModel, IKnowledgeBase knowledgeBase) {
        this.discourseModel = discourseModel;
        this.knowledgeBase = knowledgeBase;
    }

    public Utterance process(Utterance utterance) {
        log.debug("Running {}", getClass().getSimpleName());

        // see if there's a new entity in the rheme
        for (Clause clause : utterance.getClauses()) {
            Boolean n1 = c.match(clause.getArticulations().getFirst());
            Boolean n2 = c.match(clause.getArticulations().getSecond());

            // produce iconic behavior if the rheme contained a new entity
            if (n1 != null && n1) {
                log.trace("Articulation: {}", clause.getArticulations().getFirst());
                p.match(clause.getArticulations().getFirst());
            }
            if (n2 != null && n2) {
                log.trace("Articulation: {}", clause.getArticulations().getSecond());
                p.match(clause.getArticulations().getSecond());
            }
        }

        return utterance;
    }

    @Collect
    private boolean collectRheme(Rheme rheme) {
        for (IFeatureStructure featureStructure : rheme.getPhrases()) {
            Boolean match = c.match(featureStructure);
            if (match != null && match) {
                log.trace("Collecting Rheme: {}", rheme);
                return true;
            }
        }
        return false;
    }

    @Collect
    private boolean collectConstituent(Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            Boolean match = c.match(featureStructure);
            if (match != null && match) {
                log.trace("Collection constituent: {}", constituent);
                return true;
            }
        }
        return false;
    }

    @Collect
    private boolean collectWord(Word word) {
        final boolean isNew = discourseModel.isNew(word);
        if (isNew) {
            log.debug("Collecting words: {}", word);
        }
        return isNew;
    }

    @Produce
    private void produceRheme(Rheme rheme) {
        log.trace("Producing rheme {}", rheme);
        for (IFeatureStructure featureStructure : rheme.getPhrases()) {
            p.match(featureStructure);
        }
    }

    @Produce
    private void produceConstituent(Constituent constituent) {
        log.trace("Producing constituent: {}", constituent);
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            p.match(featureStructure);
        }
    }

    /**
     * Assigns iconic gesture on subject for {@link NounPhrase} within {@link Rheme} that contains a <tt>new</tt> word
     * if the <tt>id</tt> returns a suggestion from the knowledgebase.
     */
    @Produce
    private void produceNounPhrase(NounPhrase nounPhrase) {
        final String id = nounPhrase.getId();
        if (id != null) {
            log.debug("Producing for nounPhrase: {}", id);

            final IKnowledgeInstance instance = knowledgeBase.getInstance(id);
            log.trace("Got the instance: {}", instance);
            final String value = instance.getSurprisingValue(knowledgeBase);
            log.trace("Got the value {}", value);

            if (value != null) {
                final IGesture gesture = knowledgeBase.getCompactGesture(value);

                if (gesture != null) {
                    log.debug("Got the gesture {}", gesture);
                    gesture.setPriority(20);
                    nounPhrase.addBehavior(gesture);
                }
            }
        }
    }

    /**
     * Assigns iconic gesture on action for {@link VerbPhrase} within {@link Rheme} that contains a <tt>new</tt> word if
     * the <tt>id</tt> returns a suggestion from the knowledgebase.
     */
    @Produce
    private void produceVerbPhrase(VerbPhrase verbPhrase) {
        if (verbPhrase.getId() != null) {
            log.debug("Producing for verbPhrase: {}", verbPhrase.getId());
            final IGesture gesture = knowledgeBase.getCompactGesture(verbPhrase.getId());

            if (gesture != null) {
                log.debug("Got the gesture {}", gesture);
                gesture.setPriority(20);
                verbPhrase.addBehavior(gesture);
            }
        }
    }
}

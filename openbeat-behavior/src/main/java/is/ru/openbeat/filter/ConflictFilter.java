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

package is.ru.openbeat.filter;

import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Singleton;
import is.ru.openbeat.annotation.ConflictsWith;
import is.ru.openbeat.behavior.IBehavior;
import is.ru.openbeat.behavior.IBehaviorContainer;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.pipeline.IBehaviorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * <tt>ConflictFilter</tt> Removes conflicting behaviors.
 * If a behavior has been annotated with a conflict to another behavior,
 * the one with the higher priority wins.
 *
 * Note: Implement the co-articulation check.
 *
 * @author eirikurp06@ru.is (Eiríkur A. Pétursson)
 */
@Singleton
class ConflictFilter implements IBehaviorFilter {
    private static final Logger log = LoggerFactory.getLogger(ConflictFilter.class);

    private static final int DEFAULT_PRIORITY = 0;

    private final Multimethod mm = new Multimethod(this);

    public Utterance process(Utterance utterance) {
        log.debug("Running {}", getClass().getSimpleName());

        final List<IBehaviorContainer> behaviorContainers = newArrayList();

        for (Clause clause : utterance.getClauses()) {
            log.debug("Adding clauses {}", clause);
            behaviorContainers.add(clause);
            mm.match(behaviorContainers, clause.getArticulations().getFirst());
            mm.match(behaviorContainers, clause.getArticulations().getSecond());
        }

        return utterance;
    }

    @Multi
    private void handleTheme(List<IBehaviorContainer> behaviorContainers, Theme theme) {
        log.debug("Adding theme {}", theme);
        behaviorContainers.add(theme);
        for (IFeatureStructure featureStructure : theme.getPhrases()) {
            mm.match(behaviorContainers, featureStructure);
        }
        handleConflict(behaviorContainers);
    }

    @Multi
    private void handleRheme(List<IBehaviorContainer> behaviorContainers, Rheme rheme) {
        log.debug("Adding rheme {}", rheme);
        behaviorContainers.add(rheme);
        for (IFeatureStructure featureStructure : rheme.getPhrases()) {
            mm.match(behaviorContainers, featureStructure);
        }
        handleConflict(behaviorContainers);
    }

    @Multi
    private void handleConstituent(List<IBehaviorContainer> behaviorContainers, Constituent constituent) {
        log.debug("Adding constituent {}", constituent);
        behaviorContainers.add(constituent);
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(behaviorContainers, featureStructure);
        }
        handleConflict(behaviorContainers);
    }

    @Multi
    private void handleWord(List<IBehaviorContainer> behaviorContainers, Word word) {
        log.debug("Adding word {}", word);
        behaviorContainers.add(word);
        handleConflict(behaviorContainers);
    }

    /**
     * Gather all assigned behaviors and matches them for a conflic.
     * If there is a conflict then remove the one with the lower priority.
     * @param behaviorContainers
     */
    private void handleConflict(List<IBehaviorContainer> behaviorContainers) {
        for (int i = 0; i < behaviorContainers.size(); i++) {
            IBehaviorContainer bc1 = behaviorContainers.get(i);

            for (int j = 1; j < behaviorContainers.size(); j++) {
                IBehaviorContainer bc2 = behaviorContainers.get(j);

                final List<Pair<IBehaviorContainer,IBehavior>> removableBehaviors = newArrayList();
                for (IBehavior b1 : bc1.getBehaviors()) {
                    for (IBehavior b2 : bc2.getBehaviors()) {
                        if (matchConflict(b1, b2)) {
                            // TODO: Check for CoArticulation
                            if (getPriority(b1) >= getPriority(b2)) {
                                log.debug("Conflicts between {} and {}", b1, b2);
                                log.debug("Priority of {} equals to {}", b1.getClass().getSimpleName(),
                                    getPriority(b1));
                                log.debug("Items {}", behaviorContainers);
                                log.debug("{} removed", b2.getClass().getSimpleName());
                                log.debug("ItemContents {}", bc2.getBehaviors());
                                removableBehaviors.add(Pair.of(bc2, b2));
                                log.debug("Items {}", behaviorContainers);
                            }
                        }
                    }
                }
                for (Pair<IBehaviorContainer,IBehavior> pair : removableBehaviors) {
                    pair.getFirst().removeBehavior(pair.getSecond());
                }
            }
        }
        //CleanUp
        log.debug("Removing {}", behaviorContainers.size() - 1);
        behaviorContainers.remove(behaviorContainers.size() - 1);
    }

    private boolean matchConflict(IBehavior b1, IBehavior b2) {
        return doesConflict(b1, b2) || doesConflict(b2, b1);
    }

    /**
     * Checks if a given behaviors have been annotated with the ConflictsWith
     * annotation that defines if those two behaviors conflicts.
     * @param b1
     * @param b2
     * @return true/false
     */
    private boolean doesConflict(IBehavior b1, IBehavior b2) {
        for (Annotation annotation : b1.getClass().getAnnotations()) {
            if (ConflictsWith.class.isAssignableFrom(annotation.getClass())) {
                ConflictsWith conflictsWith = (ConflictsWith) annotation;
                for (Class<? extends IBehavior> conflict : conflictsWith.value()) {
                    if (b2.getClass().equals(conflict)) {
                        return true;
                    }
                }
            }
        }
        log.trace("{} does not conflict with {}", b1, b2);
        return false;
    }

    /**
     * Gets the priority of the current behavior
     * @param b is the current behavior
     * @return the priority of behavior or a default priority.
     */
    private int getPriority(IBehavior b) {
        if (b.getPriority() != null) {
            return b.getPriority();
        }
        return DEFAULT_PRIORITY;
    }
}

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

package is.ru.openbeat.mcneill;

import static com.google.common.collect.Lists.immutableList;
import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Singleton;
import is.ru.openbeat.behavior.IBehavior;
import is.ru.openbeat.behavior.IBehaviorContainer;
import is.ru.openbeat.behavior.IMcNeillProducer;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.pipeline.ICompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Class <tt>McNeillCompiler</tt> creates a text representation of the speech and nonverbal behaviors following McNeill
 * 1992 and returns string representation of it.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
@Singleton
class McNeillCompiler implements ICompiler {
    private static final Logger log = LoggerFactory.getLogger(McNeillCompiler.class);

    private final Multimethod mm = new Multimethod(this);

    public String process(Utterance utterance) {
        final StringBuilder sb = new StringBuilder();
        for (Clause clause : utterance.getClauses()) {
            final List<IMcNeillProducer> producers = findProducers(clause);
            executePrefix(sb, producers);
            mm.match(sb, clause.getArticulations().getFirst());
            mm.match(sb, clause.getArticulations().getSecond());
            executePostfix(sb, producers);
        }

        return sb.toString();
    }

    @Multi
    private void handleRheme(StringBuilder sb, Rheme rheme) {
        final List<IMcNeillProducer> producers = findProducers(rheme);
        executePrefix(sb, producers);
        for (IFeatureStructure featureStructure : rheme.getPhrases()) {
            mm.match(sb, featureStructure);
        }
        executePostfix(sb, producers);
    }

    @Multi
    private void handleTheme(StringBuilder sb, Theme theme) {
        final List<IMcNeillProducer> producers = findProducers(theme);
        executePrefix(sb, producers);
        for (IFeatureStructure featureStructure : theme.getPhrases()) {
            mm.match(sb, featureStructure);
        }
        executePostfix(sb, producers);
    }

    @Multi
    private void handleConstituent(StringBuilder sb, Constituent constituent) {
        final List<IMcNeillProducer> producers = findProducers(constituent);
        executePrefix(sb, producers);
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(sb, featureStructure);
        }
        executePostfix(sb, producers);
    }

    @Multi
    private void handleWord(StringBuilder sb, Word word) {
        final List<IMcNeillProducer> producers = findProducers(word);
        executePrefix(sb, producers);
        sb.append(word.getToken());
        executePostfix(sb, producers);
        if (!word.is(WordClassAttribute.PUNCTUATION)) {
            sb.append(" ");
        }
    }

    private List<IMcNeillProducer> findProducers(IBehaviorContainer behaviorContainer) {
        final List<IMcNeillProducer> producers = newArrayList();
        for (IBehavior behavior : behaviorContainer.getBehaviors()) {
            IMcNeillProducer producer = mm.match(behavior);
            if (producer != null) {
                producers.add(producer);
            } else {
                log.warn("Cannot produce McNeill for {}", behavior);
            }
        }
        return immutableList(producers);
    }

    @Multi
    private IMcNeillProducer matchProducer(IMcNeillProducer mcNeillProducer) {
        log.debug("Received McNeill producer {}", mcNeillProducer);
        return mcNeillProducer;
    }

    private void executePrefix(StringBuilder sb, List<IMcNeillProducer> producers) {
        for (IMcNeillProducer producer : producers) {
            String s = producer.createPrefix();
            if (s != null) {
                sb.append(s);
            }
        }
    }

    private void executePostfix(StringBuilder sb, List<IMcNeillProducer> producers) {
        for (IMcNeillProducer producer : producers) {
            String s = producer.createPostfix();
            if (s != null) {
                sb.append(s);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

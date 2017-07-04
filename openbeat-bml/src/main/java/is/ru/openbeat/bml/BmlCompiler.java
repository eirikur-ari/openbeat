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

package is.ru.openbeat.bml;

import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Singleton;
import is.ru.openbeat.behavior.IBehavior;
import is.ru.openbeat.behavior.IBehaviorContainer;
import is.ru.openbeat.behavior.IBmlProducer;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.pipeline.ICompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class <tt>BmlCompiler</tt> compiles the utterance to BML blocks and returns string representation of it.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
@Singleton
class BmlCompiler implements ICompiler {
    private static final Logger log = LoggerFactory.getLogger(BmlCompiler.class);
    private static final String NEWLINE = "\n";

    public String process(Utterance utterance) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<bml>");

        // produce speech
        new SpeechProducer(sb, utterance);

        // produce behaviors
        new BehaviorProducer(sb, utterance);

        sb.append("</bml>");

        return sb.toString();
    }


    private class SpeechProducer {
        private final Multimethod mm = new Multimethod(this);

        private SpeechProducer(StringBuilder sb, Utterance utterance) {
            sb.append("<speech>");
            for (Clause clause : utterance.getClauses()) {
                handleArticulation(sb, clause.getArticulations().getFirst());
                handleArticulation(sb, clause.getArticulations().getSecond());
            }
            sb.append("</speech>").append(NEWLINE);
        }

        private void handleArticulation(StringBuilder sb, IArticulation articulation) {
            for (IFeatureStructure featureStructure : articulation.getPhrases()) {
                mm.match(sb, featureStructure);
            }
        }

        @Multi
        private void handleConstituent(StringBuilder sb, Constituent constituent) {
            for (IFeatureStructure featureStructure : constituent.getFeatures()) {
                mm.match(sb, featureStructure);
            }
        }

        @Multi
        private void handleWord(StringBuilder sb, Word word) {
            if (word.getBeginTime() != null) {
                sb.append("<mark time=\"").append(String.format("%.1f", word.getBeginTime())).append("\"/>");
            }
            if (!word.is(WordClassAttribute.PUNCTUATION)) {
                sb.append(" ");
            }
            sb.append(word.getToken());
            sb.append(" ");
        }
    }

    private class BehaviorProducer {
        private final Multimethod mm = new Multimethod(this);

        private BehaviorProducer(StringBuilder sb, Utterance utterance) {
            for (Clause clause : utterance.getClauses()) {
                final List<Word> words = newArrayList();

                mm.match(words, sb, clause.getArticulations().getFirst());
                mm.match(words, sb, clause.getArticulations().getSecond());

                matchBehaviors(sb, clause, beginTime(words), endTime(words));
            }
        }

        @Multi
        private void handleRheme(List<Word> words, StringBuilder sb, Rheme rheme) {
            final List<Word> rhemeWords = newArrayList();
            for (IFeatureStructure featureStructure : rheme.getPhrases()) {
                mm.match(rhemeWords, sb, featureStructure);
            }
            words.addAll(rhemeWords);

            matchBehaviors(sb, rheme, beginTime(rhemeWords), endTime(rhemeWords));
        }

        @Multi
        private void handleTheme(List<Word> words, StringBuilder sb, Theme theme) {
            final List<Word> themeWords = newArrayList();
            for (IFeatureStructure featureStructure : theme.getPhrases()) {
                mm.match(themeWords, sb, featureStructure);
            }
            words.addAll(themeWords);

            matchBehaviors(sb, theme, beginTime(themeWords), endTime(themeWords));
        }

        @Multi
        private void handleConstituent(List<Word> words, StringBuilder sb, Constituent constituent) {
            final List<Word> constituentWords = newArrayList();
            for (IFeatureStructure featureStructure : constituent.getFeatures()) {
                mm.match(constituentWords, sb, featureStructure);
            }
            words.addAll(constituentWords);

            matchBehaviors(sb, constituent, beginTime(constituentWords), endTime(constituentWords));
        }

        @Multi
        private void handleWord(List<Word> words, StringBuilder sb, Word word) {
            words.add(word);

            matchBehaviors(sb, word, boxTime(word.getBeginTime()), boxTime(word.getEndTime()));
        }

        @Multi
        private Boolean handleBmlProducer(StringBuilder sb, IBehaviorContainer container, IBmlProducer bmlProducer,
                                          Float beginTime, Float endTime) {
            Float begin = unboxTime(beginTime);
            Float end = unboxTime(endTime);
            if (begin == null) {
                log.warn("No begin time assigned to {} through {}", bmlProducer, container);
            } else {
                if (begin.equals(end)) {
                    log.debug("{} on {} has the same begin and end time", bmlProducer, container);
                    end = null;
                }
                sb.append(bmlProducer.createBml(begin, end)).append(NEWLINE);
            }

            return true;
        }

        private void matchBehaviors(StringBuilder sb, IBehaviorContainer behaviorContainer, Float beginTime,
                                    Float endTime) {
            for (IBehavior behavior : behaviorContainer.getBehaviors()) {
                log.debug("Matching behaviors on {}", behaviorContainer);
                Boolean matched = mm.match(sb, behaviorContainer, behavior, beginTime, endTime);
                if (matched == null || !matched) {
                    log.warn("Cannot produce BML for {} at {}", behavior, beginTime);
                }
            }
        }

        private Float beginTime(List<Word> words) {
            if (!words.isEmpty()) {
                return boxTime(words.get(0).getBeginTime());
            }
            return -1F;
        }

        private Float endTime(List<Word> words) {
            if (!words.isEmpty()) {
                return boxTime(words.get(words.size() - 1).getBeginTime());
            }
            return -1F;
        }

        private Float boxTime(Float time) {
            return time != null ? time : -1F;
        }

        private Float unboxTime(Float time) {
            return time != -1F ? time : null;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

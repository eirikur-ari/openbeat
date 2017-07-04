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

package is.ru.openbeat.freetts;

import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.speech.freetts.Voice;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Collect;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.multimethod.Produce;
import is.ru.openbeat.pipeline.ITimingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class <tt>FreeTtsTimingSource</tt> reads timing information from FreeTTS based on the text in this utterance. Timings
 * are then associated back onto the words in the utterance.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
@Singleton
class FreeTtsTimingSource implements ITimingSource {
    private static final Logger log = LoggerFactory.getLogger(FreeTtsTimingSource.class);

    private final Multimethod c = new Multimethod(this, Collect.class);
    private final Multimethod p = new Multimethod(this, Produce.class);

    private final Voice timingVoice;
    private final Voice plabackVoice;
    private final TimingProcessor timingProcessor;

    private String text;

    @Inject
    FreeTtsTimingSource(@Named("freetts.timingvoice") Voice timingVoice,
                        @Named("freetts.playbackvoice") Voice plabackVoice, TimingProcessor timingProcessor) {
        this.timingVoice = timingVoice;
        this.plabackVoice = plabackVoice;
        this.timingProcessor = timingProcessor;
    }

    public void play() {
        try {
            plabackVoice.speak(text);
        } catch (RuntimeException e) {
            log.warn(
                "Note: Only operating system with verification of FreeTTS synthesized speak working is Microsoft Windows");
            log.warn("Exception occured trying to let FreeTTS speak '{}'", text);
        }
    }

    public Utterance process(Utterance utterance) {
        text = null; // reset the text

        final StringBuilder sb = new StringBuilder();
        for (Clause clause : utterance.getClauses()) {
            handleArticulation(sb, clause.getArticulations().getFirst());
            handleArticulation(sb, clause.getArticulations().getSecond());
        }

        text = sb.toString().trim();
        log.debug("Sending '{}' to timingVoice", text);
        timingVoice.speak(text);

        // collect the results (listOf Pair<token, endTime>) 
        final List<Pair<String, Float>> timings = newArrayList(timingProcessor.getTimings());
        log.debug("Received {} from timings operation", timings);
        for (Clause clause : utterance.getClauses()) {
            handleArticulation(timings, clause.getArticulations().getFirst());
            handleArticulation(timings, clause.getArticulations().getSecond());
        }

        return utterance;
    }

    private void handleArticulation(StringBuilder sb, IArticulation articulation) {
        for (IFeatureStructure featureStructure : articulation.getPhrases()) {
            c.match(sb, featureStructure);
        }
    }

    @Collect
    private void handleConstituent(StringBuilder sb, Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            c.match(sb, featureStructure);
        }
    }

    @Collect
    private void handleWord(StringBuilder sb, Word word) {
        sb.append(word.getToken());
        if (!word.is(WordClassAttribute.PUNCTUATION)) {
            sb.append(" ");
        }
    }

    private void handleArticulation(List<Pair<String, Float>> timings, IArticulation articulation) {
        for (IFeatureStructure featureStructure : articulation.getPhrases()) {
            p.match(timings, featureStructure);
        }
    }

    @Produce
    private void handleConstituent(List<Pair<String, Float>> timings, Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            p.match(timings, featureStructure);
        }
    }

    @Produce
    private void handleWord(List<Pair<String, Float>> timings, Word word) {
        Pair<String, Float> remove = null;
        for (Pair<String, Float> pair : timings) {
            if (word.getToken().equalsIgnoreCase(pair.getFirst())) {
                log.debug("{} has start time {}", pair.getFirst(), pair.getSecond());
                word.setBeginTime(pair.getSecond());
                remove = pair;
                break;
            }
        }

        if (remove != null) {
            log.debug("Removing {} from timings", remove);
            timings.remove(remove);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

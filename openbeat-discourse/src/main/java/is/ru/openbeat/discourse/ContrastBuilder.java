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

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Collect;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.multimethod.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Class <tt>ContrastBuilder</tt> builds all contrasts in an utterance and adds them to defined objects in the dictionarysource
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
public class ContrastBuilder {
    private static final Logger log = LoggerFactory.getLogger(ContrastBuilder.class);

    private final Multimethod c = new Multimethod(this, Collect.class);
    private final Multimethod p = new Multimethod(this, Produce.class);

    private final IDictionarySource dictionarySource;

    @Inject
    public ContrastBuilder(IDictionarySource dictionarySource) {
        this.dictionarySource = dictionarySource;
    }

    public void buildContrast(Utterance utterance) {
        final Map<Word, List<String>> contrastMap = Maps.newHashMap();

        for (Clause clause : utterance.getClauses()) {
            collectArticulation(contrastMap, clause.getArticulations().getFirst());
            collectArticulation(contrastMap, clause.getArticulations().getSecond());
        }

        for (Clause clause : utterance.getClauses()) {
            produceArticulation(contrastMap, clause.getArticulations().getFirst());
            produceArticulation(contrastMap, clause.getArticulations().getSecond());
        }
    }

    private void collectArticulation(Map<Word, List<String>> contrastMap, IArticulation articulation) {
        for (IFeatureStructure featureStructure : articulation.getPhrases()) {
            c.match(contrastMap, featureStructure);
        }
    }

    private void produceArticulation(Map<Word, List<String>> contrastMap, IArticulation articulation) {
        for (IFeatureStructure featureStructure : articulation.getPhrases()) {
            p.match(contrastMap, featureStructure);
        }
    }

    @Collect
    private void collectWord(Map<Word, List<String>> contrastMap, Word word) {
        final List<String> contrasts = dictionarySource.findContrasts(word);
        if (!contrasts.isEmpty()) {
            log.debug("Collecting word: " + word);
            contrastMap.put(word, contrasts);
        }
    }

    @Collect
    private void collectConstituent(Map<Word, List<String>> contrastMap, Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            c.match(contrastMap, featureStructure);
        }
    }

    @Produce
    private void produceWord(Map<Word, List<String>> contrastMap, Word word) {
        // TODO: athuga hvort lemma á word, sé einhver staðar inn í contrastMap, ef svo, tengja orð
        for (Map.Entry<Word, List<String>> entry : contrastMap.entrySet()) {
            for (String constrastString : entry.getValue()) {
                if (constrastString.equals(word.getLemma())) {
                    log.debug("Processing word: " + word);
                    word.addContrast(entry.getKey());
                }
            }
        }
    }

    @Produce
    private void procudeConstituent(Map<Word, List<String>> contrastMap, Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            p.match(contrastMap, featureStructure);
        }
    }
}

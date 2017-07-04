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

import static com.google.common.base.Join.join;
import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.openbeat.knowledge.IKnowledgeBase;
import is.ru.openbeat.knowledge.IKnowledgeInstance;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class <tt>NounPhraseIdentifier</tt> identifies all noun phrases in an utterance and links it to defined objects in the knowledge base.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
@Singleton
public class NounPhraseIdentifier {
    private static final Logger log = LoggerFactory.getLogger(NounPhraseIdentifier.class);

    private final Multimethod mm = new Multimethod(this);

    private final IKnowledgeBase knowledgeBase;

    @Inject
    public NounPhraseIdentifier(IKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public void identify(Utterance utterance) {
        for (Clause clause : utterance.getClauses()) {
            handleArticulation(clause.getArticulations().getFirst());
            handleArticulation(clause.getArticulations().getSecond());
        }
    }

    private void handleArticulation(IArticulation articulation) {
        for (IFeatureStructure featureStructure : articulation.getPhrases()) {
            mm.match(featureStructure);
        }
    }

    @Multi
    private void handleConstituent(Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(featureStructure);
        }
    }

    @Multi
    private void handleNounPhrase(NounPhrase nounPhrase) {
        final List<String> words = newArrayList();
        for (IFeatureStructure featureStructure : nounPhrase.getFeatures()) {
            mm.match(words, featureStructure);
        }

        final String identifier = join(" ", words);
        log.debug("Checking if '{}' is an nounPhrase identifier in knowledge base", identifier);
        final IKnowledgeInstance instance = knowledgeBase.getBestInstanceMatch(identifier);
        if (instance != null) {
            log.debug("Setting nounPhrase identifier '{}' for '{}'", instance.getId(), identifier);
            nounPhrase.setId(instance.getId());
            log.debug("Updated noun phrase: {}", nounPhrase);
        }
    }

    @Multi
    private void handleWord(List<String> words, Word word) {
        log.trace("Adding {} to words", word);
        words.add(word.getLemma() != null ? word.getLemma() : word.getToken());
    }
}

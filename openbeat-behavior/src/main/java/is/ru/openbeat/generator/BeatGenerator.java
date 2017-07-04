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

import is.ru.openbeat.pipeline.IBehaviorGenerator;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.discourse.IDiscourseModel;
import is.ru.openbeat.behavior.GestureBehavior;
import is.ru.openbeat.knowledge.IGesture;
import static is.ru.openbeat.knowledge.IGesture.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BeatGenerator implements IBehaviorGenerator {
    private static final Logger log = LoggerFactory.getLogger(BeatGenerator.class);

    private final Multimethod mm = new Multimethod(this);

    private final IDiscourseModel discourseModel;

    @Inject
    public BeatGenerator(IDiscourseModel discourseModel) {
        this.discourseModel = discourseModel;
    }

    public Utterance process(Utterance utterance) {
        log.debug("Running {}", getClass().getSimpleName());

        for (Clause clause : utterance.getClauses()) {
            mm.match(clause.getArticulations().getFirst());
            mm.match(clause.getArticulations().getSecond());
        }
        return utterance;
    }

    @Multi
    private void handleRheme(Rheme rheme) {
        log.debug("Rheme: {}", rheme);
        for (IFeatureStructure featureStructure : rheme.getPhrases()) {
            mm.match(rheme, featureStructure);
        }
    }

    @Multi
    private void handleConstituent(Rheme rheme, Constituent constituent) {
        log.debug("Constituent: {}", constituent);
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(rheme, featureStructure);
        }
    }
    @Multi
    private void handleWord(Rheme rheme, Word word) {
        log.debug("Check word: {}",word);
        if (discourseModel.isNew(word)) {
            log.debug("Adding BEAT to {}", rheme);
            word.addBehavior(new GestureBehavior("beat", "offer", GestureType.RIGHT));
        }
    }
}
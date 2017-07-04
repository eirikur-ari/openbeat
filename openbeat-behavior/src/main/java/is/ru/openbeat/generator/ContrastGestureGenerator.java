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

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import is.ru.openbeat.behavior.Arm;
import is.ru.openbeat.behavior.GestureBehavior;
import is.ru.openbeat.knowledge.IArm;
import is.ru.openbeat.knowledge.IGesture;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.pipeline.IBehaviorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * <tt>ContrastGestureGenerator </tt> generates gestures for items which are contrasted in an utterance.
 * If there are exactly two items being contrasted, then a right-hand and left-hand contrast is generated.
 * Otherwise, beats are generated for all contrasted items.
 * <p/>
 * Note: Uncomplete version.
 *
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
@Singleton
public class ContrastGestureGenerator implements IBehaviorGenerator {
    private static final Logger log = LoggerFactory.getLogger(ContrastGestureGenerator.class);

    private final Multimethod mm = new Multimethod(this);

    public Utterance process(Utterance utterance) {
        log.debug("Running {}", getClass().getSimpleName());

        for (Clause clause : utterance.getClauses()) {
            log.debug("Clause: {}", clause);
            handleArticulation(clause.getArticulations().getFirst());
            handleArticulation(clause.getArticulations().getSecond());
        }

        return utterance;
    }

    private void handleArticulation(IArticulation articulation) {
        for (IFeatureStructure featureStructure : articulation.getPhrases()) {
            mm.match(featureStructure);
        }
        log.debug("Articulation: {}", articulation);
    }

    @Multi
    private void handleConstituent(Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(featureStructure);
        }
        log.debug("Constituent: {}", constituent);
    }

    @Multi
    private void handleWord(Word word) {
        log.debug("Word: {}", word);
        Set<Word> contrasts = word.getContrasts();
        log.debug("Contrast: {}", contrasts);

        if (contrasts.size() == 1) {
            log.debug("Contrast equals to 1");
            for (Word contrast : contrasts) {
                makeRightGesture(contrast.getLemma());
                makeLeftGesture(contrast.getLemma());
            }
        } else if (contrasts.size() > 1) {
            log.debug("Contrast larger than 1");
            for (Word contrast : contrasts) {
                makeBeatGesture(contrast.getLemma());
            }
        }
    }

    private GestureBehavior makeRightGesture(String value) {
        log.debug("return makeRightGesture {}", value);
        return new GestureBehavior("contrast_1",
                value,
                Lists.<IArm>newArrayList(new Arm("contrast", "contrast_trajectory", IArm.ArmType.RIGHT)),
                IGesture.GestureType.RIGHT);
    }

    private GestureBehavior makeLeftGesture(String value) {
        log.debug("return makeLeftGesture {}", value);
        return new GestureBehavior("contrast_2",
                value,
                Lists.<IArm>newArrayList(new Arm("contrast", "contrast_trajectory", IArm.ArmType.LEFT)),
                IGesture.GestureType.LEFT);
    }

    private GestureBehavior makeBeatGesture(String value) {
        log.debug("makeBeatGesture {}", value);
        return new GestureBehavior("beat", value, IGesture.GestureType.RIGHT);
    }
}
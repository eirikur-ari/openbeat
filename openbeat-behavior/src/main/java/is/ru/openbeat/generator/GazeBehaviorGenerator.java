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

import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.openbeat.behavior.GazeBehavior;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.participation.Participant;
import is.ru.openbeat.participation.ParticipationFramework;
import is.ru.openbeat.participation.ParticipationFrameworkBase;
import is.ru.openbeat.pipeline.IBehaviorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

@Singleton
public class GazeBehaviorGenerator implements IBehaviorGenerator {
    private static final Logger log = LoggerFactory.getLogger(GazeBehaviorGenerator.class);

    private final Multimethod mm = new Multimethod(this);

    private final Random random = new Random();

    private final ParticipationFrameworkBase participationFrameworkBase;

    @Inject
    public GazeBehaviorGenerator(ParticipationFrameworkBase participationFrameworkBase) {
        this.participationFrameworkBase = participationFrameworkBase;
    }

    public Utterance process(Utterance utterance) {
        log.debug("Running {}", getClass().getSimpleName());

        final List<IArticulation> articulations = newArrayList();
        for (Clause clause : utterance.getClauses()) {
            articulations.add(clause.getArticulations().getFirst());
            articulations.add(clause.getArticulations().getSecond());
        }

        for (int i = 0; i < articulations.size(); i++) {
            IArticulation articulation = articulations.get(i);
            mm.match(articulation, i == 0, i == (articulations.size() - 1));
        }

        return utterance;
    }

    @Multi
    private void generateGaze(Theme theme, Boolean isBeginningOfTurn, Boolean isEndOfTurn) {
        if (isBeginningOfTurn || randBool(0.7)) {
            log.debug("Adding gaze to {}", theme);
            theme.addBehavior(new GazeBehavior(GazeBehavior.Direction.AWAY_FROM_HEARER, 1));
        }
    }

    @Multi
    private void generateGaze(Rheme rheme, Boolean isBeginningOfTurn, Boolean isEndOfTurn) {
        if (isEndOfTurn || randBool(0.73)) {
            //final Participant target = getParticipationFramework().getAddressee() != null ?
            //    getParticipationFramework().getAddressee() :
            //    randomHearer();

            log.debug("Adding gaze to {}", rheme);
            rheme.addBehavior(new GazeBehavior(GazeBehavior.Direction.TOWARDS_HEARER, 5, randomHearer()));
        }
    }

    /**
     * Looks up the current participation framework in the framework base.
     *
     * @return the current participation framework
     */
    private ParticipationFramework getParticipationFramework() {
        return participationFrameworkBase.getCurrentParticipationFramework();
    }

    /**
     * Selects a random participant who is hearing the speaker.
     *
     * @return a random hearer if available, else {@link Participant#any}
     */
    private Participant randomHearer() {
        final List<Participant> hearers = newArrayList(getParticipationFramework().getHearers());
        log.debug("Returning a random hearer from {}", hearers);
        return hearers.get(random.nextInt(hearers.size()));
    }

    /**
     * Returns true P*100% of the time (uniform distribution).
     *
     * @param P probability of event occuring
     * @return true if event occurs, else false
     */
    private boolean randBool(double P) {
        return P > Math.random();
    }
}

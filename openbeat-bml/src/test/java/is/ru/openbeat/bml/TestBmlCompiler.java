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

import is.ru.openbeat.behavior.EyebrowsBehavior;
import is.ru.openbeat.behavior.GazeBehavior;
import is.ru.openbeat.behavior.HeadnodBehavior;
import is.ru.openbeat.model.*;
import is.ru.openbeat.pipeline.ICompiler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBmlCompiler {
    private static final Logger log = LoggerFactory.getLogger(TestBmlCompiler.class);

    @Test
    public void testSimpleUtterance() {
        final Utterance utteranceFixture = Utterance.with(
            Clause.with(
                Theme.with(
                    Word.with("I").behaviors(new EyebrowsBehavior()).beginTime(0.3261844F).build(),
                    Word.with("saw").beginTime(0.5861022F).build(),
                    Word.with("a").beginTime(0.6229302F).build()
                ).behaviors(new GazeBehavior(GazeBehavior.Direction.AWAY_FROM_HEARER, 10)).build(),
                Rheme.with(
                    NounPhrase.with().features(
                        Word.with("little").beginTime(0.8247782F).build(),
                        Word.with("car").behaviors(new HeadnodBehavior()).beginTime(1.3050451F).build()
                    ).build()
                ).build()
            ).build()
        ).build();

        final ICompiler compiler = new BmlCompiler();
        String output = compiler.process(utteranceFixture);

        log.debug("Output from BmlCompiler {}", output);
    }
}

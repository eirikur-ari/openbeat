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

import is.ru.openbeat.model.*;
import static is.ru.openbeat.model.PhraseAttribute.ADJECTIVE_PHRASE;
import static is.ru.openbeat.model.PhraseAttribute.COORDINATING_CONJUNCTION_PHRASE;
import org.junit.Before;
import org.junit.Test;


public class TestConflictfilter {
    private ConflictFilter conflictFilter;

    private Utterance utteranceFixture;

    private Behavior1 b1;
    private Behavior2 b2;
    private Behavior3 b3;

    @Before
    public void setUp() {
        conflictFilter = new ConflictFilter();
        b1 = new Behavior1(10);
        b2 = new Behavior2();
        b3 = new Behavior3();
        b3.setPriority(15);

        utteranceFixture = Utterance.with(
            Clause.with(
                Theme.with(
                    NounPhrase.with().features(Word.with("I").behaviors(b1).build(), Word.with("never").build()).build(),
                    VerbPhrase.with().features(Word.with("saw").build()).build()
                ).behaviors(b2).build(),
                Rheme.with(
                    NounPhrase.with().features(Word.with("a").build()).build(),
                    NounPhrase.with().features(Word.with("little").build()).build(),
                    NounPhrase.with().features(Word.with("car").build()).build(),
                    Constituent.with(COORDINATING_CONJUNCTION_PHRASE).features(Word.with(",").build()).build()
                ).build()
            ).build(),
            Clause.with(
                Theme.with(
                    NounPhrase.with().features(Word.with("he").build()).build()
                ).build(),
                Rheme.with(
                    VerbPhrase.with().features(Word.with("was").build()).build(),
                    Constituent.with(ADJECTIVE_PHRASE).behaviors(b1).features(Word.with("red").build()).build(),
                    Constituent.with(COORDINATING_CONJUNCTION_PHRASE).features(Word.with(".").build()).build()
                ).behaviors(b3).build()
            ).build()
        ).build();
    }

    @Test
    public void isConflict() {
        conflictFilter.process(utteranceFixture);

    }
}

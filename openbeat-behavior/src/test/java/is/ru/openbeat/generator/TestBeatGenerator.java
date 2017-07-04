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

import com.google.inject.Inject;
import atunit.*;
import atunit.mockito.MockitoFramework;
import atunit.guice.GuiceContainer;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mockito.Mockito.verify;
import is.ru.openbeat.discourse.IDiscourseModel;
import is.ru.openbeat.model.*;
import static is.ru.openbeat.model.WordClassAttribute.*;
import static is.ru.openbeat.model.PhraseAttribute.COORDINATING_CONJUNCTION_PHRASE;
import static is.ru.openbeat.model.PhraseAttribute.ADJECTIVE_PHRASE;
import static is.ru.openbeat.model.NumberAttribute.SINGULAR;
import is.ru.openbeat.behavior.GestureBehavior;
import is.ru.openbeat.behavior.IBehavior;
import static is.ru.openbeat.knowledge.IGesture.*;

@RunWith(AtUnit.class)
@Container(GuiceContainer.class)
@MockFramework(MockitoFramework.class)
public class TestBeatGenerator {
    private static final Logger log = LoggerFactory.getLogger(TestBeatGenerator.class);

    @Inject
    @Unit
    private BeatGenerator beatGenerator;

    @Mock
    private IDiscourseModel discourseModel;

    private Utterance utteranceFixture;
    private Rheme rheme;

    @Mock
    private GestureBehavior gestureBehavior;

    @Before
    public void setUp() {

        utteranceFixture = Utterance.with(
            Clause.with(
                Theme.with(
                    NounPhrase.with().features(Word.with("I").attributes(PERSONAL_PRONOUN).build()).build(),
                    VerbPhrase.with().features(Word.with("saw").attributes(VERB).build()).build()
                ).build(),
                rheme =Rheme.with(
                    NounPhrase.with().features(Word.with("a").attributes(DETERMINER).build()).build(),
                    NounPhrase.with().features(Word.with("little").attributes(ADJECTIVE).build()).build(),
                    NounPhrase.with().features(Word.with("car").attributes(NOUN, SINGULAR).build()).build(),
                    Constituent.with(COORDINATING_CONJUNCTION_PHRASE).features(Word.with(",").attributes(PUNCTUATION).build()).build()
                ).behaviors(new GestureBehavior("beat", "offer", GestureType.RIGHT, 10)).build()
            ).build(),
            Clause.with(
                Theme.with(
                    NounPhrase.with().features(Word.with("it").attributes(PERSONAL_PRONOUN).build()).build()
                ).build(),
                Rheme.with(
                    VerbPhrase.with().features(Word.with("was").attributes(VERB).build()).build(),
                    Constituent.with(ADJECTIVE_PHRASE).features(Word.with("red").attributes(ADJECTIVE).build()).build(),
                    Constituent.with(COORDINATING_CONJUNCTION_PHRASE).features(
                        Word.with(".").attributes(PUNCTUATION).build()).build()
                ).build()
            ).build()
        ).build();
    }

    @Test
    public void TestBeatGenerator() {
        beatGenerator.process(utteranceFixture);

        log.debug("Got the behavior: {}", rheme.getBehaviors());
        assertThat(rheme.getBehaviors(), Matchers.<IBehavior>hasItem(new GestureBehavior("beat", "offer", GestureType.RIGHT, 10)));
    }
}

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

import atunit.*;
import atunit.guice.GuiceContainer;
import atunit.mockito.MockitoFramework;
import com.google.inject.Inject;
import is.ru.openbeat.behavior.EyebrowsBehavior;
import is.ru.openbeat.behavior.IBehavior;
import is.ru.openbeat.discourse.IDiscourseModel;
import is.ru.openbeat.model.*;
import static is.ru.openbeat.model.NumberAttribute.SINGULAR;
import static is.ru.openbeat.model.PhraseAttribute.ADJECTIVE_PHRASE;
import static is.ru.openbeat.model.PhraseAttribute.COORDINATING_CONJUNCTION_PHRASE;
import static is.ru.openbeat.model.WordClassAttribute.*;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AtUnit.class)
@Container(GuiceContainer.class)
@MockFramework(MockitoFramework.class)
public class TestEyebrowsBehaviorGenerator {
    @Inject
    @Unit
    private EyebrowsBehaviorGenerator eyebrowsBehaviorGenerator;

    @Mock
    private IDiscourseModel discourseModel;

    private Utterance utteranceFixture;
    private NounPhrase nounPhrase;

    @Before
    public void setUp() {
        utteranceFixture = Utterance.with(
            Clause.with(
                Theme.with(
                    NounPhrase.with().features(Word.with("I").attributes(PERSONAL_PRONOUN).build()).build(),
                    VerbPhrase.with().features(Word.with("saw").attributes(VERB).build()).build()
                ).build(),
                Rheme.with(
                    NounPhrase.with().features(Word.with("a").attributes(DETERMINER).build()).build(),
                    NounPhrase.with().features(Word.with("little").attributes(ADJECTIVE).build()).build(),
                    nounPhrase = NounPhrase.with().features(Word.with("car").attributes(NOUN, SINGULAR).build()).behaviors(
                        new EyebrowsBehavior()).build(),
                    Constituent.with(COORDINATING_CONJUNCTION_PHRASE).features(
                        Word.with(",").attributes(PUNCTUATION).build()).build()
                ).build()
            ).build(),
            Clause.with(
                Theme.with(
                    NounPhrase.with().features(Word.with("he").attributes(PERSONAL_PRONOUN).build()).build()
                ).build(),
                Rheme.with(
                    VerbPhrase.with().features(Word.with("was").attributes(VERB).build()).build(),
                    Constituent.with(ADJECTIVE_PHRASE).features(new Word("red", ADJECTIVE)).build(),
                    Constituent.with(COORDINATING_CONJUNCTION_PHRASE).features(
                        Word.with(".").attributes(PUNCTUATION).build()).build()
                ).build()
            ).build()
        ).build();
    }

    @Test
    public void matchEyebrows() {
        eyebrowsBehaviorGenerator.process(utteranceFixture);

        assertThat(nounPhrase.getBehaviors(), Matchers.<IBehavior>hasItem(new EyebrowsBehavior()));
    }
}

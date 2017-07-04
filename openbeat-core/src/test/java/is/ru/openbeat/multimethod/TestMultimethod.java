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

package is.ru.openbeat.multimethod;

import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import is.ru.openbeat.model.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMultimethod {
    private static final Logger log = LoggerFactory.getLogger(TestMultimethod.class);

    @Test
    public void testSimpleVerbPhraseOrder() {
        final VerbPhraseRecorder recorder = new VerbPhraseRecorder();
        recorder.mm.match(new Constituent(PhraseAttribute.ADVERB_PHRASE));
        recorder.mm.match(new VerbPhrase());
        recorder.mm.match(new Word("car"), new VerbPhrase());

        log.debug("VerbPhrase recorder ints: {}", recorder.ints);
        assertThat(recorder.ints, is(equalTo(newArrayList(1, 2, 3))));
    }

    @Test
    public void testSimpleNounPhraseOrder() {
        final NounPhraseRecorder recorder = new NounPhraseRecorder();
        recorder.mm.match(new Constituent(PhraseAttribute.ADVERB_PHRASE));
        recorder.mm.match(new NounPhrase());

        log.debug("NounPhrase recorder ints: {}", recorder.ints);
        assertThat(recorder.ints, is(equalTo(newArrayList(1, 2))));
    }

    @Test
    public void testSimpleNounPhraseWordOrder() {
        final NounPhraseRecorder recorder = new NounPhraseRecorder();
        recorder.mm.match(new Constituent(PhraseAttribute.ADVERB_PHRASE));
        recorder.mm.match(new NounPhrase());
        recorder.mm.match(Lists.newArrayList(), new Word("car"));

        log.debug("NounPhrase recorder ints: {}", recorder.ints);
        assertThat(recorder.ints, is(equalTo(newArrayList(1, 2, 3))));
    }

    @Test
    public void testComplexNounPhraseOrder() {
        final NounPhraseRecorder recorder = new NounPhraseRecorder();
        final StringBuilder sb = new StringBuilder();
        recorder.mm.match(1, sb, new Constituent(PhraseAttribute.ADVERB_PHRASE));
        recorder.mm.match(1, sb, new NounPhrase());

        log.debug("NounPhrase recorder ints: {}", recorder.ints);
        assertThat(recorder.ints, is(equalTo(newArrayList(10, 11))));
    }

    @Test
    public void testSimpleCompleteOrder() {
        final CompleteRecorder recorder = new CompleteRecorder();
        final StringBuilder sb = new StringBuilder();
        recorder.mm.match(new Rheme(), sb);
        recorder.mm.match(new Theme(), sb);
        recorder.mm.match(new NounPhrase(), 1, sb);
        recorder.mm.match(new VerbPhrase(), 1, sb);
        recorder.mm.match(new Constituent(PhraseAttribute.ADVERB_PHRASE), 1, sb);
        recorder.mm.match(new Word(""), 1, sb);

        assertThat(recorder.ints, is(equalTo(newArrayList(1, 2, 3, 4, 5, 6))));
    }

}

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

package is.ru.openbeat.icenlp;

import atunit.*;
import atunit.guice.GuiceContainer;
import atunit.mockito.MockitoFramework;
import static com.google.common.base.Join.join;
import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import is.ru.openbeat.discourse.DiscourseModule;
import is.ru.openbeat.model.Utterance;
import is.ru.openbeat.knowledge.IKnowledgeBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Needs to be run with vm parameter: "-Xmx512m" */
@RunWith(AtUnit.class)
@Container(GuiceContainer.class)
@MockFramework(MockitoFramework.class)
public class TestIceNlpSource extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(TestIceNlpSource.class);

    @Inject
    @Unit
    private IceNlpSource nlpSource;

    @Mock
    private IceNlpSegmentizer segmentizer;

    @Mock
    private IceNlpTagger tagger;

    @Mock
    private IceNlpParser parser;

    @Mock
    private IKnowledgeBase knowledgeBase;

    protected void configure() {
        install(new DiscourseModule());
    }

    @Test
    public void testSimpleString() {
        final String s = "Ég sá lítinn bíl, hann var rauður.";
        final String tagged = "Ég fp1en sá sfg1eþ lítinn lkeosf bíl nkeo , , hann fpken var sfg3eþ rauður lkensf . . ";
        final String parsed = join("\n",
            newArrayList("[NP Ég fp1en NP]", "[VP sá sfg1eþ VP]", "[NP [AP lítinn lkeosf AP] bíl nkeo NP]", ", ,",
                "[NP hann fpken NP]", "[VPb var sfg3eþ VPb]", "[AP rauður lkensf AP]", ". ."));

        when(segmentizer.segmentize(s)).thenReturn(newArrayList(s));
        when(tagger.tag(s)).thenReturn(tagged);
        when(parser.parse(tagged)).thenReturn(parsed);

        final Utterance utterance = nlpSource.process(s);
        log.debug("Utterance: {}", utterance);
    }
}

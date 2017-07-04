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

import static com.google.common.collect.Lists.newArrayList;
import is.ru.openbeat.discourse.*;
import is.ru.openbeat.model.*;
import static is.ru.openbeat.model.GenderAttribute.MASCULINE;
import static is.ru.openbeat.model.NumberAttribute.SINGULAR;
import static is.ru.openbeat.model.PersonAttribute.FIRST;
import static is.ru.openbeat.model.PersonAttribute.THIRD;
import static is.ru.openbeat.model.PhraseAttribute.ADJECTIVE_PHRASE;
import static is.ru.openbeat.model.WordClassAttribute.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TestInformationStructure {
    private static final Logger log = LoggerFactory.getLogger(TestInformationStructure.class);

    private IDiscourseModel discourseModel;
    private IDiscourseTagger discourseTagger;
    private InformationStructureBuilder informationStructureBuilder;

    private List<IFeatureStructure> features;

    @Before
    public void setUp() {
        discourseModel = new DiscourseModel();
        discourseTagger = new DiscourseTagger(discourseModel);
        informationStructureBuilder = new InformationStructureBuilder(discourseModel);
    }

    @Before
    public void setupFixture() {
        features = newArrayList();

        Constituent c;
        features.add(c = new NounPhrase());
        c.addFeature(new Word("Ég", PERSONAL_PRONOUN, FIRST, SINGULAR));

        features.add(c = new VerbPhrase());
        c.addFeature(new Word("sá", VERB, FIRST, SINGULAR));

        features.add(c = new NounPhrase());
        Constituent c1;
        c.addFeature(c1 = new Constituent(ADJECTIVE_PHRASE));
        c1.addFeature(new Word("lítinn", ADJECTIVE, MASCULINE, SINGULAR));
        c.addFeature(new Word("bíl", NOUN, MASCULINE, SINGULAR));

        features.add(new Word(","));

        features.add(c = new NounPhrase());
        c.addFeature(new Word("hann", PERSONAL_PRONOUN, THIRD, SINGULAR));

        features.add(c = new VerbPhrase());
        c.addFeature(new Word("var", VERB, FIRST, SINGULAR));

        features.add(c = new Constituent(ADJECTIVE_PHRASE));
        c.addFeature(new Word("rauður", ADJECTIVE, MASCULINE, SINGULAR));

        features.add(new Word("."));
    }

    @Test
    public void testDiscourse() {
        discourseTagger.tag(features);
        informationStructureBuilder.buildInformationStructure(features);

        //log.debug("######################################################");
        //log.debug("Created information structure: {}", informationStructure);
        //log.debug("######################################################");
    }
}
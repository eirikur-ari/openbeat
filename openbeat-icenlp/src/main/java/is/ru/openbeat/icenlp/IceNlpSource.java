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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.openbeat.discourse.*;
import is.ru.openbeat.model.*;
import is.ru.openbeat.pipeline.INlpSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
class IceNlpSource implements INlpSource {
    private static final Logger log = LoggerFactory.getLogger(IceNlpSource.class);

    private final IceNlpSegmentizer segmentizer;
    private final IceNlpTagger tagger;
    private final IceNlpParser parser;
    private final IceNlpModelBuilder modelBuilder;
    private final IDiscourseTagger discourseTagger;
    private final InformationStructureBuilder informationStructureBuilder;
    private final InformationChunker informationChunker;
    private final NounPhraseIdentifier nounPhraseIdentifier;
    private final VerbPhraseIdentifier verbPhraseIdentifier;

    @Inject
    IceNlpSource(IceNlpSegmentizer segmentizer, IceNlpTagger tagger, IceNlpParser parser,
                 IceNlpModelBuilder modelBuilder, IDiscourseTagger discourseTagger,
                 InformationStructureBuilder informationStructureBuilder, InformationChunker informationChunker,
                 NounPhraseIdentifier nounPhraseIdentifier, VerbPhraseIdentifier verbPhraseIdentifier) {
        this.segmentizer = segmentizer;
        this.tagger = tagger;
        this.parser = parser;
        this.modelBuilder = modelBuilder;
        this.discourseTagger = discourseTagger;
        this.informationStructureBuilder = informationStructureBuilder;
        this.informationChunker = informationChunker;
        this.nounPhraseIdentifier = nounPhraseIdentifier;
        this.verbPhraseIdentifier = verbPhraseIdentifier;
    }

    public Utterance process(String text) {
        log.debug("Processing {}", text);

        final List<Iterable<IFeatureStructure>> structure = newArrayList();

        final List<String> sentences = segmentizer.segmentize(text);
        for (String sentence : sentences) {
            final String taggedText = tagger.tag(sentence);
            log.debug("Tagged text: {}", taggedText);

            final String parsedText = parser.parse(taggedText);
            log.debug("Parsed text: {}", parsedText);

            structure.add(modelBuilder.build(parsedText));
        }

        for (Iterable<IFeatureStructure> featureStructure : structure) {
            discourseTagger.tag(featureStructure);
        }

        final List<Clause> clauses = newArrayList();
        for (Iterable<IFeatureStructure> sentenceStructure : structure) {
            final Iterable<Iterable<IFeatureStructure>> chunks = informationChunker.chunk(sentenceStructure);
            for (Iterable<IFeatureStructure> chunk : chunks) {
                final Pair<IArticulation, IArticulation> articulations = informationStructureBuilder.buildInformationStructure(
                    chunk);
                log.debug("Articulations: {}", articulations);
                clauses.add(new Clause(articulations));
            }
        }

        final Utterance utterance = new Utterance(clauses);

        // match subjects and actions against the discourse model
        nounPhraseIdentifier.identify(utterance);
        verbPhraseIdentifier.identify(utterance);

        return utterance;
    }

    public void clearState() {
        discourseTagger.clearState();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

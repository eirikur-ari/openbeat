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

package is.ru.openbeat.opennlp;

import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.openbeat.discourse.*;
import is.ru.openbeat.model.*;
import static is.ru.openbeat.model.PhraseAttribute.*;
import is.ru.openbeat.pipeline.INlpSource;
import opennlp.tools.lang.english.Tokenizer;
import opennlp.tools.lang.english.TreebankChunker;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.sentdetect.SentenceDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
class OpenNlpSource implements INlpSource {
    private static final Logger log = LoggerFactory.getLogger(OpenNlpSource.class);

    private final SentenceDetector sentenceDetector;
    private final Tokenizer tokenizer;
    private final POSTagger tagger;
    private final TreebankChunker chunker;
    private final OpenNlpTagResolver tagResolver;
    private final IDiscourseTagger discourseTagger;
    private final InformationStructureBuilder informationStructureBuilder;
    private final InformationChunker informationChunker;
    private final IDictionarySource dictionarySource;
    private final ContrastBuilder contrastBuilder;
    private final NounPhraseIdentifier nounPhraseIdentifier;
    private final VerbPhraseIdentifier verbPhraseIdentifier;

    @Inject
    OpenNlpSource(SentenceDetector sentenceDetector, Tokenizer tokenizer, POSTagger tagger, TreebankChunker chunker,
                  OpenNlpTagResolver tagResolver, IDiscourseTagger discourseTagger,
                  InformationStructureBuilder informationStructureBuilder, InformationChunker informationChunker,
                  IDictionarySource dictionarySource, ContrastBuilder contrastBuilder,
                  NounPhraseIdentifier nounPhraseIdentifier, VerbPhraseIdentifier verbPhraseIdentifier) {
        this.sentenceDetector = sentenceDetector;
        this.tokenizer = tokenizer;
        this.tagger = tagger;
        this.chunker = chunker;
        this.tagResolver = tagResolver;
        this.discourseTagger = discourseTagger;
        this.informationStructureBuilder = informationStructureBuilder;
        this.informationChunker = informationChunker;
        this.dictionarySource = dictionarySource;
        this.contrastBuilder = contrastBuilder;
        this.nounPhraseIdentifier = nounPhraseIdentifier;
        this.verbPhraseIdentifier = verbPhraseIdentifier;
    }

    public Utterance process(String text) {
        log.debug("Processing {}", text);

        final List<Iterable<IFeatureStructure>> structure = newArrayList();

        final String[] sentences = sentenceDetector.sentDetect(text);
        for (String sentence : sentences) {
            log.debug("Processing sentence {}", sentence);

            final List<IFeatureStructure> sentenceStructure = newArrayList();
            final String[] tokens = tokenizer.tokenize(sentence);
            final String[] tags = tagger.tag(tokens);
            final String[] chunks = chunker.chunk(tokens, tags);

            String lastChunkTag = chunks[0];
            Constituent lastConstituent = buildConstituent(chunkFor(lastChunkTag));
            for (int i = 0; i < tokens.length; i++) {
                final String token = tokens[i];
                final String tag = tags[i];
                final String chunkTag = chunks[i];
                final boolean chunkFollowing = matchingChunkTags(chunkTag, lastChunkTag);

                log.trace("[" + chunkTag + " {}/{}]", token, tag);

                if (!chunkTag.equals(lastChunkTag) && !chunkFollowing) {
                    sentenceStructure.add(lastConstituent);
                }

                if (!chunkTag.equals(lastChunkTag) && !chunkFollowing) {
                    lastConstituent = buildConstituent(chunkFor(chunkTag));
                }
                lastConstituent.addFeature(buildWord(token, tag));

                lastChunkTag = chunkTag;
                if (!chunkTag.equals(lastChunkTag) && !chunkFollowing) {
                    sentenceStructure.add(lastConstituent);
                }
            }
            sentenceStructure.add(lastConstituent);
            structure.add(sentenceStructure);
        }

        log.debug("Built structure: ");
        for (Iterable<IFeatureStructure> sentenceStructure : structure) {
            log.debug("  {}", sentenceStructure);
            discourseTagger.tag(sentenceStructure);
        }

        // split feature structure (constituents and words) into complete information structure (with clauses & theme/rheme)
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

        // mark contrast
        contrastBuilder.buildContrast(utterance);

        return utterance;
    }

    public void clearState() {
        discourseTagger.clearState();
    }

    private boolean matchingChunkTags(String chunk1, String chunk2) {
        if (chunk1.indexOf("-") > -1 && chunk2.indexOf("-") > -1) {
            String[] chunk1Parts = chunk1.split("-");
            String[] chunk2Parts = chunk2.split("-");

            if (chunk1Parts[1].equals(chunk2Parts[1]) && "I".equals(chunk1Parts[0]) && "B".equals(chunk2Parts[0])) {
                log.debug("Found matching chunks: {} is equal to {}", chunk1Parts[1], chunk2Parts[1]);
                return true;
            }
        }
        return false;
    }

    private String chunkFor(String chunkTag) {
        if (chunkTag.indexOf("-") > -1) {
            return chunkTag.substring(chunkTag.indexOf("-") + 1);
        }
        return chunkTag;
    }

    private Constituent buildConstituent(final String type) {
        if (type.startsWith("NP")) {
            return new NounPhrase();
        } else if (type.startsWith("VP")) {
            return new VerbPhrase();
        } else if (type.startsWith("ADVP")) {
            return new Constituent(ADVERB_PHRASE);
        } else if (type.startsWith("ADJP")) {
            return new Constituent(ADJECTIVE_PHRASE);
        } else if (type.startsWith("PP")) {
            return new Constituent(PREPOSITION_PHRASE);
        } else if (type.startsWith("O")) {
            return new Constituent(COORDINATING_CONJUNCTION_PHRASE);
        }

        log.debug("Unknown constituent type: {}", type);
        return new Constituent(new IConstituentAttribute() {
            @Override
            public String toString() {
                return "Unknown constituent type: " + type;
            }
        });
    }

    private Word buildWord(String token, String tag) {
        final Word word = new Word(token, tagResolver.resolveAttributes(tag));
        word.setLemma(dictionarySource.findLemma(word));
        return word;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

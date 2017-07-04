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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import is.ru.cs.nlp.icenlp.core.icemorphy.IceMorphy;
import is.ru.cs.nlp.icenlp.core.icetagger.IceTagger;
import is.ru.cs.nlp.icenlp.core.icetagger.IceTaggerLexicons;
import is.ru.cs.nlp.icenlp.core.tokenizer.Tokenizer;
import is.ru.cs.nlp.icenlp.core.utils.Lexicon;
import is.ru.cs.nlp.icenlp.facade.IceParserFacade;
import is.ru.openbeat.discourse.IDiscourseModel;
import is.ru.openbeat.discourse.IDiscourseTagger;
import is.ru.openbeat.pipeline.INlpSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class IceNlpModule extends AbstractModule {
    private static final String ICENLP_PATH_PREFIX = "models/icenlp/";

    protected void configure() {
        requireBinding(IDiscourseModel.class);
        requireBinding(IDiscourseTagger.class);

        final Multibinder<INlpSource> multibinder = Multibinder.newSetBinder(binder(), INlpSource.class);
        multibinder.addBinding().to(IceNlpSource.class);
    }

    @Provides
    @Singleton
    public Lexicon createLexicon() throws IOException {
        return new Lexicon(fromFile(ICENLP_PATH_PREFIX + "dict/tokenizer/lexicon.txt"));
    }

    @Provides
    @Singleton
    public Tokenizer createTokenizer(Lexicon lexicon) {
        Tokenizer tokenizer = new Tokenizer(2, true, lexicon);
        tokenizer.findMultiWords(false);
        return tokenizer;
    }

    @Provides
    @Singleton
    public IceTaggerLexicons createIceTaggerLexicons() throws IOException {
        return new IceTaggerLexicons(fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/baseDict.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/otb.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/baseEndings.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/otb.endings.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/otb.endingsProper.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/otb.verbPrep.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/otb.verbObj.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/otb.verbAdverb.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/idioms.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/prefixes.dict"),
            fromFile(ICENLP_PATH_PREFIX + "dict/icetagger/otbTags.freq.dict"));
    }

    // the following can be used to use IceNLP TriTagger and supplying it as the second last parameter to new IceTagger(...)
    /*@Provides
    @Singleton
    public TriTaggerLexicons createTriTaggerLexicons() throws IOException {
        return new TriTaggerLexicons(fromFile(ICENLP_PATH_PREFIX + "ngrams/models/otb.ngram"),
            fromFile(ICENLP_PATH_PREFIX + "ngrams/models/otb.lambda"),
            fromFile(ICENLP_PATH_PREFIX + "ngrams/models/otb.lex"), true);
    }

    @Provides
    @Singleton
    public TriTagger createTriTagger(TriTaggerLexicons triTaggerLexicons) {
        return new TriTagger(0, 3, triTaggerLexicons.ngrams, triTaggerLexicons.freqLexicon, null, null, null);
    }*/

    @Provides
    @Singleton
    public IceMorphy createIceMorphy(IceTaggerLexicons iceTaggerLexicons) {
        return new IceMorphy(iceTaggerLexicons.morphyLexicons.dict, iceTaggerLexicons.morphyLexicons.baseDict,
            iceTaggerLexicons.morphyLexicons.endingsBase, iceTaggerLexicons.morphyLexicons.endings,
            iceTaggerLexicons.morphyLexicons.endingsProper, iceTaggerLexicons.morphyLexicons.prefixes,
            iceTaggerLexicons.morphyLexicons.tagFrequency, null);
    }

    @Provides
    @Singleton
    public IceTagger createIceTagger(IceMorphy iceMorphy, IceTaggerLexicons iceTaggerLexicons) {
        return new IceTagger(0, null, iceMorphy, iceTaggerLexicons.morphyLexicons.baseDict,
            iceTaggerLexicons.morphyLexicons.dict, iceTaggerLexicons.idioms, iceTaggerLexicons.verbPrep,
            iceTaggerLexicons.verbObj, iceTaggerLexicons.verbAdverb, false, true, null, false);
    }

    /*public Lemmatizer createLemmatizer() {
        return new Lemmatizer(ICENLP_PATH_PREFIX + "dict/lemmald/settings.txt");
    }*/

    @Provides
    @Singleton
    public IceParserFacade createIceParserFacade() {
        return new IceParserFacade();
    }

    private InputStream fromFile(String file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private InputStream fromClasspath(String file) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
    }
}

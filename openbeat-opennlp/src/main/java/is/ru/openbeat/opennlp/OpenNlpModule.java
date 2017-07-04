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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import is.ru.openbeat.discourse.IDictionarySource;
import is.ru.openbeat.discourse.IDiscourseModel;
import is.ru.openbeat.discourse.IDiscourseTagger;
import is.ru.openbeat.pipeline.INlpSource;
import opennlp.maxent.io.BinaryGISModelReader;
import opennlp.maxent.io.GISModelReader;
import opennlp.maxent.io.PlainTextGISModelReader;
import opennlp.tools.lang.english.Tokenizer;
import opennlp.tools.lang.english.TreebankChunker;
import opennlp.tools.postag.*;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class OpenNlpModule extends AbstractModule {
    private static final String OPENNLP_PATH_PREFIX = "models/opennlp/";

    protected void configure() {
        requireBinding(IDictionarySource.class);
        requireBinding(IDiscourseModel.class);
        requireBinding(IDiscourseTagger.class);

        final Multibinder<INlpSource> multibinder = Multibinder.newSetBinder(binder(), INlpSource.class);
        multibinder.addBinding().to(OpenNlpSource.class);
    }

    @Provides
    @Singleton
    public SentenceDetector createSentenceDetector() throws IOException {
        return new SentenceDetectorME(
            createGisModelReader(OPENNLP_PATH_PREFIX + "english/sentdetect/EnglishSD.bin.gz").getModel()) {
            {
                useTokenEnd = true;
            }
        };
    }

    @Provides
    @Singleton
    public Tokenizer createTokenizer() throws IOException {
        return new Tokenizer(
            createGisModelReader(OPENNLP_PATH_PREFIX + "english/tokenize/EnglishTok.bin.gz").getModel());
    }

    @Provides
    @Singleton
    public TagDictionary createTagDictionary() throws IOException {
        return new POSDictionary(
            new BufferedReader(new InputStreamReader(fromFile(OPENNLP_PATH_PREFIX + "english/postag/tagdict"))),
            true);
    }

    @Provides
    @Singleton
    public POSTagger createPosTagger(TagDictionary tagDictionary) throws IOException {
        return new POSTaggerME(createGisModelReader(OPENNLP_PATH_PREFIX + "english/postag/tag.bin.gz").getModel(),
            new DefaultPOSContextGenerator(null), tagDictionary);
    }

    @Provides
    @Singleton
    public TreebankChunker createTreebankChunker() throws IOException {
        return new TreebankChunker(
            createGisModelReader(OPENNLP_PATH_PREFIX + "english/chunker/EnglishChunk.bin.gz").getModel());
    }

    private GISModelReader createGisModelReader(String resource) throws IOException {
        final InputStream inputStream;
        if (resource.endsWith(".gz")) {
            inputStream = new BufferedInputStream(
                new GZIPInputStream(new BufferedInputStream(fromFile(resource))));
            resource = resource.substring(0, resource.length() - 3);
        } else {
            inputStream = new BufferedInputStream(fromFile(resource));
        }

        if (resource.endsWith(".bin")) {
            return new BinaryGISModelReader(new DataInputStream(inputStream));
        } else {
            return new PlainTextGISModelReader(new BufferedReader(new InputStreamReader(inputStream)));
        }
    }

    private InputStream fromFile(String file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private InputStream fromClasspath(String file) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
    }
}

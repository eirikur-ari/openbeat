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

import com.google.common.base.Function;
import static com.google.common.base.Join.join;
import com.google.common.base.Nullable;
import static com.google.common.collect.Iterables.transform;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.cs.nlp.icenlp.core.icetagger.IceTagger;
import is.ru.cs.nlp.icenlp.core.tokenizer.IceTokenTags;
import is.ru.cs.nlp.icenlp.core.tokenizer.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Singleton
class IceNlpTagger {
    private static final Logger log = LoggerFactory.getLogger(IceNlpTagger.class);

    private final IceTagger tagger;
    private final Tokenizer tokenizer;

    @Inject
    IceNlpTagger(IceTagger tagger, Tokenizer tokenizer) {
        this.tagger = tagger;
        this.tokenizer = tokenizer;
    }

    String tag(String sentence) {
        try {
            return unsafeTag(sentence);
        } catch (IOException e) {
            log.warn("Could not tag text", e);
            throw new RuntimeException(e);
        }
    }

    String unsafeTag(String sentence) throws IOException {
        if (!"".equals(sentence)) {
            tokenizer.tokenize(sentence);

            if (!tokenizer.tokens.isEmpty()) {
                tokenizer.splitAbbreviations();
                tagger.tagTokens(tokenizer.tokens);

                return (join(" ", transform(tokenizer.tokens, new Function<IceTokenTags, String>() {
                    public String apply(@Nullable IceTokenTags from) {
                        return from.lexeme + " " + from.getFirstTagStr();
                    }
                })));
            }
        }
        return sentence;
    }
}

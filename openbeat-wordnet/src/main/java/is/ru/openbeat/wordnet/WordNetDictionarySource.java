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

package is.ru.openbeat.wordnet;

import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.IStemmer;
import is.ru.openbeat.discourse.IDictionarySource;
import is.ru.openbeat.model.IWordAttribute;
import is.ru.openbeat.model.Word;
import is.ru.openbeat.model.WordAttributeType;
import is.ru.openbeat.model.WordClassAttribute;

import java.util.List;

@Singleton
class WordNetDictionarySource implements IDictionarySource {
    private final IDictionary dictionary;

    private final IStemmer stemmer;

    @Inject
    WordNetDictionarySource(IDictionary dictionary, IStemmer stemmer) {
        this.dictionary = dictionary;
        this.stemmer = stemmer;
    }

    public String findLemma(Word word) {
        final IWordAttribute wordClassAttribute = word.findAttributeOf(WordAttributeType.WordClass.class);

        final POS pos;
        if (WordClassAttribute.NOUN.equals(wordClassAttribute)) {
            pos = POS.NOUN;
        } else if (WordClassAttribute.ADJECTIVE.equals(wordClassAttribute)) {
            pos = POS.ADJECTIVE;
        } else if (WordClassAttribute.ADVERB.equals(wordClassAttribute)) {
            pos = POS.ADVERB;
        } else if (WordClassAttribute.VERB.equals(wordClassAttribute)) {
            pos = POS.VERB;
        } else {
            pos = null;
        }

        final List<String> stems = stemmer.findStems(word.getToken(), pos);
        if (!stems.isEmpty()) {
            return stems.get(0);
        }
        return null;
    }

    public List<String> findContrasts(Word word) {
        final IWordAttribute wordClassAttribute = word.findAttributeOf(WordAttributeType.WordClass.class);
        List<String> contrastWords = newArrayList();

        if (WordClassAttribute.ADJECTIVE.equals(wordClassAttribute)) {
            Pointer antonymPtr = Pointer.ANTONYM;
            IIndexWord indexWord = dictionary.getIndexWord(word.getLemma(), POS.ADJECTIVE);
            if (indexWord != null) {
                IWordID wordId = indexWord.getWordIDs().get(0);
                IWord iWord = dictionary.getWord(wordId);
                List<IWordID> antonyms = iWord.getRelatedWords(antonymPtr);
                for (IWordID antonym : antonyms) {
                    contrastWords.add(antonym.getLemma());
                }
            }
        }
        return contrastWords;
    }
}

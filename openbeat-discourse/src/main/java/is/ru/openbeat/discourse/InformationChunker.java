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

package is.ru.openbeat.discourse;

import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Singleton;
import is.ru.openbeat.model.Constituent;
import is.ru.openbeat.model.IFeatureStructure;
import is.ru.openbeat.model.Word;
import static is.ru.openbeat.model.WordClassAttribute.PUNCTUATION;
import static is.ru.openbeat.model.WordClassAttribute.VERB;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class <tt>InformationChunker</tt> chunks sentences into featurestructures
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
@Singleton
public class InformationChunker {
    private static final Logger log = LoggerFactory.getLogger(InformationChunker.class);

    private final Multimethod mm = new Multimethod(this);

    private final List<Iterable<IFeatureStructure>> clauses = newArrayList();
    private final List<IFeatureStructure> features = newArrayList();
    private boolean hasVerb;

    public InformationChunker() {
    }

    public Iterable<Iterable<IFeatureStructure>> chunk(Iterable<IFeatureStructure> sentences) {
        clauses.clear();
        features.clear();
        hasVerb = false;

        for (IFeatureStructure featureStructure : sentences) {
            features.add(featureStructure);
            mm.match(featureStructure);
        }

        if (clauses.isEmpty()) {
            log.warn("Sentence probably didn't end with punctuation, cleaning up afterwards");
            clauses.add(newArrayList(features));
        }
        return clauses;
    }

    @Multi
    public void handleWord(Word word) {
        if (word.is(VERB)) {
            hasVerb = true;
        }

        if (word.is(PUNCTUATION)) {
            if (hasVerb) {
                clauses.add(newArrayList(features));
                features.clear();
                hasVerb = false;
            }
        }
    }

    @Multi
    public void handleConstituent(Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(featureStructure);
        }
    }
}

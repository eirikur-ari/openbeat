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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.pipeline.ICompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class <tt>DiscourseCompiler</tt> prints a string describing elements in the utterance structure
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
@Singleton
public class DiscourseCompiler implements ICompiler {
    private static final Logger log = LoggerFactory.getLogger(DiscourseCompiler.class);

    private static final String NEWLINE = "\n";

    private final Multimethod mm = new Multimethod(this);

    private final IDiscourseModel discourseModel;

    @Inject
    public DiscourseCompiler(IDiscourseModel discourseModel) {
        this.discourseModel = discourseModel;
    }

    public String process(Utterance utterance) {
        final StringBuilder sb = new StringBuilder();

        sb.append("Utterance");
        sb.append(NEWLINE);
        for (Clause clause : utterance.getClauses()) {
            sb.append("  + Clause");
            sb.append(NEWLINE);
            mm.match(sb, clause.getArticulations().getFirst());
            mm.match(sb, clause.getArticulations().getSecond());
        }
        sb.append(NEWLINE);

        // collect discourse model
        new DiscourseCollector(discourseModel, sb);

        return sb.toString();
    }

    @Multi
    private void handleRheme(StringBuilder sb, Rheme rheme) {
        sb.append("    + Rheme");
        sb.append(NEWLINE);
        for (IFeatureStructure featureStructure : rheme.getPhrases()) {
            mm.match(6, sb, featureStructure);
        }
    }

    @Multi
    private void handleTheme(StringBuilder sb, Theme theme) {
        sb.append("    + Theme");
        sb.append(NEWLINE);
        for (IFeatureStructure featureStructure : theme.getPhrases()) {
            mm.match(6, sb, featureStructure);
        }
    }

    @Multi
    private void handleNounPhrase(Integer indent, StringBuilder sb, NounPhrase nounPhrase) {
        sb.append(spaceFor(indent));
        sb.append("+ NP");
        sb.append(NEWLINE);
        for (IFeatureStructure featureStructure : nounPhrase.getFeatures()) {
            mm.match(indent + 2, sb, featureStructure);
        }
    }

    @Multi
    private void handleVerbPhrase(Integer indent, StringBuilder sb, VerbPhrase verbPhrase) {
        sb.append(spaceFor(indent));
        sb.append("+ VP");
        sb.append(NEWLINE);
        for (IFeatureStructure featureStructure : verbPhrase.getFeatures()) {
            mm.match(indent + 2, sb, featureStructure);
        }
    }

    @Multi
    private void handleConstituent(Integer indent, StringBuilder sb, Constituent constituent) {
        sb.append(spaceFor(indent));
        sb.append("+ Constituent (").append(constituent.getType()).append(")");
        sb.append(NEWLINE);
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(indent + 2, sb, featureStructure);
        }
    }

    @Multi
    private void handleWord(Integer indent, StringBuilder sb, Word word) {
        sb.append(spaceFor(indent));
        sb.append("- ");
        sb.append(word.getToken());
        sb.append(NEWLINE);
    }

    private String spaceFor(Integer level) {
        String spaces = "";
        for (int i = 0; i < level; i++) {
            spaces += " ";
        }
        return spaces;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    static class DiscourseCollector {
        private final Multimethod mm = new Multimethod(this);

        DiscourseCollector(IDiscourseModel discourseModel, StringBuilder sb) {
            sb.append("Discourse model:").append(NEWLINE);
            for (IDiscourseEntity entity : discourseModel.getEntities()) {
                sb.append(entity.getId());
                sb.append(": ");
                for (Constituent constituent : entity.getReferingExpressions()) {
                    for (IFeatureStructure featureStructure : constituent.getFeatures()) {
                        mm.match(sb, featureStructure);
                    }
                    sb.append(",");
                }
                sb.append(NEWLINE);
            }
        }

        @Multi
        void handleConstituent(StringBuilder sb, Constituent constituent) {
            for (IFeatureStructure featureStructure : constituent.getFeatures()) {
                mm.match(sb, featureStructure);
            }
        }

        @Multi
        void handleWord(StringBuilder sb, Word word) {
            sb.append(word.getToken()).append(" ");
        }
    }
}

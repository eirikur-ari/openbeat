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

package is.ru.openbeat.console;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import is.ru.openbeat.pipeline.IBehaviorGenerator;
import is.ru.openbeat.pipeline.INlpSource;
import is.ru.openbeat.pipeline.IPipelineRunner;
import is.ru.openbeat.pipeline.ITimingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Class <tt>ConsoleRunner</tt> runs the application in console.
 * <p/>
 * Note: this hasn't been maintained since the GUI was introduced.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
@Singleton
public class ConsoleRunner implements IPipelineRunner {
    private static final Logger log = LoggerFactory.getLogger(ConsoleRunner.class);
    private static final String NEWLINE = "\n";

    private final Multimethod mm = new Multimethod(this);
    private final Scanner scanner = new Scanner(System.in);

    private final List<INlpSource> nlpSources;
    private final Set<IBehaviorGenerator> generators;
    private INlpSource currentSource;
    private final ITimingSource timingSource;

    @Inject
    public ConsoleRunner(List<INlpSource> nlpSources, Set<IBehaviorGenerator> generators, ITimingSource timingSource) {
        this.nlpSources = nlpSources;
        this.generators = generators;
        this.timingSource = timingSource;
    }

    public void run() {
        selectNlpSource();
        System.out.print("> Type utterances at the prompt or '.' to exit.");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if ("switch nlp".equals(line)) {
                selectNlpSource();
            } else if (".".equals(line)) {
                break;
            } else if (!line.isEmpty()) {
                Utterance utterance = currentSource.process(line);
                for (IBehaviorGenerator generator : generators) {
                    utterance = generator.process(utterance);
                }
                utterance = timingSource.process(utterance);

                System.out.println("> Output from NLP source:");
                for (Clause clause : utterance.getClauses()) {
                    System.out.println(String.format("- Clause [%s]", clause.getBehaviors()));
                    mm.match(clause.getArticulations().getFirst());
                    mm.match(clause.getArticulations().getSecond());
                }
                System.out.print(NEWLINE + "> ");
            } else {
                System.out.print(NEWLINE + "> ");
            }
        }
    }

    private void selectNlpSource() {
        log.debug("Nlp sources: {}", nlpSources);

        if (nlpSources.size() > 1) {
            System.out.print(NEWLINE
                + "> Multiple NLP sources configured, please select one (type 'switch nlp' to switch later on):");
            for (int i = 0; i < nlpSources.size(); i++) {
                System.out.print(NEWLINE + String.format("  [%s] %s", i, nlpSources.get(i)));
            }
            while (true) {
                System.out.print(NEWLINE + "> ");

                int i = scanner.nextInt();
                if (i >= 0 && i < nlpSources.size()) {
                    currentSource = nlpSources.get(i);
                    break;
                }
            }
        } else if (nlpSources.size() == 1) {
            currentSource = nlpSources.get(0);
            System.out.println(String.format("> One NLP source configured: %s", currentSource));
        } else {
            throw new RuntimeException("No nlp source configured, cannot proceed.");
        }

        log.debug("Using {} for current source", currentSource);
    }

    private void outputIndent(Integer indent) {
        if (indent == 0) return;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        System.out.print(sb.toString());
    }

    @Multi
    private void handleWord(Integer indent, Word word) {
        outputIndent(indent);
        System.out.println(String.format("    - Word [%s, %s]", word.getToken(), word.getBehaviors()));
    }

    @Multi
    private void handleConstituent(Integer indent, Constituent constituent) {
        outputIndent(indent);
        System.out.println(
            String.format("    - %s (%s) [%s]", constituent.getClass().getSimpleName(), constituent.getType(),
                constituent.getBehaviors()));
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(indent + 1, featureStructure);
        }
    }

    @Multi
    private void handleRheme(Rheme rheme) {
        System.out.println("  - " + rheme.getClass().getSimpleName() + ": " + rheme.getBehaviors());
        for (IFeatureStructure featureStructure : rheme.getPhrases()) {
            mm.match(0, featureStructure);
        }
    }

    @Multi
    private void handleTheme(Theme theme) {
        System.out.println("  - " + theme.getClass().getSimpleName() + ": " + theme.getBehaviors());
        for (IFeatureStructure featureStructure : theme.getPhrases()) {
            mm.match(0, featureStructure);
        }
    }
}

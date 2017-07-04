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
import com.google.inject.Inject;
import is.ru.openbeat.model.*;
import is.ru.openbeat.multimethod.Multi;
import is.ru.openbeat.multimethod.Multimethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class <tt>InformationStructureBuilder</tt> creates information struction from a given feature structure. Divides each <tt>CLAUSE</tt> into a <tt>RHEME</tt> and a <tt>THEME</tt> according to
 * heuristic rules that look at the location of <tt>NEW</tt> tagged words with respect to the verb head of the clause
 *
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
public class InformationStructureBuilder {
    private static final Logger log = LoggerFactory.getLogger(InformationStructureBuilder.class);

    private final Multimethod mm = new Multimethod(this);

    private final IDiscourseModel discourseModel;
    private final List<IFeatureStructure> pre = newArrayList();
    private final List<IFeatureStructure> post = newArrayList();
    private VerbPhrase verb;

    private boolean preVerb;
    private boolean hasVerb;
    private boolean postVerb;
    private boolean focused;

    /**
     * Constructor InformationStructureBuilder creates a new <tt>InformationStructureBuilder</tt> instance.
     *
     * @param discourseModel the discourseModel
     */
    @Inject
    public InformationStructureBuilder(IDiscourseModel discourseModel) {
        this.discourseModel = discourseModel;
    }


    /**
     * Method buildInformationStructure builds information structure from a given feature structure.
     *
     * @param structure the feature structure
     * @return Pair<IArticulation, IArticulation> the information structure
     */
    @SuppressWarnings({"ConstantConditions"})
    public Pair<IArticulation, IArticulation> buildInformationStructure(Iterable<IFeatureStructure> structure) {
        pre.clear();
        post.clear();
        verb = null;

        preVerb = false;
        hasVerb = false;
        postVerb = false;
        focused = false;

        for (IFeatureStructure featureStructure : structure) {
            log.trace("Matching on {}", featureStructure);
            mm.match(featureStructure);

            if (verb == null) {
                pre.add(featureStructure);
            } else if (!verb.equals(featureStructure)) {
                post.add(featureStructure);
            }
        }

        final List<IFeatureStructure> themes = newArrayList();
        final List<IFeatureStructure> rhemes = newArrayList();

        log.trace("preVerb: {}", preVerb);
        log.trace("hasVerb: {}", hasVerb);
        log.trace("postVerb: {}", postVerb);
        if (!preVerb && !hasVerb && !postVerb) {
            log.debug("Case 1");
            themes.addAll(pre);
            addVerb(rhemes, verb);
            rhemes.addAll(post);
            return new Pair<IArticulation, IArticulation>(new Theme(themes), new Rheme(rhemes));
        } else if (preVerb && hasVerb && !postVerb) {
            log.debug("Case 2");
            themes.addAll(pre);
            addVerb(rhemes, verb);
            rhemes.addAll(post);
            return new Pair<IArticulation, IArticulation>(new Theme(themes), new Rheme(rhemes));
        } else if (preVerb && !hasVerb && postVerb) {
            log.debug("Case 3");
            themes.addAll(pre);
            addVerb(themes, verb);
            rhemes.addAll(post);
            return new Pair<IArticulation, IArticulation>(new Theme(themes), new Rheme(rhemes));
        } else if (!preVerb && hasVerb && postVerb) {
            log.debug("Case 4");
            themes.addAll(pre);
            addVerb(themes, verb);
            rhemes.addAll(post);
            return new Pair<IArticulation, IArticulation>(new Theme(themes), new Rheme(rhemes));
        } else if (postVerb && preVerb && hasVerb) {
            log.debug("Case 5");
            themes.addAll(pre);
            addVerb(themes, verb);
            rhemes.addAll(post);
            return new Pair<IArticulation, IArticulation>(new Theme(themes), new Rheme(rhemes));
        } else if (preVerb && !hasVerb && !postVerb) {
            log.debug("Case 6");
            rhemes.addAll(pre);
            addVerb(themes, verb);
            themes.addAll(post);
            return new Pair<IArticulation, IArticulation>(new Rheme(rhemes), new Theme(themes));
        } else if (!preVerb && hasVerb && !postVerb) {
            log.debug("Case 7");
            themes.addAll(pre);
            addVerb(rhemes, verb);
            rhemes.addAll(post);
            return new Pair<IArticulation, IArticulation>(new Theme(themes), new Rheme(rhemes));
        } else if (!preVerb && !hasVerb && postVerb) {
            log.debug("Case 8");
            themes.addAll(pre);
            addVerb(themes, verb);
            rhemes.addAll(post);
            return new Pair<IArticulation, IArticulation>(new Theme(themes), new Rheme(rhemes));
        }

        throw new IllegalStateException(
                String.format("No legal state after processing themes/rhemes with preVerb: %s, hasVerb: %s, postVerb: %s",
                        preVerb, hasVerb, postVerb));
    }

    private void addVerb(List<IFeatureStructure> collection, IFeatureStructure verb) {
        if (verb != null) {
            collection.add(verb);
        } else {
            log.warn("No verb found");
        }                                                         
    }

    @Multi
    private void handleConstituent(Constituent constituent) {
        for (IFeatureStructure featureStructure : constituent.getFeatures()) {
            mm.match(featureStructure);
        }
    }

    @Multi
    private void handleNounPhrase(NounPhrase nounPhrase) {
        for (IFeatureStructure featureStructure : nounPhrase.getFeatures()) {
            mm.match(nounPhrase, featureStructure);
        }

        if (verb == null) {
            preVerb = preVerb || focused;
        } else {
            postVerb = postVerb || focused;
        }
    }

    /**
     * Method handleVerbPhrase is the part of the algorithm described in the class that handles {@link is.ru.openbeat.model.VerbPhrase} instances.
     *
     * @param verbPhrase the verb phrase
     */
    @Multi
    private void handleVerbPhrase(VerbPhrase verbPhrase) {
        log.debug("Handling verb phrase");
        for (IFeatureStructure featureStructure : verbPhrase.getFeatures()) {
            mm.match(verbPhrase, featureStructure);
        }

        if (verb == null) {
            log.debug("Setting verb phrase: {}", verbPhrase);
            verb = verbPhrase;
            hasVerb = focused;
        }
    }

    /**
     * Method handleWord checks if a given word is <tt>NEW</tt>.
     *
     * @param constituent the constituent this words belongs to
     * @param word the word
     */
    @Multi
    private void handleWord(Constituent constituent, Word word) {
        focused = discourseModel.isNew(word);
    }
}

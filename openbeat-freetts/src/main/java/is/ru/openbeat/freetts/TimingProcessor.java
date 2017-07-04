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

package is.ru.openbeat.freetts;

import static com.google.common.collect.Lists.immutableList;
import static com.google.common.collect.Lists.newArrayList;
import com.google.inject.Singleton;
import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.UtteranceProcessor;
import is.ru.openbeat.model.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Class <tt>TimingProcessor</tt> processes word end times from FreeTTS. It does so by listing phonemes and their end
 * times and selecting the last one for the current token.
 * <p/>
 * See <a href="http://freetts.sourceforge.net/docs/ProgrammerGuide.html"><i>http://freetts.sourceforge.net/docs/ProgrammerGuide.html</i></a>
 * for more information on how FreeTTS operates.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
@Singleton
class TimingProcessor implements UtteranceProcessor {
    private static final Logger log = LoggerFactory.getLogger(TimingProcessor.class);

    private final List<Pair<String, Float>> timings = newArrayList();

    public void processUtterance(Utterance utterance) throws ProcessException {
        timings.clear();

        //utterance.dump(new java.io.PrintWriter(System.out), 1, "");
        //log.info("syl struct: {}", utterance.getRelation("SylStructure"));

        float lastEnding = 0;
        for (Item sylStructure = utterance.getRelation("SylStructure").getHead();
             sylStructure != null; sylStructure = sylStructure.getNext()) {

            final Item d1 = sylStructure.getDaughter();
            if (d1 != null) {
                final String name = sylStructure.getFeatures().getString("name");
                final Item d2 = d1.getLastDaughter();
                timings.add(Pair.of(name, lastEnding));
                log.debug("Assigning {} as begin time for '{}'", lastEnding, name);
                lastEnding = d2.getFeatures().getFloat("end");
            }
        }
        log.debug("Assigned {}", timings);
    }

    public List<Pair<String, Float>> getTimings() {
        return immutableList(timings);
    }
}

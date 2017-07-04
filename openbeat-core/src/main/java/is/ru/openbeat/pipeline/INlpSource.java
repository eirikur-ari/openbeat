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

package is.ru.openbeat.pipeline;

import is.ru.openbeat.model.Utterance;

/**
 * Interface <tt>INlpSource</tt> creates object model structure from text.
 * <p/>
 * More specifically, this part of the pipeline process consumes a {@link String} and produces an {@link Utterance}.
 * <p/>
 * Example: This varies between implementation but given the text "<em>I saw a little car</em>", the NLP source could
 * produce something like this: <ul> <li>Utterance <ul> <li>Clause<ul> <li>Theme<ul><li>NounPhrase
 * (I)</li><li>VerbPhrase (saw)</li></ul></li> <li>Rheme<ul><li>NounPhrase (a)</li><li>NounPhrase
 * (little)</li><li>NounPhrase (car)</li><li>,</li></ul></li></ul> </li> <li>Clause<ul> <li>Theme<ul><li>NounPhrase
 * (he)</li></ul></li> <li>Rheme<ul><li>VerbPhrase (was)</li><li>Constituent (red)</li><li>.</li></ul></li></ul>
 * </li></ul> </li> </ul>
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public interface INlpSource extends IPipe<String, Utterance> {

    /**
     * Method clearState clears all state associated with the NLP source.
     * <p/>
     * Specifically the discourse model but any other local (private members) to the source, should be cleared or
     * reset.
     */
    void clearState();
}

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

import is.ru.openbeat.model.Constituent;
import is.ru.openbeat.model.Word;

import java.util.List;

/**
 * Interface <tt>IDiscourseEntity</tt> is a <tt>NEW</tt> word in a discourse with references to the refering
 * expressions.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public interface IDiscourseEntity {
    /**
     * Method getId gets the id of this <tt>IDiscourseEntity</tt> object.
     *
     * @return String the identifier
     */
    String getId();

    /**
     * Method getWord gets the word of this <tt>IDiscourseEntity</tt> object.
     *
     * @return IWord the word
     */
    Word getWord();

    /**
     * Method matches checks if given word matches the word assigned in the discourse entity.
     *
     * @param word the word
     * @return boolean <tt>true</tt> if word matches
     */
    boolean matches(Word word);

    /**
     * Method addReferer adds refering expression to this particular discourse entity.
     *
     * @param constituent the refering expression
     */
    void addReferer(Constituent constituent);


    /**
     * Method getReferingExpressions gets the referingExpressions of this <tt>IDiscourseEntity</tt> object.
     *
     * @return List<Constituent> list of refering expressions
     */
    List<Constituent> getReferingExpressions();
}

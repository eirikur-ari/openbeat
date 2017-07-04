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

import is.ru.openbeat.model.Word;

/**
 * Interface <tt>IDiscourseModel</tt> is used to keep track of the <tt>Context</tt> of the ongoing discourse.
 * <p/>
 * TODO: investigate an abstraction of a discourse model, this looks like it only supports <em>recency method</em>.
 * 
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public interface IDiscourseModel {
    /**
     * Method addEntity adds a <tt>NEW</tt> entity to the discourse model.
     *
     * @param entity the entity
     */
    void addEntity(IDiscourseEntity entity);

    /**
     * Method refer refers a particular discourse entity in the model.
     *
     * @param entity the entity to refer
     */
    void refer(IDiscourseEntity entity);

    /**
     * Method getEntities gets the entities of this <tt>IDiscourseModel</tt> object.
     *
     * @return Iterable<IDiscourseEntity> the discourse entities
     */
    Iterable<IDiscourseEntity> getEntities();

    /**
     * Method isNew checks if a given word is assigned to any of the discourse entities in the model.
     *
     * @param word the word
     * @return boolean <tt>true</tt> if given word is <tt>NEW</tt>
     */
    boolean isNew(Word word);

    /** Method clearState clears discourse state. */
    void clearState();
}

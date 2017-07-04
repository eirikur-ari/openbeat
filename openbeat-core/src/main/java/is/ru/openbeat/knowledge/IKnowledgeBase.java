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

package is.ru.openbeat.knowledge;

import java.util.List;

/**
 * Interface <tt>IKnowledgeBase</tt> withholds methods implemented in a domain knowledge base for OpenBEAT.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
public interface IKnowledgeBase {
    /**
     * Returns all instances in the knowledge base.
     *
     * @return list of all instances
     */
    List<IKnowledgeInstance> getAllInstances();

    /**
     * Returns all scenes in the knowledge base.
     *
     * @return list of all scenes
     */
    List<IKnowledgeScene> getAllScenes();

    /**
     * Returns all types in the knowledge base.
     *
     * @return list of all types
     */
    List<IKnowledgeType> getAllTypes();

    /**
     * Returns the instance in the knowledge base that has values that correspond most closely to the words found in the
     * passed description. null is returned if there is no clear best match or if there is no match at all.
     *
     * @param description the value of the instance attribute
     * @return best instance if any, else null
     */
    IKnowledgeInstance getBestInstanceMatch(String description);

    /**
     * Returns the gesture as a single compact gesture in a form suitable for a single behavior suggestion element.
     *
     * @param value the value of the gesture instance
     * @return compact gesture if found, else null
     */
    IGesture getCompactGesture(String value);

    /**
     * Gets a gesture by a given id.
     *
     * @param id the id of the gesture
     * @return gesture if found, else null
     */
    IGesture getGesture(String id);

    /**
     * Gets an instance by a given id.
     *
     * @param id the id of the instance
     * @return instance if found, else null
     */
    IKnowledgeInstance getInstance(String id);

    /**
     * Returns true if an object with an id of objectId is found inside the scene with id of sceneId, returns false
     * otherwise
     *
     * @param sceneId  the id of the scene
     * @param objectId the id of the object
     * @return true if observable, otherwise false
     */
    boolean isObservable(String sceneId, String objectId);
}

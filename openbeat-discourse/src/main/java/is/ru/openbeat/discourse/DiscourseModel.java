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

import com.google.inject.Singleton;
import is.ru.openbeat.model.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Class <tt>DiscourseModel</tt> uses the recency method to find new entities in each discourse entity
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
@Singleton
public class DiscourseModel implements IDiscourseModel {
    private static final Logger log = LoggerFactory.getLogger(DiscourseModel.class);

    private final Deque<IDiscourseEntity> recent = new ArrayDeque<IDiscourseEntity>();

    public DiscourseModel() {
    }

    public void addEntity(IDiscourseEntity entity) {
        recent.addFirst(entity);
    }

    public void refer(IDiscourseEntity entity) {
        if (!recent.contains(entity)) {
            throw new IllegalStateException("Attempting to move " + entity + " up recent list without it being in it");
        }

        final IDiscourseEntity mostRecent = recent.getFirst();
        if (mostRecent != null && !mostRecent.equals(entity)) {
            log.debug("Moving {} to beginning of recency list", entity);

            recent.remove(entity);
            recent.addFirst(entity);
        }
    }

    public Iterable<IDiscourseEntity> getEntities() {
        return recent;
    }

    public boolean isNew(Word word) {
        for (IDiscourseEntity entity : recent) {
            if (entity.getWord() == word) { // compare instances, not equality
                return true;
            }
        }
        return false;
    }

    public void clearState() {
        recent.clear();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "recent=" + recent +
            '}';
    }
}

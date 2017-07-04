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

package is.ru.openbeat.behavior;

import is.ru.openbeat.participation.Participant;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * <tt>GazeBehavior</tt> stores the information about the direction, priority and the participant focus of the behavior.
 * Also it creates the BML for the behavior.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public class GazeBehavior implements IBehavior, IBmlProducer, IMcNeillProducer {
    private static final Logger log = LoggerFactory.getLogger(GazeBehavior.class);

    private final Direction direction;
    private Integer priority;
    private Participant focus;

    public GazeBehavior(Direction direction, Integer priority) {
        this.direction = direction;
        this.priority = priority;
    }

    public GazeBehavior(Direction direction, Integer priority, Participant focus) {
        this.direction = direction;
        this.priority = priority;
        this.focus = focus;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }

    public String createBml(Float beginTime, Float endTime) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<gaze");

        if (Direction.TOWARDS_HEARER.equals(direction)) {
            if (focus != null) {
                final String name = focus.getName() != null ? focus.getName() : "Camera";
                log.debug("Setting gaze focus on {}", name);
                sb.append(" target=\"").append(name).append("\"");
            }
        } else if (Direction.AWAY_FROM_HEARER.equals(direction)) {
            // TODO: this is hardcoded to camera (for demo purpose) -- make this configurable
            sb.append(" target=\"Camera\"");
        }

        if (beginTime != null) {
            sb.append(" start=\"").append(String.format("%.1f", beginTime)).append("\"");
        }

        /*if (endTime != null) {
            sb.append(" end=\"").append(endTime).append("\"");
        }*/

        sb.append("/>");
        return sb.toString();
    }

    public String createPrefix() {
        if (Direction.AWAY_FROM_HEARER.equals(direction)) {
            return "(GA)";
        } else if (Direction.TOWARDS_HEARER.equals(direction)) {
            return "(GT)";
        }
        return null;
    }

    public String createPostfix() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GazeBehavior that = (GazeBehavior) o;

        if (direction != that.direction) return false;
        if (focus != null ? !focus.equals(that.focus) : that.focus != null) return false;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = direction != null ? direction.hashCode() : 0;
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (focus != null ? focus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "direction=" + direction +
            ", priority=" + priority +
            ", focus=" + focus +
            '}';
    }

    public static enum Direction {
        TOWARDS_HEARER, TOWARDS_SPEAKER, AWAY_FROM_HEARER
    }
}

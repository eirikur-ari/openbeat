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

import is.ru.openbeat.knowledge.IArm;
import is.ru.openbeat.knowledge.IGesture;

import java.util.List;

/**
 * <tt>GestureBehavior</tt> stores the information of the type, value, priority, list of arms and gestureType of the behavior.
 *
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
public class GestureBehavior implements IGesture, IBmlProducer {
    private String type;
    private String value;
    private Integer priority;
    private List<IArm> arms;
    private GestureType gestureType;

    public GestureBehavior() {
    }

    public GestureBehavior(String type, String value, List<IArm> arms) {
        this.type = type;
        this.value = value;
        this.arms = arms;
    }

    public GestureBehavior(String type, String value, List<IArm> arms, GestureType gestureType) {
        this.type = type;
        this.value = value;
        this.arms = arms;
        this.gestureType = gestureType;
    }

    public GestureBehavior(String type, String value, GestureType gestureType) {
        this.type = type;
        this.value = value;
        this.gestureType = gestureType;
    }

     public GestureBehavior(String type, String value, GestureType gestureType, Integer priority) {
        this.type = type;
        this.value = value;
        this.gestureType = gestureType;
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<IArm> getArms() {
        return arms;
    }

    public void setArms(List<IArm> arms) {
        this.arms = arms;
    }

    public GestureType getGestureType() {
        return gestureType;
    }

    public void setGestureType(GestureType gestureType) {
        this.gestureType = gestureType;
    }

    public String createBml(Float beginTime, Float endTime) {
        if ("iconic".equals(type) && !arms.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("<gesture");
            final IArm arm = arms.get(0);
            sb.append(" type=\"").append(arm.getHandshape()).append("\"");

            if (beginTime != null) {
                sb.append(" start=\"").append(String.format("%.1f", beginTime)).append("\"");
            }

            /*if (endTime != null) {
                sb.append(" end=\"").append(endTime).append("\"");
            }*/

            sb.append("/>");
            return sb.toString();
        } else if ("beat".equals(type)) {
            final StringBuilder sb = new StringBuilder();
            sb.append("<gesture");

            sb.append(" type=\"").append(value).append("\"");

            if (beginTime != null) {
                sb.append(" start=\"").append(String.format("%.1f", beginTime)).append("\"");
            }

            /*if (endTime != null) {
                sb.append(" end=\"").append(endTime).append("\"");
            }*/

            sb.append("/>");
            return sb.toString();
        } else if ("deictic".equals(type)) {
            final StringBuilder sb = new StringBuilder();
            sb.append("<gesture");

            sb.append(" type=\"you\"");
            sb.append(" target=\"Camera\""); // target SHOULD BE value

            if (beginTime != null) {
                sb.append(" start=\"").append(String.format("%.1f", beginTime)).append("\"");
            }

            /*if (endTime != null) {
                sb.append(" end=\"").append(endTime).append("\"");
            }*/

            sb.append("/>");
            return sb.toString();
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GestureBehavior that = (GestureBehavior) o;

        if (arms != null ? !arms.equals(that.arms) : that.arms != null) return false;
        if (gestureType != that.gestureType) return false;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (arms != null ? arms.hashCode() : 0);
        result = 31 * result + (gestureType != null ? gestureType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GestureBehavior{" +
            "type='" + type + '\'' +
            ", value='" + value + '\'' +
            ", priority=" + priority +
            ", arms=" + arms +
            ", gestureType=" + gestureType +
            '}';
    }
}
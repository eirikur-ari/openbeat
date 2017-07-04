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

import java.util.StringTokenizer;

/**
 * Class <tt>KnowledgeFeature</tt> withholds attributes of the <tt>KnowledgeType</tt>
 *
 * @author gudleifur05@ru.is (Guðleifur Kristjánsson)
 */
public class KnowledgeFeature implements IKnowledgeFeature {
    private String name;
    private String typical;
    private FeatureType type;

    public KnowledgeFeature() {
    }

    public KnowledgeFeature(String name, String typical, FeatureType type) {
        this.name = name;
        this.typical = typical;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypical() {
        return typical;
    }

    public void setTypical(String typical) {
        this.typical = typical;
    }

    public FeatureType getType() {
        return type;
    }

    public void setType(FeatureType type) {
        this.type = type;
    }

    /**
     * Returns true if the value passed falls within this feature's typical range
     *
     * @param value a string to be evaluated
     * @return
     */
    public boolean isTypical(String value) {
        if (IKnowledgeFeature.FeatureType.SYM.equals(type)) {
            if (typical.isEmpty()) {
                return false;
            } else if ("any".equals(typical)) {
                return true;
            } else if (typical.equals(value)) {
                return true;
            }
        } else if (IKnowledgeFeature.FeatureType.NUM.equals(type)) {
            final StringTokenizer tokenizer = new StringTokenizer(value, "-");
            final String first = tokenizer.nextToken();
            String last = first;
            if (tokenizer.hasMoreTokens()) {
                last = tokenizer.nextToken();
            }
            final double minValue = Double.valueOf(first);
            final double maxValue = Double.valueOf(last);
            final double dValue = Double.valueOf(value);

            if (dValue >= minValue && dValue <= maxValue) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KnowledgeFeature that = (KnowledgeFeature) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != that.type) return false;
        if (typical != null ? !typical.equals(that.typical) : that.typical != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (typical != null ? typical.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KnowledgeFeature{" +
            "name='" + name + '\'' +
            ", typical='" + typical + '\'' +
            ", type=" + type +
            '}';
    }
}
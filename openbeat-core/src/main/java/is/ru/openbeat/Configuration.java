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

package is.ru.openbeat;

import com.google.inject.Provider;

/**
 * Class <tt>Configuration</tt> is an extension to guice {@link Provider} which supports mutating the variable inside
 * the provider.
 * <p/>
 * This allows certain configurations to be altered during the runtime.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 */
public abstract class Configuration<T> implements Provider<T> {
    T t;

    public Configuration() {
    }

    public Configuration(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }

    public abstract void set(String s);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "t=" + t +
            '}';
    }

    public static Configuration<String> of(String s) {
        return new StringConfiguration(s);
    }

    public static Configuration<Double> of(Double d) {
        return new DoubleConfiguration(d);
    }

    public static Configuration<Float> of(Float f) {
        return new FloatConfiguration(f);
    }

    public static Configuration<Integer> of(Integer i) {
        return new IntegerConfiguration(i);
    }

    static class StringConfiguration extends Configuration<String> {
        StringConfiguration(String s) {
            super(s);
        }

        public void set(String s) {
            this.t = s;
        }
    }

    static class DoubleConfiguration extends Configuration<Double> {
        DoubleConfiguration(Double aDouble) {
            super(aDouble);
        }

        public void set(String s) {
            this.t = Double.valueOf(s);
        }
    }

    static class FloatConfiguration extends Configuration<Float> {
        FloatConfiguration(Float aFloat) {
            super(aFloat);
        }

        public void set(String s) {
            this.t = Float.valueOf(s);
        }
    }

    static class IntegerConfiguration extends Configuration<Integer> {
        IntegerConfiguration(Integer integer) {
            super(integer);
        }

        public void set(String s) {
            this.t = Integer.valueOf(s);
        }
    }
}

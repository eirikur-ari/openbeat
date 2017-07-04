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

package is.ru.openbeat.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Statement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Class <tt>BuilderFactory</tt> is a proxy handler which intercepts calls to a {@link Builder} and performs method
 * invocation on an instance of the builders type.
 * <p/>
 * Note: there is some performance penalty when creating objects like this -- a bit reflection heavy, especially if
 * constructing large object graphs. However, this is mainly intended to be used in test fixtures.
 *
 * @author arnir06@ru.is (Árni Hermann Reynisson)
 * @param <O> the type of object instance this builder factory will create
 */
public class BuilderFactory<O> implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(BuilderFactory.class);

    private O object;

    public BuilderFactory(O object) {
        this.object = object;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String name = method.getName();
        if ("build".equals(name)) {
            return object;
        } else {
            if (method.isVarArgs()) {
                log.trace("Found varargs method {}", method);

                // attempt to "add" the objects to the collection (via addCollection)
                final String adder = "add" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length() - 1);
                try {
                    for (Object argument : ((Object[]) args[0])) {
                        final Statement statement = new Statement(object, adder, new Object[]{argument});
                        statement.execute();
                    }
                } catch (RuntimeException e) {
                    log.warn("Could not invoke adder '{}' on {}", adder, object);
                    log.warn("Exception invoked", e);
                }

                // TODO: else, attempt to set the whole collection (via setCollection)
            } else {
                // attempt to set the object (via setObject)
                final String setter = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
                try {
                    final Statement statement = new Statement(object, setter, args);
                    statement.execute();
                } catch (RuntimeException e) {
                    log.warn("Could not invoke setter '{}' on {}", setter, object);
                    log.warn("Exception invoked", e);
                }
            }
        }
        return proxy;
    }

    public static <T extends Builder<U>, U> T create(U object, Class<T> builderClass) {
        final BuilderFactory<U> handler = new BuilderFactory<U>(object);

        //noinspection unchecked
        return (T) Proxy.newProxyInstance(builderClass.getClassLoader(), new Class[]{builderClass}, handler);
    }
}

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

package is.ru.openbeat.multimethod;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.sortedCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class Multimethod {
    private static final Logger log = LoggerFactory.getLogger(Multimethod.class);

    private final List<Method> methods = newArrayList();
    private final Object self;

    public Multimethod(Object self) {
        this(self, Multi.class);
    }

    public Multimethod(Object self, Class<? extends Annotation> target) {
        this.self = self;

        findMethods(self.getClass(), target);
    }

    public Multimethod(Class<?> source) {
        this(source, Multi.class);
    }

    public Multimethod(Class<?> source, Class<? extends Annotation> target) {
        this.self = null;

        findMethods(source, target);
    }

    private void findMethods(Class<?> source, Class<? extends Annotation> target) {
        final List<Method> unsortedMethods = newArrayList();
        for (Method method : source.getDeclaredMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (target.isAssignableFrom(annotation.getClass())) {
                    if (!method.isAccessible()) {
                        log.trace("Setting method {} as accessible", method);
                        method.setAccessible(true);
                    }
                    unsortedMethods.add(method);
                }
            }
        }

        methods.addAll(sortedCopy(unsortedMethods, new MethodComparator()));
        if (log.isTraceEnabled()) {
            for (Method method : methods) {
                log.trace("Method: {}", method.getName());
            }
        }
    }

    /**
     * Matches a method based on the arguments given. Throws exception if method not found.
     *
     * @param args the arguments
     * @param <T>  the return type
     * @return the object from the matched method
     */
    public <T> T invoke(Object... args) {
        final Object context = self != null ? self : args[0];
        final Object[] a = self != null ? args : Arrays.copyOfRange(args, 1, args.length); // TODO: second case untested
        final Method method = findMethod(a);

        if (method == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("No applicable/public method found for arguments in '").append(context).append("': ");
            for (Object arg : args) {
                sb.append("'").append(arg).append("'");
            }
            throw new RuntimeException(sb.toString());
        }

        try {
            return (T) method.invoke(context, a);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Method invocation failed, illegal access '" + method.getName() + "'", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Method invocation failed '" + method.getName() + "'", e);
        }
    }

    /**
     * Matches a method based on the arguments given. Logs to low level (trace) if method not found.
     *
     * @param args the arguments
     * @param <T>  the return type
     * @return the object from the matched method
     */
    public <T> T match(Object... args) {
        final Object context = self != null ? self : args[0];
        final Object[] a = self != null ? args : Arrays.copyOfRange(args, 1, args.length); // TODO: second case untested
        final Method method = findMethod(a);

        if (method == null) {
            log.trace("No applicable/public method found for arguments in '{}': ", context);
            for (Object arg : args) {
                log.trace("  '{}'", arg);
            }
            return null;
        }

        try {
            return (T) method.invoke(context, a);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Method invocation failed, illegal access '" + method.getName() + "'", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Method invocation failed '" + method.getName() + "'", e);
        }
    }

    private Method findMethod(Object... args) {
        for (final Method method : methods) {
            if (isApplicable(method, args)) {
                return method;
            }
        }

        return null;
    }

    private boolean isApplicable(Method method, Object... args) {
        final Class[] classes = method.getParameterTypes();
        if (args.length != classes.length) {
            return false;
        }
        for (int i = 0; i < classes.length; i++) {
            final Class klass = classes[i];
            if (!klass.isInstance(args[i])) {
                if (args[i] != null || !Object.class.equals(classes[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class MethodComparator implements Comparator<Method>, Serializable {
        public int compare(Method l, Method r) {
            // most specific methods first
            final Class[] lc = l.getParameterTypes();
            final Class[] rc = r.getParameterTypes();

            if (lc.length <= rc.length) {
                return compare(lc, rc, 1);
            } else {
                return compare(rc, lc, -1);
            }
        }

        /** Ensures correct comparison of method parameters. The definition order of the parameteres */
        private int compare(Class[] c1, Class[] c2, int mul) {
            for (int i = 0; i < c1.length; i++) {
                if (!c1[i].equals(c2[i])) {
                    if (c1[i].isAssignableFrom(c2[i])) {
                        return mul;
                    }
                }
            }
            return -1 * mul;
        }
    }
}

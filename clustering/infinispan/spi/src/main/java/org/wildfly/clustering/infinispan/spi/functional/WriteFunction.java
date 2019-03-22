/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.clustering.infinispan.spi.functional;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.infinispan.functional.EntryView.ReadWriteEntryView;

/**
 * Function that performs a read-write operation on a cache entry.
 * @author Paul Ferraro
 */
public class WriteFunction<T, K, V, R> implements BiFunction<T, ReadWriteEntryView<K, V>, R> {
    private final BiFunction<V, T, R> function;
    private final UnaryOperator<V> copier;
    private final Supplier<V> factory;
//    private final Function<V, Boolean> empty;

    public WriteFunction(BiFunction<V, T, R> function, UnaryOperator<V> copier, Supplier<V> factory, Function<V, Boolean> empty) {
        this.function = function;
        this.copier = copier;
        this.factory = factory;
//        this.empty = empty;
    }

    @Override
    public R apply(T operand, ReadWriteEntryView<K, V> entry) {
        V value = entry.find().map(this.copier).orElseGet(this.factory);
        R result = this.function.apply(value, operand);
        entry.set(value);
/*
        if (this.empty.apply(value)) {
            entry.remove();
        } else {
            entry.set(value);
        }
*/
        return result;
    }
}
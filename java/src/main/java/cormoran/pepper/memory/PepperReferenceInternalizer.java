/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cormoran.pepper.memory;

import java.util.Set;

import org.ehcache.sizeof.PepperObjectGraphWalker;
import org.ehcache.sizeof.filters.CombinationSizeOfFilter;
import org.ehcache.sizeof.filters.SizeOfFilter;
import org.ehcache.sizeof.filters.TypeFilter;
import org.ehcache.sizeof.impl.PassThroughFilter;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;

/**
 * This class enables 'internalization' of objects referenced by given roots. Typically, given a List of 2 equals but
 * not same Strings, this class enables replacing of the 2 references by the other. It then enables a lower heap (as the
 * 'other' String may have become GC-collectable) and possibly better performances (as some .equals operation may
 * shortcut by first checking reference comparison (i.e. '=='))
 * 
 * @author Benoit Lacelle
 *
 */
@Beta
public class PepperReferenceInternalizer implements IReferenceInternalizer {

	// These are the classes with the expected safe contract for internalization
	private static final Set<Class<?>> KNOWN_FINAL_IMMUTABLE_WITH_HASHCODE_EQUALS =
			ImmutableSet.of(String.class, java.time.LocalDate.class);

	private final PepperObjectGraphWalker walker;

	public PepperReferenceInternalizer() {
		// We byPass flyWeights as they are references automatically shared
		this(new PassThroughFilter(), true);
	}

	public PepperReferenceInternalizer(SizeOfFilter fieldFilter, boolean bypassFlyweight) {
		this(fieldFilter, bypassFlyweight, KNOWN_FINAL_IMMUTABLE_WITH_HASHCODE_EQUALS);
	}

	public PepperReferenceInternalizer(SizeOfFilter fieldFilter,
			boolean bypassFlyweight,
			Set<Class<?>> classesToInternalize) {
		TypeFilter filter = new TypeFilter();
		classesToInternalize.forEach(c -> filter.addClass(c, false));

		this.walker = new PepperObjectGraphWalker(new CombinationSizeOfFilter(filter, fieldFilter), bypassFlyweight);
	}

	@Override
	public void internalize(Object... obj) {
		walker.walk(obj);
	}

}

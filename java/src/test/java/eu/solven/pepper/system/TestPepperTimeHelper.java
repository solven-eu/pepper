/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.system;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPepperTimeHelper {

	@Test
	public void testDetectOutlier() {
		PepperTimeHelper.NB_LOG_FOR_OUTLIER.set(0);

		AtomicLong nb = new AtomicLong();
		AtomicLong max = new AtomicLong(Long.MIN_VALUE);

		// First call: this is a max
		String simpleName = getClass().getSimpleName();
		{
			Assertions
					.assertTrue(PepperTimeHelper.updateOutlierDetectorStatistics(nb, max, 0, simpleName, "methodName"));

			Assertions.assertEquals(1L, nb.get());
			Assertions.assertEquals(0L, max.get());
		}

		// Second call: 1 > 0
		{
			Assertions
					.assertTrue(PepperTimeHelper.updateOutlierDetectorStatistics(nb, max, 1, simpleName, "methodName"));

			Assertions.assertEquals(2L, nb.get());
			Assertions.assertEquals(1L, max.get());
		}

		// Third call: 1 == 1
		{
			Assertions.assertFalse(
					PepperTimeHelper.updateOutlierDetectorStatistics(nb, max, 1, simpleName, "methodName"));

			Assertions.assertEquals(3L, nb.get());
			Assertions.assertEquals(1L, max.get());
		}

		// Third call: 0 < 1
		{
			Assertions.assertFalse(
					PepperTimeHelper.updateOutlierDetectorStatistics(nb, max, 0, simpleName, "methodName"));

			Assertions.assertEquals(4L, nb.get());
			Assertions.assertEquals(1L, max.get());
		}

		{
			Assertions
					.assertTrue(PepperTimeHelper.updateOutlierDetectorStatistics(nb, max, 2, simpleName, "methodName"));

			Assertions.assertEquals(5L, nb.get());
			Assertions.assertEquals(2L, max.get());
		}

		// We should not have logged as not enough occurrences
		Assertions.assertEquals(0L, PepperTimeHelper.NB_LOG_FOR_OUTLIER.get());
	}

	@Test
	public void testDetectOutlierMoreInfos() {
		PepperTimeHelper.NB_LOG_FOR_OUTLIER.set(0);

		AtomicLong nb = new AtomicLong(PepperTimeHelper.NB_OCCURENCES_FOR_INFO);
		AtomicLong max = new AtomicLong(128);

		String simpleName = getClass().getSimpleName();

		// First call: this is a max
		Assertions.assertTrue(PepperTimeHelper
				.updateOutlierDetectorStatistics(nb, max, 317, simpleName, "methodName", "more", "evenMore"));

		// We should have logged once as nb was NB_OCCURENCES_FOR_INFO
		Assertions.assertEquals(1L, PepperTimeHelper.NB_LOG_FOR_OUTLIER.get());
	}
}

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
package cormoran.pepper.jvm;

import java.lang.management.MemoryUsage;
import java.util.Map;

import javax.management.openmbean.CompositeData;

import com.sun.management.GarbageCollectionNotificationInfo;

/**
 * Wrap the usage of GarbageCollectionNotificationInfo which is restricted API
 * 
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings("restriction")
public class PepperGarbageCollectionNotificationInfo implements IPepperGarbageCollectionNotificationInfo {
	protected final GarbageCollectionNotificationInfo info;

	public PepperGarbageCollectionNotificationInfo(GarbageCollectionNotificationInfo info) {
		this.info = info;
	}

	@Override
	public String getGcAction() {
		return info.getGcAction();
	}

	@Override
	public long getGcDuration() {
		return info.getGcInfo().getDuration();
	}

	@Override
	public String getGcName() {
		return info.getGcName();
	}

	public static PepperGarbageCollectionNotificationInfo from(CompositeData cd) {
		return new PepperGarbageCollectionNotificationInfo(GarbageCollectionNotificationInfo.from(cd));
	}

	@Override
	public Map<String, MemoryUsage> getMemoryUsageBeforeGc() {
		return info.getGcInfo().getMemoryUsageBeforeGc();
	}

	@Override
	public Map<String, MemoryUsage> getMemoryUsageAfterGc() {
		return info.getGcInfo().getMemoryUsageAfterGc();
	}

}

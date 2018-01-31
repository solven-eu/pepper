/**
 * Copyright (C) 2014 Benoit Lacelle (benoit.lacelle@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cormoran.pepper.jvm;

/**
 * Abstract the information defining when we run out of heap. It is typically used by enquiing mechanism to stop
 * accepting data if the heap is too high
 * 
 * @author Benoit Lacelle
 *
 */
public interface IHeapIsTooHigh {

	/**
	 * By default, we accepting enqueuing data for transactions until the heap is 80% full. Above 80%, we will try doing
	 * transactions as small as possible without any enqueuing
	 */
	int DEFAULT_HEAP_RATIO_WRITE_LIMIT = 80;

	boolean getHeapIsTooHigh();

	void setRatioForTooHigh(int ratioForTooHigh);
}

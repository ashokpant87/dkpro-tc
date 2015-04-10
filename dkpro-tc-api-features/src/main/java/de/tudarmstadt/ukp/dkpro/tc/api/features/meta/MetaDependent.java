/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.api.features.meta;

import java.util.List;


/**
 * Feature extractors that depend on {@link MetaCollector}s should implemnt this interface.
 * By doing so they declare what kind of {@link MetaCollector}s are used in the MetaInfoTask.
 * 
 * @author zesch
 *
 */
public interface MetaDependent
{
    /**
     * @return A list of meta collector classes that a "meta dependent" collector depdends on
     */
    // FIXME - This should be changed to return instantiated MetaCollector objects that are already
    // configured in the way that the FeatureCollector requires them
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses();
}

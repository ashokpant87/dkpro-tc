/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit�t Darmstadt
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
package de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.regression;

import java.util.HashMap;
import java.util.LinkedList;

import de.tudarmstadt.ukp.dkpro.tc.evaluation.evaluator.EvaluatorBase;


/**
 * @author Andriy Nadolskyy
 * 
 */
public class RegressionEvaluator extends EvaluatorBase {

	public RegressionEvaluator(HashMap<String, Integer> class2number,
			LinkedList<String> readData) {
		super(class2number, readData);
	}

	@Override
	public HashMap<String, String> calculateEvaluationMeasures() {
		// TODO Auto-generated method stub
		return null;
	}
		
}
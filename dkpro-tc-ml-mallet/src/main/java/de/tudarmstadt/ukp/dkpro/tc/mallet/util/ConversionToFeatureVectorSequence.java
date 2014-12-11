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
package de.tudarmstadt.ukp.dkpro.tc.mallet.util;

import java.util.ArrayList;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.FeatureVectorSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/*
 * Modification of SimpleTagger2FeatureVectorSequence from Mallet
 * @author Krish Perumal
 */

/**
 * @deprecated As of release 0.7.0, only dkpro-tc-ml-crfsuite is supported
 */
public class ConversionToFeatureVectorSequence extends Pipe
{

	private int idFeatureIndex;
	private boolean denseFeatureValues;

	// Previously, there was no serialVersionUID.  This is ID that would 
	// have been automatically generated by the compiler.  Therefore,
	// other changes should not break serialization.  
	private static final long serialVersionUID = -2059308802200728625L;

	public ConversionToFeatureVectorSequence (boolean denseFeatureValues)
	{
		super (new Alphabet(), new LabelAlphabet());
		idFeatureIndex = -1;
	}

	/**
	 * Parses a string representing a sequence of rows of tokens into an
	 * array of arrays of tokens (Ignore first sentence containing feature names)  
	 *
	 * @param sentence a <code>String</code>
	 * @return the corresponding array of arrays of tokens.
	 */
	private String[][] parseSentence(String sentence)
	{
		String[] lines = sentence.split("\n");
		String[][] tokens = null;
		if (lines[0].matches("^[A-Za-z]+.*$")) { //parsing first line group containing feature names
			String[] featureNames = lines[0].split(" ");
			for (int i = 0; i < featureNames.length; i++) {
				if (featureNames[i].equals(Constants.ID_FEATURE_NAME)) {
					idFeatureIndex = i;
				}
			}
			if (idFeatureIndex != -1) { // if file contained the DKPro Instance ID feature
				tokens = new String[lines.length - 1][];
				String[][] tempTokens = new String[lines.length - 1][];
				for (int i = 1; i < lines.length; i++) {
					tempTokens[i - 1] = lines[i].split(" ");
				}
				for (int i = 0; i < tempTokens.length; i++) {
					tokens[i] = new String[tempTokens[i].length - 1];
					int tokenLineIndex = 0;
					for (int j = 0; j < tempTokens[i].length; j++) {
						if (j != idFeatureIndex) {
							tokens[i][tokenLineIndex++] = tempTokens[i][j];
						}
					}
				}
			}
			else {
				tokens = new String[lines.length - 1][];
				for (int i = 1; i < lines.length; i++) {
					tokens[i - 1] = lines[i].split(" ");
				}
			}
		}
		else {
			if (idFeatureIndex != -1) { // if file contained the DKPro Instance ID feature
				tokens = new String[lines.length][];
				String[][] tempTokens = new String[lines.length][];
				for (int i = 0; i < lines.length; i++) {
					tempTokens[i] = lines[i].split(" ");
				}
				for (int i = 0; i < tempTokens.length; i++) {
					tokens[i] = new String[tempTokens[i].length - 1];
					int tokenLineIndex = 0;
					for (int j = 0; j < tempTokens[i].length; j++) {
						if (j != idFeatureIndex) {
							tokens[i][tokenLineIndex++] = tempTokens[i][j];
						}
					}
				}
			}
			else {
				tokens = new String[lines.length][];
				for (int i = 0; i < lines.length; i++) {
					tokens[i] = lines[i].split(" ");
				}
			}
		}
		return tokens;
	}

	@Override
    public Instance pipe (Instance carrier)
	{
		Object inputData = carrier.getData();
		Alphabet features = getDataAlphabet();
		LabelAlphabet labels;
		LabelSequence target = null;
		String [][] tokens;
		if (inputData instanceof String)
			tokens = parseSentence((String)inputData);
		else if (inputData instanceof String[][])
			tokens = (String[][])inputData;
		else
			throw new IllegalArgumentException("Not a String or String[][]; got "+inputData);
		FeatureVector[] fvs = new FeatureVector[tokens.length];
		if (isTargetProcessing())
		{
			labels = (LabelAlphabet)getTargetAlphabet();
			target = new LabelSequence (labels, tokens.length);
		}
		
		for (int l = 0; l < tokens.length; l++) {
			int nFeatures;
			if (isTargetProcessing())
			{
				if (tokens[l].length < 1)
					throw new IllegalStateException ("Missing label at line " + l + " instance "+carrier.getName ());
				nFeatures = tokens[l].length - 1;
				target.add(tokens[l][nFeatures]);
			}
			else nFeatures = tokens[l].length;
			ArrayList<Integer> featureIndices = new ArrayList<Integer>();
			ArrayList<Double> featureValues = new ArrayList<Double>();
			for (int f = 0; f < nFeatures; f++) {
				int featureIndex = features.lookupIndex(tokens[l][f]);
				// gdruck
				// If the data alphabet's growth is stopped, featureIndex
				// will be -1.  Ignore these features.
				if (featureIndex >= 0) {
					featureIndices.add(featureIndex);
				}
				featureValues.add(Double.parseDouble(tokens[l][f]));
			}
			int[] featureIndicesArr = new int[featureIndices.size()];
			for (int index = 0; index < featureIndices.size(); index++) {
				featureIndicesArr[index] = featureIndices.get(index);
			}
			double[] featureValuesArr = new double[featureValues.size()];
			for (int index = 0; index < featureValues.size(); index++) {
				featureValuesArr[index] = featureValues.get(index);
			}
			if (denseFeatureValues)
				fvs[l] = new FeatureVector(features, featureValuesArr);
			else
				fvs[l] = new FeatureVector(features, featureIndicesArr);
			//fvs[l] = featureInductionOption.value ? new AugmentableFeatureVector(features, featureIndicesArr, null, featureIndicesArr.length) : 
			//        fvs[l] = featureInductionOption.value ? new AugmentableFeatureVector(features, featureIndicesArr, null, featureIndicesArr.length) : 
			//        	new FeatureVector(features, featureValues);
		}
		carrier.setData(new FeatureVectorSequence(fvs));
		if (isTargetProcessing())
			carrier.setTarget(target);
		else
			carrier.setTarget(new LabelSequence(getTargetAlphabet()));
		return carrier;
	}
}